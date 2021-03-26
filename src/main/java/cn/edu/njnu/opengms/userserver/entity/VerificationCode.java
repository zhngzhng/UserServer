package cn.edu.njnu.opengms.userserver.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @ClassName VerificationCode
 * @Description 修改密码时的验证码
 * @Author zhngzhng
 * @Date 2021/3/25
 **/
@Data
@Document(collection = "verificationCode")
public class VerificationCode {
    @Id
    String id;

    String email;
    String code;
    Date date;
}
