package cn.edu.njnu.opengms.userserver.entity;

import lombok.Data;

import java.util.ArrayList;

/**
 * @ClassName UserPortal
 * @Description 门户专用
 * @Author zhngzhng
 * @Date 2021/7/28
 **/
@Data
public class UserPortal {
    private String userId;
    private String email;
    private String name;

    private String phone;
    private UserTitle title;
    private String country;
    private String state;
    private String city;
    private String homepage;
    private ArrayList<String> organizations;
    //存静态文件路径，前端传送 string 的base64过来
    private String avatar;
    private String introduction;
    private ArrayList<String> domain;
}
