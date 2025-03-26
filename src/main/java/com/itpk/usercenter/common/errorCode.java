package com.itpk.usercenter.common;
public enum errorCode {
    PARAMS_ERROR(40000,"请求参数错误",""),
    NULL_ERROR(40001,"请求参数为空",""),
    NOT_LOGIN(40100,"未登录",""),
    NO_AUTH(40101,"无权限",""),
    NOT_FOUND_ERROR(40400, "资源未找到", ""),
    SYSTEM_ERROR(50000,"系统内部异常","");

    private final int code;
    private final  String msg;
    private final String description;

    errorCode(int code, String msg, String description) {
        this.code = code;
        this.msg = msg;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getMsg() {
        return msg;
    }

    public int getCode() {
        return code;
    }
}
