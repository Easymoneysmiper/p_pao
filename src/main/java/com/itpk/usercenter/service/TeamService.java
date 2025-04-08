package com.itpk.usercenter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itpk.usercenter.model.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itpk.usercenter.model.User;
import com.itpk.usercenter.model.VO.TeamUserVO;
import com.itpk.usercenter.model.dto.SearchTeamQuery;
import com.itpk.usercenter.model.request.DeleteRequest;
import com.itpk.usercenter.model.request.JoinTeamRequest;
import com.itpk.usercenter.model.request.QuitTeamReQuest;
import com.itpk.usercenter.model.request.UpdateTeamRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author easymoneysniper
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2025-03-23 15:36:52
*/
public interface TeamService extends IService<Team> {
    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
 Long addTeam(Team team, User loginUser);

    /**
     * 分页查询队伍
     * @param searchTeamQuery
     * @return
     */
    List<TeamUserVO> listTeams(SearchTeamQuery searchTeamQuery, HttpServletRequest httpServletRequest);

    /**
     * 修改队伍信息
     * @param updateTeamRequest
     * @param request
     * @return
     */
    boolean updateTeam(UpdateTeamRequest updateTeamRequest, HttpServletRequest request);

    /**
     * 加入队伍
     * @param joinTeamRequest
     * @param request
     * @return
     */
    boolean join(JoinTeamRequest joinTeamRequest, HttpServletRequest request);

    /**
     * 退出队伍
     *
     * @param quitTeamReQuest
     * @param request
     * @return
     */
    boolean quitTeam(QuitTeamReQuest quitTeamReQuest, HttpServletRequest request);

   /**
    * 解散队伍
    * @param deleteRequest
    * @param request
    * @return
    */
    boolean deleteTeam(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 分页展示队伍
     * @param searchTeamQuery
     * @param request
     * @return
     */
    Page<TeamUserVO> pageListTeams(SearchTeamQuery searchTeamQuery, HttpServletRequest request);
}
