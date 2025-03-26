package com.itpk.usercenter.Exception;

import com.itpk.usercenter.common.BasicResponse;
import com.itpk.usercenter.common.ResultUtils;
import com.itpk.usercenter.common.errorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public BasicResponse handleBusinessException(BusinessException e) {
        log.error("BusinessException"+e.getMessage(),e.getDescription());
         return ResultUtils.error(e.getCode(),e.getMessage(),e.getDescription());
    }
    @ExceptionHandler(RuntimeException.class)
    public BasicResponse handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException",e);
        return ResultUtils.error(errorCode.SYSTEM_ERROR,errorCode.SYSTEM_ERROR.getMsg(),"");
    }
}
