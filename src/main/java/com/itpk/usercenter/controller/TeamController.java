package com.itpk.usercenter.controller;



import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itpk.usercenter.Exception.BusinessException;
import com.itpk.usercenter.common.BasicResponse;
import com.itpk.usercenter.common.ResultUtils;
import com.itpk.usercenter.common.errorCode;
import com.itpk.usercenter.model.Team;
import com.itpk.usercenter.model.User;
import com.itpk.usercenter.model.VO.TeamUserVO;
import com.itpk.usercenter.model.dto.SearchTeamQuery;
import com.itpk.usercenter.model.request.*;
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
@CrossOrigin(origins = {"https://ppao.easy-money-code.com","http://localhost:5173"}, allowCredentials = "true")
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
    public BasicResponse deleteTeam(@RequestBody DeleteRequest deleteRequest , HttpServletRequest request) {
       if(deleteRequest==null||request==null)
           throw new BusinessException(errorCode.NULL_ERROR);

       boolean result = teamService.deleteTeam(deleteRequest,request);
        return ResultUtils.success(result);
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
    if(searchTeamQuery==null||httpServletRequest==null)
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

   @PostMapping("/list/page")
    public BasicResponse<Page<TeamUserVO>> searchTeamInPage(@RequestBody SearchTeamQuery  searchTeamQuery,HttpServletRequest request) {
        if(searchTeamQuery==null||request==null)
        {
            throw new BusinessException(errorCode.NULL_ERROR);
        }
        Page<TeamUserVO> teams = teamService.pageListTeams(searchTeamQuery,request );
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
    @PostMapping("quit")
    public BasicResponse<Boolean> quitTeam(@RequestBody QuitTeamReQuest quitTeamReQuest, HttpServletRequest request) {
        if (quitTeamReQuest==null)
            throw new BusinessException(errorCode.NULL_ERROR);
        if(request==null)
            throw new BusinessException(errorCode.NULL_ERROR);
        boolean result = teamService.quitTeam(quitTeamReQuest, request);
       if (!result)
       {
           throw new BusinessException(errorCode.SYSTEM_ERROR,"退出队伍失败");
       }
       return ResultUtils.success(result);
   }
}


