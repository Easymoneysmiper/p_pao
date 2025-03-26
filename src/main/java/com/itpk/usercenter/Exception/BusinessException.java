package com.itpk.usercenter.Exception;

import com.itpk.usercenter.common.errorCode;

public class BusinessException extends RuntimeException {
    private final int code;
    private final String description;

    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(errorCode errorCode, String description) {
        this(errorCode.getMsg(), errorCode.getCode(), description);
    }

    public BusinessException(errorCode errorCode) {
        this(errorCode.getMsg(), errorCode.getCode(),errorCode.getDescription());
    }
    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
