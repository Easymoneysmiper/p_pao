package com.itpk.usercenter.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 队伍用户信息封装类
 */
@Data
public class TeamUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 -加密
     */
    private Integer status;


    /**
     *  创建时间
     */
    private Date createTime;
    /**
     *
     */
    private List<UserVO> users;

}
