package cn.edu.njnu.opengms.userserver.dao;

import cn.edu.njnu.opengms.userserver.common.JsonResult;
import cn.edu.njnu.opengms.userserver.entity.ClientDetails;
import cn.edu.njnu.opengms.userserver.entity.User;
import cn.edu.njnu.opengms.userserver.entity.VerificationCode;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public interface UserDao {
    JsonResult createUser(User user);

    JsonResult findUserById(String id);


    JsonResult findUser(Query query);

    User searchUser(String id);

    //此处 id 可以是 userId 也可以是 email
    JsonResult updateInfo(String id, Update update);

    //======================验证码相关操作=============================
    JsonResult insetCode(VerificationCode code);

    JsonResult delCode(String email);

    JsonResult findCode(String email, String code);

    //============ 客户端相关操作 ===============
    JsonResult addClient(ClientDetails client);

    //用户入库函数
    Integer moveInDb(User user);

    //专用于用户入库
    User queryUserByEmail(String email);

}
