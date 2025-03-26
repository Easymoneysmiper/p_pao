package com.itpk.usercenter.model.VO;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;

    /**
     * 昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;


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

    /**
     *  状态 0--正常
     */
    private Integer userStatus;

    /**
     *  状态 0--默认权限 1--管理员
     */
    private Integer userRole;
    /**
     * 创建时间
     */

    private Date createTime;


    /**
     * 标签json
     */
    private String tags;
}
