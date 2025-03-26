package com.itpk.usercenter.common;

public class ResultUtils {
   public static <T>BasicResponse<T> success(T data)
    {
        return new BasicResponse<>(0,data,"success","");
    }

    /**
     * 失败
     * @param
     * @return
     */
   public static BasicResponse  error(errorCode errorCode)
   {
       return new BasicResponse<>(errorCode);
   }
    public static BasicResponse  error(errorCode errorCode,String description)
    {
        return new BasicResponse<>(errorCode,description);
    }
    public static BasicResponse  error(errorCode errorCode,String msg,String description)
    {
        return new BasicResponse(errorCode.getCode(),null,msg,description);
    }
    public static BasicResponse  error(int code,String msg,String description)
    {
        return new BasicResponse(code,msg,description);
    }
}