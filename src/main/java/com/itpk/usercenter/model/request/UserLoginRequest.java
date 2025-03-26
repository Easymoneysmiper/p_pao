package com.itpk.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求头
 */
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userAccount;

    private String userPassword;


}
