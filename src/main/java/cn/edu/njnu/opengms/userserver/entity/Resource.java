package cn.edu.njnu.opengms.userserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Resource {
    @Id
    private String uid;
    private String name;
    //数据在数据容器中的链接
    private String address;
    private Boolean folder;
    //用户上传或者模型运行结果
    private Boolean userUpload;
    private String type;
    private String privacy;
    private String thumbnail;
    private String editToolInfo;
    private long fileSize;
    //存储父资源的 uuid
    private String parent;
    private String md5;
    private String suffix;
    private String description;
    private String template;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private Date uploadTime;
    private ArrayList<Resource> children;

    public Resource(String uid, String name, Boolean folder, String privacy, String parent, ArrayList<Resource> children) {
        this.uid = uid;
        this.name = name;
        this.folder = folder;
        this.privacy = privacy;
        this.parent = parent;
        this.uploadTime = new Date();
        this.children = children;
    }

}
