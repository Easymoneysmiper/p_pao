package com.itpk.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itpk.usercenter.Exception.BusinessException;
import com.itpk.usercenter.common.errorCode;
import com.itpk.usercenter.model.UserTeam;
import com.itpk.usercenter.service.UserTeamService;
import com.itpk.usercenter.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author easymoneysniper
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2025-03-23 15:39:10
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{
    public Integer countTeamMembers(Long teamId) {
        if (teamId == null) {
            throw new BusinessException(errorCode.NULL_ERROR);
        }
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId)
                .eq("isDelete", 0);
        return this.count(queryWrapper);
    }

    public Integer countUserTeams(Long userId) {
        if (userId == null) {
            throw new BusinessException(errorCode.NULL_ERROR);
        }
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                .eq("isDelete", 0);
        return this.count(queryWrapper);
    }

    public boolean isUserInTeam(Long userId, Long teamId) {
        if (userId == null || teamId == null) {
            throw new BusinessException(errorCode.NULL_ERROR);
        }
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                .eq("teamId", teamId)
                .eq("isDelete", 0);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public boolean removeUserTeam(Long userId, Long teamId) {
        if (userId == null || teamId == null) {
            throw new BusinessException(errorCode.NULL_ERROR);
        }
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                .eq("teamId", teamId)
                .eq("isDelete", 0);
        return this.remove(queryWrapper);
    }
}




