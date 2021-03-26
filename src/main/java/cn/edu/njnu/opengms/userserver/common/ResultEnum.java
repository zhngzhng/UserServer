package cn.edu.njnu.opengms.userserver.common;

import lombok.AllArgsConstructor;
import lombok.Data;

public enum ResultEnum {
    SUCCESS(0, "suc"),
    NO_OBJECT(-1, "NO Object"),
    EXISTS_OBJECT(-3, "Object have exists"),
    ERROR(-2,"Fail");
    private Integer code;
    private String msg;

    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
