package cn.edu.njnu.opengms.userserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String userId;
    private String email;
    private String password;
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
    //限制容量为 5G = 5368709120b
    private long capacity;
    private long usedCapacity;
    private String introduction;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date createdTime;
    private ArrayList<String> loginIp;
    private ArrayList<String> domain;
    private ArrayList<Resource> resource;

}
