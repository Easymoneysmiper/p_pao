package com.itpk.usercenter.mapper;

import com.itpk.usercenter.model.Team;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author easymoneysniper
* @description 针对表【team(队伍)】的数据库操作Mapper
* @createDate 2025-03-23 15:36:52
* @Entity com.itpk.usercenter.model.Team
*/
public interface TeamMapper extends BaseMapper<Team> {

    Team selectTeamForUpdate(Long teamId);
}




