package com.itpk.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class JoinTeamRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 队伍Id
     */
    private Long teamId;
    /**
     * 队伍密码
     */
    private String password;

}
