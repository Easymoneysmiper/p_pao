package com.itpk.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itpk.usercenter.Exception.BusinessException;
import com.itpk.usercenter.common.errorCode;
import com.itpk.usercenter.enums.StatusEnums;
import com.itpk.usercenter.model.Team;
import com.itpk.usercenter.model.User;
import com.itpk.usercenter.model.UserTeam;
import com.itpk.usercenter.model.VO.TeamUserVO;
import com.itpk.usercenter.model.VO.UserVO;
import com.itpk.usercenter.model.dto.SearchTeamQuery;
import com.itpk.usercenter.model.request.DeleteRequest;
import com.itpk.usercenter.model.request.JoinTeamRequest;
import com.itpk.usercenter.model.request.QuitTeamReQuest;
import com.itpk.usercenter.model.request.UpdateTeamRequest;
import com.itpk.usercenter.service.TeamService;
import com.itpk.usercenter.mapper.TeamMapper;
import com.itpk.usercenter.service.UserService;
import com.itpk.usercenter.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
* @author easymoneysniper
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2025-03-23 15:36:52
*/
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;
    @Override
    public Long addTeam(Team team, User loginUser) {
        //1.检验参数
        if (team == null)
            throw new BusinessException(errorCode.NULL_ERROR);
        if (loginUser == null)
            throw new BusinessException(errorCode.NOT_LOGIN);
        validateTeamParams(team);
        //7.用户创建第几个队伍
        //todo 用户同时点100下可以创建一百个队伍
        QueryWrapper<Team> qw = new QueryWrapper<>();
        qw.eq("userId", loginUser.getId());
        long hasTeams = this.count(qw);
        if (hasTeams >= 5)
            throw new BusinessException(errorCode.PARAMS_ERROR, "最多创建5个队伍");

        //插入team
        team.setId(null);
        team.setUserId(loginUser.getId());
        boolean save = this.save(team);
//        if(true)
//            throw new BusinessException(errorCode.SYSTEM_ERROR,"插入用户队伍关系失败");
        if (!save)
            throw new BusinessException(errorCode.SYSTEM_ERROR, "创建队伍失败");
        //插入关系
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(loginUser.getId());
        userTeam.setJoinTime(new Date());
        userTeam.setTeamId(team.getId());
        boolean result = userTeamService.save(userTeam);
        if (!result)
            throw new BusinessException(errorCode.SYSTEM_ERROR, "用户队伍关系存入失败");
        return team.getId();
    }

    @Override
    public List<TeamUserVO> listTeams(SearchTeamQuery searchTeamQuery, HttpServletRequest httpServletRequest) {
        // 获取当前登录用户信息
        User currentUser = userService.getCurrentUser(httpServletRequest);
        if (currentUser == null) {
            throw new BusinessException(errorCode.NOT_LOGIN);
        }
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();

        // 1. 处理ID
        if (searchTeamQuery.getId() != null && searchTeamQuery.getId() > 0) {
            queryWrapper.eq("id", searchTeamQuery.getId());
        }

        // 2. 名称和描述联合模糊查询
        if (StringUtils.isNotBlank(searchTeamQuery.getName())) {
            String name = searchTeamQuery.getName();
            queryWrapper.and(qw -> qw.like("name", name).or().like("description", name));
        }

        // 3. 最大人数
        if (searchTeamQuery.getMaxNum() != null && searchTeamQuery.getMaxNum() > 1) {
            queryWrapper.eq("maxNum", searchTeamQuery.getMaxNum());
        }

        // 4. 创建人
        if (searchTeamQuery.getUserId() != null && searchTeamQuery.getUserId() > 0) {
            queryWrapper.eq("userId", searchTeamQuery.getUserId());
        }

        // 5. 状态处理：管理员可查看所有，非管理员只能查看公开/加密
        Integer status = searchTeamQuery.getStatus();
        if (status != null) {
            if (!userService.isAdminUser(httpServletRequest) && status == 2) {
                throw new BusinessException(errorCode.NO_AUTH, "非管理员无权查看私密队伍");
            }
            queryWrapper.eq("status", status);
        } else {
            if (!userService.isAdminUser(httpServletRequest)) {
                queryWrapper.in("status", 0, 1); // 公开和私有
            }
        }

        // 6. 创建时间（精确匹配）
        if (searchTeamQuery.getCreateTime() != null && searchTeamQuery.getCreateTime().before(new Date())) {
            queryWrapper.eq("createTime", searchTeamQuery.getCreateTime());
        }

        // 7. 排除过期队伍：expire_time为null或大于当前时间
        queryWrapper.and(qw -> qw.isNull("expireTime").or().gt("expireTime", new Date()));

        // 执行查询
        List<Team> teamList = this.list(queryWrapper);

        // 封装结果
        return teamList.stream().map(team -> {
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);

            // 查询队员信息
            List<UserVO> members = getTeamMembers(team.getId());
            teamUserVO.setUsers(members);

            return teamUserVO;
        }).collect(Collectors.toList());
    }

    //1.查询队伍是否存在
    //2.请求参数是否为空
    //3.只有队伍创建者和管理员允许修改
    //4.对比是否与旧信息相同的不用修改
    @Override
    public boolean updateTeam(UpdateTeamRequest updateTeamRequest, HttpServletRequest request) {
        // 1. 基础校验
        if (updateTeamRequest == null) {
            throw new BusinessException(errorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long teamId = updateTeamRequest.getId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(errorCode.PARAMS_ERROR, "队伍ID错误");
        }

        // 2. 登录校验
        User loginUser = userService.getCurrentUser(request);
        if (loginUser == null) {
            throw new BusinessException(errorCode.NOT_LOGIN);
        }

        // 3. 查询队伍是否存在
        Team oldTeam = this.getById(teamId);
        if (oldTeam == null) {
            throw new BusinessException(errorCode.NULL_ERROR, "队伍不存在");
        }

        // 4. 权限校验（只有创建者或管理员能修改）
        if (!oldTeam.getUserId().equals(loginUser.getId()) && !userService.isAdminUser(request)) {
            throw new BusinessException(errorCode.NO_AUTH, "无权修改该队伍");
        }

        // 5. 数据更新处理
        Team updateTeam = new Team();
        // 名称修改判断
        boolean hasChange = false;
        if (StringUtils.isNotBlank(updateTeamRequest.getName())
                && !updateTeamRequest.getName().equals(oldTeam.getName())) {
            updateTeam.setName(updateTeamRequest.getName());
            hasChange = true;
        }

        // 描述修改判断
        if (StringUtils.isNotBlank(updateTeamRequest.getDescription())
                && !updateTeamRequest.getDescription().equals(oldTeam.getDescription())) {
            updateTeam.setDescription(updateTeamRequest.getDescription());
            hasChange = true;
        }

        // 过期时间修改判断（处理可能的null值）
        if (updateTeamRequest.getExpireTime() != null
                && !updateTeamRequest.getExpireTime().equals(oldTeam.getExpireTime())) {
            updateTeam.setExpireTime(updateTeamRequest.getExpireTime());
            hasChange = true;
        }

        // 状态修改判断
        if (updateTeamRequest.getStatus() != null
                && !updateTeamRequest.getStatus().equals(oldTeam.getStatus())) {
            updateTeam.setStatus(updateTeamRequest.getStatus());
            hasChange = true;
        }

        // 密码修改判断（需结合状态校验）
        if (StringUtils.isNotBlank(updateTeamRequest.getPassword())
                && !updateTeamRequest.getPassword().equals(oldTeam.getPassword())) {
            updateTeam.setPassword(updateTeamRequest.getPassword());
            hasChange = true;
        }

        // 如果没有字段变化，直接返回
        if (!hasChange) {
            log.info("队伍 {} 信息未变化，跳过更新", teamId);
            return true;
        }
        // 6. 参数校验（复用创建时的校验规则）
        validateTeamParams(updateTeam);
        updateTeam.setUpdateTime(new Date()); // 强制更新时间
        // 8. 执行更新
        return this.updateById(updateTeam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean join(JoinTeamRequest joinTeamRequest, HttpServletRequest request) {
        // 1. 基础参数校验
        if (joinTeamRequest == null) {
            throw new BusinessException(errorCode.PARAMS_ERROR);
        }
        Long teamId = joinTeamRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(errorCode.PARAMS_ERROR, "队伍ID错误");
        }

        // 2. 登录校验
        User loginUser = userService.getCurrentUser(request);
        if (loginUser == null) {
            throw new BusinessException(errorCode.NOT_LOGIN);
        }
        final Long userId = loginUser.getId();

        // 3. 查询队伍信息（带排他锁防止并发修改）
        Team team = this.getBaseMapper().selectTeamForUpdate(teamId);
        if (team == null) {
            throw new BusinessException(errorCode.NULL_ERROR, "队伍不存在");
        }


        // 4. 校验队伍状态
        if (team.getStatus() == 1) {
            throw new BusinessException(errorCode.NO_AUTH, "私有队伍不可加入");
        }
        // 5. 校验过期时间
        if (team.getExpireTime() != null && team.getExpireTime().before(new Date())) {
            throw new BusinessException(errorCode.PARAMS_ERROR, "队伍已过期");
        }
        // 9. 加密队伍校验密码
        if (team.getStatus() == 2) {
            String inputPassword = joinTeamRequest.getPassword();
            if (StringUtils.isBlank(inputPassword) || !inputPassword.equals(team.getPassword())) {
                throw new BusinessException(errorCode.PARAMS_ERROR, "密码错误");
            }
        }

         RLock lock = redissonClient.getLock("p_pao:team:join");
            try {
                while (true){
                    if( lock.tryLock(0,-1, TimeUnit.MILLISECONDS))
                {
                    System.out.println("currentLock:"+lock.getName());
                    // 8. 校验是否已加入
                    if (userTeamService.isUserInTeam(userId, teamId)) {
                        throw new BusinessException(errorCode.PARAMS_ERROR, "请勿重复加入");
                    }

                    // 6. 校验队伍人数
                    Integer currentMembers = userTeamService.countTeamMembers(teamId);
                    if (currentMembers >= team.getMaxNum()) {
                        throw new BusinessException(errorCode.PARAMS_ERROR, "队伍人数已满");
                    }

                    // 7. 校验用户已加入队伍数
                    Integer userJoinedCount = userTeamService.countUserTeams(userId);
                    if (userJoinedCount >= 5) {
                        throw new BusinessException(errorCode.PARAMS_ERROR, "最多加入5个队伍");
                    }

                    // 10. 创建用户-队伍关系
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }}
            }
            catch (Exception e) {
                log.error("Redisson error", e);
                return false;
            }
            finally {
                if (lock.isHeldByCurrentThread())
                    lock.unlock();
            }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(QuitTeamReQuest quitTeamReQuest, HttpServletRequest request) {
        if(quitTeamReQuest == null) {
            throw new BusinessException(errorCode.PARAMS_ERROR);
        }
        if(request == null) {
            throw new BusinessException(errorCode.NO_AUTH);
        }
        Long teamId = quitTeamReQuest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw  new BusinessException(errorCode.PARAMS_ERROR);
        }
        Team targetTeam=this.getById(teamId);
        if(targetTeam == null) {
            throw new BusinessException(errorCode.PARAMS_ERROR,"队伍不存在");
        }
        validateTeamParams(targetTeam);
        User loginUser = userService.getCurrentUser(request);
        if (loginUser == null) {
            throw new BusinessException(errorCode.NOT_LOGIN);
        }
        Long userId = loginUser.getId();
        //检验是否在队伍中
        if(!userTeamService.isUserInTeam(userId, teamId)) {
            throw new BusinessException(errorCode.PARAMS_ERROR,"您不在该队伍中");
        }
        //队伍只有一人：解散！！
        if(userTeamService.countTeamMembers(teamId)<=1)
        {
            boolean b = this.removeById(teamId);
            if(!b) {
                throw new BusinessException(errorCode.PARAMS_ERROR,"队伍解散失败");
            }
        }
        else {
            //是队长：转给第二顺位
            if(targetTeam.getUserId().equals(userId))
            {
                QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("teamId", teamId)
                        .last("order by id asc limit 2");
                List<UserTeam> userTeams = userTeamService.list(queryWrapper);
                targetTeam.setUserId(userTeams.get(1).getUserId());
                boolean result = this.updateById(targetTeam);
                if (!result)
                {
                    throw new BusinessException(errorCode.SYSTEM_ERROR,"队长转移失败");
                }
            }
        }
        return userTeamService.removeUserTeam(userId,teamId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(DeleteRequest deleteRequest, HttpServletRequest request) {
        if(deleteRequest==null||request==null)
            throw new BusinessException(errorCode.NULL_ERROR);
        long teamId=deleteRequest.getTeamId();
        if(teamId<=0)
        {
            throw new BusinessException(errorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        if (loginUser == null) {
            throw new BusinessException(errorCode.NOT_LOGIN);
        }
        Team targetTeam=this.getById(teamId);
        if(targetTeam == null) {
            throw new BusinessException(errorCode.PARAMS_ERROR,"队伍不存在");
        }
        //是否为队长
        if(!targetTeam.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(errorCode.NO_AUTH,"你不是该队伍队长，无权限解散队伍");
        }
        boolean remove = this.removeById(teamId);
        if (!remove)
        {
            throw new BusinessException(errorCode.SYSTEM_ERROR,"队伍删除异常");
        }
        boolean result = userTeamService.removeUserTeam(loginUser.getId(),teamId);
        if (!result) {
            throw new BusinessException(errorCode.SYSTEM_ERROR,"用户队伍关系删除失败");
        }

        return remove;
    }

    @Override
    public Page<TeamUserVO> pageListTeams(SearchTeamQuery searchTeamQuery, HttpServletRequest request) {
        // 获取当前登录用户信息
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(errorCode.NOT_LOGIN);
        }
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();

        // 1. 处理ID
        if (searchTeamQuery.getId() != null && searchTeamQuery.getId() > 0) {
            queryWrapper.eq("id", searchTeamQuery.getId());
        }

        // 2. 名称和描述联合模糊查询
        if (StringUtils.isNotBlank(searchTeamQuery.getName())) {
            String name = searchTeamQuery.getName();
            queryWrapper.and(qw -> qw.like("name", name).or().like("description", name));
        }

        // 3. 最大人数
        if (searchTeamQuery.getMaxNum() != null && searchTeamQuery.getMaxNum() > 1) {
            queryWrapper.eq("maxNum", searchTeamQuery.getMaxNum());
        }

        // 4. 创建人
        if (searchTeamQuery.getUserId() != null && searchTeamQuery.getUserId() > 0) {
            queryWrapper.eq("userId", searchTeamQuery.getUserId());
        }

        // 5. 状态处理：管理员可查看所有，非管理员只能查看公开/加密
        Integer status = searchTeamQuery.getStatus();
        if (status != null) {
            if (!userService.isAdminUser(request) && status == 2) {
                throw new BusinessException(errorCode.NO_AUTH, "非管理员无权查看私密队伍");
            }
            queryWrapper.eq("status", status);
        } else {
            if (!userService.isAdminUser(request)) {
                queryWrapper.in("status", 0, 1); // 公开和私有
            }
        }

        // 6. 创建时间（精确匹配）
        if (searchTeamQuery.getCreateTime() != null && searchTeamQuery.getCreateTime().before(new Date())) {
            queryWrapper.eq("createTime", searchTeamQuery.getCreateTime());
        }

        // 7. 排除过期队伍：expire_time为null或大于当前时间
        queryWrapper.and(qw -> qw.isNull("expireTime").or().gt("expireTime", new Date()));

        // 创建分页对象
        Page<Team> teamPage = new Page<>(searchTeamQuery.getPageNum(), searchTeamQuery.getPageSize());

        // 执行查询
        Page<Team> teamList = this.page(teamPage, queryWrapper);

        // 创建结果分页对象
        Page<TeamUserVO> resultPage = new Page<>(teamList.getCurrent(), teamList.getSize(), teamList.getTotal());

        // 转换记录
        List<TeamUserVO> teamUserVOList = teamList.getRecords().stream().map(team -> {
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);

            // 查询队员信息
            List<UserVO> members = getTeamMembers(team.getId());
            teamUserVO.setUsers(members);

            return teamUserVO;
        }).collect(Collectors.toList());

        // 设置转换后的记录
        resultPage.setRecords(teamUserVOList);

        return resultPage;
    }
    private void validateTeamParams(Team team) {

        //2.队伍标题<20
        String name = team.getName();
        if (StringUtils.isBlank(name) && name.length() > 20) {
            throw new BusinessException(errorCode.PARAMS_ERROR, "队伍名称过长");
        }
        //3.队伍人数<20
        Integer maxNum=team.getMaxNum();
        if (maxNum!=null&&(maxNum <= 1 || maxNum > 20))
            throw new BusinessException(errorCode.PARAMS_ERROR, "队伍人数错误");
        //4.队伍描述<512//
        String description = team.getDescription();
        if (StringUtils.isBlank(description) && description.length() > 512)
            throw new BusinessException(errorCode.PARAMS_ERROR, "队伍描述过长");
        // 状态校验
        //status 0时公开，1是私密
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
            if (status < 0 || status > 2) {
                throw new BusinessException(errorCode.PARAMS_ERROR, "状态参数错误");
            }
            // 加密状态需要密码
            if (StatusEnums.SECRET == StatusEnums.GetStatus(status)) {
                if (StringUtils.isBlank(team.getPassword()) ) {
                    throw new BusinessException(errorCode.PARAMS_ERROR, "加密队伍必须设置密码" );
                }
                else if(team.getPassword().length() > 32)
                {
                    throw new BusinessException(errorCode.PARAMS_ERROR,"密码长度不能超过32");
                }
            }else{
                if(!StringUtils.isBlank(team.getPassword())){
                    throw new BusinessException(errorCode.PARAMS_ERROR,"该状态队伍无法设置密码");
                }

            }
        // 过期时间校验
        if (team.getExpireTime() != null) {
            if (team.getExpireTime().before(new Date())) {
                throw new BusinessException(errorCode.PARAMS_ERROR, "过期时间不能早于当前时间");
            }
        }
    }



    /**
     * 查询队伍成员
     */
    private List<UserVO> getTeamMembers(Long teamId) {
        QueryWrapper<UserTeam> userTeamQuery = new QueryWrapper<>();
        userTeamQuery.eq("teamId", teamId).eq("isDelete", 0);
        List<UserTeam> userTeams = userTeamService.list(userTeamQuery);

        List<Long> userIds = userTeams.stream()
                .map(UserTeam::getUserId)
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询用户信息
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        userQuery.in("id", userIds);
        return userService.list(userQuery)
                .stream()
                .map(user -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    return userVO;
                }).collect(Collectors.toList());
    }

}



