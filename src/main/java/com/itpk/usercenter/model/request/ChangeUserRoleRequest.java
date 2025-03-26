package com.itpk.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChangeUserRoleRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long userId;
    private int userRole;
}
