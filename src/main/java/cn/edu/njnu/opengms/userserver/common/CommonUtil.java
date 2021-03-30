package cn.edu.njnu.opengms.userserver.common;

import cn.edu.njnu.opengms.userserver.entity.Resource;
import cn.edu.njnu.opengms.userserver.entity.UserTitle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import sun.misc.BASE64Decoder;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * @ClassName CommonUtil
 * @Description 工具类
 * @Author zhngzhng
 * @Date 2021/3/25
 **/
@Service
public class CommonUtil {
    @Autowired
    JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    String mailAddress;


    //static 在程序编译时就分配区域

    /**
     * 发送邮件
     *
     * @param to
     * @param subject
     * @param content
     * @return
     */
    public JsonResult sendEmail(String to, String subject, String content) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            InternetAddress internetAddress = new InternetAddress(mailAddress, "OpenGMS Team", "UTF-8");
            helper.setFrom(internetAddress);
            helper.setText(to);
            helper.setCc(mailAddress);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(mimeMessage);
            return ResultUtils.success("Email sent successfully.");
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return ResultUtils.error("Email sending failed: " + e.toString());
        }
    }

    public Update setUpdate(Map<String, Object> userInfoMap) {
        Update update = new Update();
        try {
            for (Map.Entry<String, Object> entry : userInfoMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (key.equals("userId") || key.equals("email") || key.equals("createdTime") || key.equals("password")) {
                    continue;
                }
                switch (key) {
                    case "title":
                        UserTitle title = UserTitle.valueOf((String) value);
                        update.set(key, title);
                        break;
                    case "domain":
                    case "organizations":
                    case "loginIp":
                    case "resource":
                        update.set(key, value);
                        break;
                    //头像传输为 Base64,将 base64 转换为静态资源进行存储，然后将路径存储avatar字段中
                    case "avatar":
                        //新建文件夹
                        File path = new File(ResourceUtils.getURL("classpath:").getPath());
                        if (!path.exists()) {
                            path = new File("");
                        }
                        File avatarLocation = new File(path.getAbsolutePath(), "static/avatar/");
                        if (!avatarLocation.exists()) {
                            avatarLocation.mkdirs();
                        }
                        String avatarId = UUID.randomUUID().toString();
                        File avatar = new File(path.getAbsolutePath(), "static/avatar/" + avatarId + ".jpg");
                        byte[] avatarBytes = new BASE64Decoder().decodeBuffer((String) value);
                        for (int i = 0; i < avatarBytes.length; i++) {
                            if (avatarBytes[i] < 0) {
                                avatarBytes[i] += 256;
                            }
                        }
                        OutputStream out = new FileOutputStream(avatar);
                        out.write(avatarBytes);
                        out.flush();
                        out.close();

                        update.set(key, "/avatar/" + avatarId + ".jpg");
                        break;
                    default:
                        update.set(key, (String) value);
                }
            }

            return update;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 通过 paths 查找资源应该存在的位置
     * 找到父节点后直接在原文上修改得到内容
     * @param paths
     * @return
     */
    public ArrayList<Resource> findRes(ArrayList<String> paths) {
        for (int i = 0; i < paths.size(); i++) {

        }

        return null;
    }
}
