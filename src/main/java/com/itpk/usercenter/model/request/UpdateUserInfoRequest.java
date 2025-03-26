package com.itpk.usercenter.model.request;

import lombok.Data;

@Data
public class UpdateUserInfoRequest {
    /**
     * 用户名
     */
    private String username;
    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 星球编号
     */
    private  String planetCode;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;
}
