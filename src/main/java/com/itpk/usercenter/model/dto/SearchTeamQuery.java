package com.itpk.usercenter.model.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import com.itpk.usercenter.model.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.Nullable;

import java.util.Date;
@EqualsAndHashCode(callSuper = true)
@Data
public class SearchTeamQuery extends PageRequest {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
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
     * 用户 id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 -加密
     */
    @Nullable
    private Integer status;

    /**
     *  创建时间
     */
    private Date createTime;

}
