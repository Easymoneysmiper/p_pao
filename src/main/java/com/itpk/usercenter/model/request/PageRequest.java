package com.itpk.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private int pageNum =1;

    private int pageSize= 10;

}
