package com.itpk.usercenter.service;

import com.itpk.usercenter.model.UserTeam;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author easymoneysniper
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service
* @createDate 2025-03-23 15:39:10
*/
public interface UserTeamService extends IService<UserTeam> {
    /**
     * 查询队伍人数
     * @param teamId
     * @return
     */
    Integer countTeamMembers(Long teamId);

    /**
     * 查询用户已加入队伍数目
     * @param userId
     * @return
     */
    Integer countUserTeams(Long userId);

    /**
     * 用户是否在队伍中
     * @param userId
     * @param teamId
     * @return
     */
    boolean isUserInTeam(Long userId, Long teamId);
}
