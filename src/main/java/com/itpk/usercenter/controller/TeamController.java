package com.itpk.usercenter.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itpk.usercenter.Exception.BusinessException;
import com.itpk.usercenter.common.BasicResponse;
import com.itpk.usercenter.common.ResultUtils;
import com.itpk.usercenter.common.errorCode;
import com.itpk.usercenter.model.Team;
import com.itpk.usercenter.model.User;
import com.itpk.usercenter.model.VO.TeamUserVO;
import com.itpk.usercenter.model.dto.SearchTeamQuery;
import com.itpk.usercenter.model.request.JoinTeamRequest;
import com.itpk.usercenter.model.request.TeamAddRequest;
import com.itpk.usercenter.model.request.UpdateTeamRequest;
import com.itpk.usercenter.service.TeamService;
import com.itpk.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import static com.itpk.usercenter.constant.UserConstant.USER_LOGIN_STATE;


@RestController
@RequestMapping("api/team")
@CrossOrigin(origins = {"http://localhost:3000","http://localhost:5173"}, allowCredentials = "true")
@Slf4j
public class TeamController  {
    @Resource
    private TeamService teamService;
    @Resource
    private UserService userService;
   @PostMapping("/add")
    public BasicResponse addTeam(@RequestBody TeamAddRequest teamAddRequest , HttpServletRequest request) {

       if(teamAddRequest==null)
       {
           throw new BusinessException(errorCode.NULL_ERROR);
       }
       Team team = new Team();
       BeanUtils.copyProperties(teamAddRequest,team);
       User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
       Long teamId = teamService.addTeam(team,user);
       if (teamId==null)
       {
           throw new BusinessException(errorCode.SYSTEM_ERROR,"队伍创建异常");
       }
       return ResultUtils.success(teamId);
   }
    @PostMapping("/delete")
    public BasicResponse deleteTeam(@RequestBody Long teamId) {
        if(teamId<0)
        {
            throw new BusinessException(errorCode.PARAMS_ERROR);
        }
        boolean remove = teamService.removeById(teamId);
        if (!remove)
        {
            throw new BusinessException(errorCode.SYSTEM_ERROR,"队伍删除异常");
        }
        return ResultUtils.success(remove);
    }
    @PostMapping("/update")
    public BasicResponse<Boolean> updateTeam(@RequestBody UpdateTeamRequest updateTeamRequest , HttpServletRequest request) {
        if(updateTeamRequest==null)
        {
            throw new BusinessException(errorCode.NULL_ERROR);
        }
        boolean save = teamService.updateTeam(updateTeamRequest,request);
        if (!save)
        {
            throw new BusinessException(errorCode.SYSTEM_ERROR,"队伍更新失败");
        }
        return ResultUtils.success(save);
    }
    @GetMapping("/search")
    public BasicResponse searchTeam(@RequestBody Long teamId) {
        if(teamId<0)
        {
            throw new BusinessException(errorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(teamId);
        if (team == null)
        {
            throw new BusinessException(errorCode.SYSTEM_ERROR,"队伍创建异常");
        }
        return ResultUtils.success(team);
    }
//    @GetMapping("/list")
//    public BasicResponse<List<Team>> listTeams(@RequestBody SearchTeamQuery  searchTeamQuery) {
//        if(searchTeamQuery==null)
//        {
//            throw new BusinessException(errorCode.NULL_ERROR);
//        }
//        Team team = new Team();
//        BeanUtils.copyProperties(searchTeamQuery,team);
//        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
//        List<Team> teams = teamService.list(queryWrapper);
//        if (teams==null)
//        {
//            throw new BusinessException(errorCode.SYSTEM_ERROR,"队伍列表查询异常");
//        }
//        return ResultUtils.success(teams);
//    }
@PostMapping("/list")
public BasicResponse<List<TeamUserVO>> listTeams(@RequestBody SearchTeamQuery  searchTeamQuery,HttpServletRequest httpServletRequest) {
    if(searchTeamQuery==null)
    {
        throw new BusinessException(errorCode.NULL_ERROR);
    }
    List<TeamUserVO> teams =teamService.listTeams(searchTeamQuery,httpServletRequest);
    if (teams==null)
    {
        throw new BusinessException(errorCode.SYSTEM_ERROR,"队伍列表查询异常");
    }
    return ResultUtils.success(teams);
}

   @GetMapping("/list/page")
    public BasicResponse<Page<Team>> searchTeamInPage(@RequestBody SearchTeamQuery  searchTeamQuery) {
        if(searchTeamQuery==null)
        {
            throw new BusinessException(errorCode.NULL_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(searchTeamQuery,team);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> page=new Page<>(searchTeamQuery.getPageNum(),searchTeamQuery.getPageSize());
        Page<Team> teams = teamService.page(page, queryWrapper);
        if (teams==null)
        {
            throw new BusinessException(errorCode.SYSTEM_ERROR,"队伍列表查询异常");
        }
        return ResultUtils.success(teams);
    }
    @PostMapping("/join")
    public BasicResponse<Boolean> joinTeam(@RequestBody JoinTeamRequest joinTeamRequest , HttpServletRequest request) {
       if (joinTeamRequest==null)
           throw new BusinessException(errorCode.NULL_ERROR);
       if (request==null)
           throw new BusinessException(errorCode.NULL_ERROR);
       boolean result=teamService.join(joinTeamRequest,request);
       if (!result)
           throw new BusinessException(errorCode.SYSTEM_ERROR,"加入队伍失败");
       return ResultUtils.success(result);
    }
}


