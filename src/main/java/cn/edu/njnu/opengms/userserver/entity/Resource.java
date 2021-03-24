package cn.edu.njnu.opengms.userserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Resource {
    private String uid;
    private String name;
    //数据在数据容器中的链接
    private String address;
    private boolean isFolder;
    private String type;
    private String privacy;
    private String thumbnail;
    private String editToolInfo;
    private long fileSize;
    private String parent;
    private String md5;
    private String suffix;
    private String description;
    private String template;
    private String uploadTime;
    private ArrayList<Resource> children;
}
