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
    /*
    ture表示用户上传的内容
    false表示模型运行结果
     */
    private Boolean userUpload;
    /*
    首字母小写
    data, paper, document, model, image, video, others
     */
    private String type;
    /*
    首字母小写
    private & public
     */
    private String privacy;
    private String thumbnail;
    private String editToolInfo;
    /*
    使用包装类的话无默认值
    如果使用几大基本类型的话，默认值可能带来影响
    还是改成 String 好了，也好显示什么的
    */
    private long fileSize;
    //存储父资源的 uuid
    private String parent;
    private String md5;
    private String suffix;
    private String description;
    private String template;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
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
