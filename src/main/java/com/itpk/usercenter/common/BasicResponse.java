package com.itpk.usercenter.common;

import lombok.Data;

import java.io.Serializable;
@Data
public class BasicResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private int code;
    private T data;
    private String msg;
    private String description;
    public BasicResponse(int code, T data, String msg,String description) {
        this.msg = msg;
        this.code = code;
        this.data = data;
        this.description = description;
    }
    public BasicResponse(int code,String msg,String description) {
        this.msg = msg;
        this.code = code;
        this.data = null;
        this.description = description;
    }
    public BasicResponse(int code, T data)
    {
        this(code, data, "","");
    }
    public BasicResponse(errorCode errorCode)
    {
        this.code = errorCode.getCode();
        this.msg = errorCode.getMsg();
        this.description = errorCode.getDescription();
        this.data = null;
    }
    public BasicResponse(errorCode errorCode, String description)
    {
        this.code = errorCode.getCode();
        this.msg = errorCode.getMsg();
        this.description = description;
        this.data = null;
    }
}
