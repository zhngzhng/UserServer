package cn.edu.njnu.opengms.userserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {
    private String email;
    private String userId;
    private String password;
    private String name;
    private String phone;
    private UserTitle title;
    private String country;
    private String state;
    private String city;
    private String homePage;
    private ArrayList<String> organizations;
    private String avatar;
    private String introduction;
    private String createdTime;
    private ArrayList<String> loginIp;
    private ArrayList<String> domain;
    private ArrayList<Resource> resource;
}
