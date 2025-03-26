package com.itpk.usercenter.enums;

public enum StatusEnums {
    PUBLIC(0,"公开"),
    PRIVATE(1,"私有"),
    SECRET(2,"加密");

    private int value;
    private String desc;
    StatusEnums(int value,String desc)
    {
        this.value = value;
        this.desc = desc;
    }
    public static StatusEnums GetStatus(Integer value)
    {
        if(value == null)
            return null;
        StatusEnums[] values = StatusEnums.values();
        for(StatusEnums a:values)
        {
            if(a.getValue() == value)
                return a;
        }
        return null;
    }
    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
