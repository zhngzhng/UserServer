package cn.edu.njnu.opengms.userserver.entity;

import lombok.Getter;

@Getter
public enum UserTitle {
    //增加一个空的
    Professor("Professor."), Dr("Dr."), Mr("Mr"), Ms("Ms"), Miss("Miss"), Mrs("Mrs"), Mx("Mx"), Unfilled("");
    private String desc;

    UserTitle(String desc) {
        this.desc = desc;
    }
}
