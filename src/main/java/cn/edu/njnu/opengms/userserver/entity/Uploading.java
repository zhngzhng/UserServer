package cn.edu.njnu.opengms.userserver.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @ClassName Uploading
 * @Description Todo
 * @Author zhngzhng
 * @Date 2021/8/4
 **/
@Data
@Document(collection = "uploading")
public class Uploading {
    @Id
    String id;
    String email;
    long fileSize;
    Date date;
}
