package cn.edu.njnu.opengms.userserver.dao.impl;

import cn.edu.njnu.opengms.userserver.common.JsonResult;
import cn.edu.njnu.opengms.userserver.common.ResultEnum;
import cn.edu.njnu.opengms.userserver.common.ResultUtils;
import cn.edu.njnu.opengms.userserver.dao.UserDao;
import cn.edu.njnu.opengms.userserver.entity.ClientDetails;
import cn.edu.njnu.opengms.userserver.entity.User;
import cn.edu.njnu.opengms.userserver.entity.VerificationCode;
import com.mongodb.MongoException;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName UserDaoImpl
 * @Description 用户相关接口实现
 * @Author zhngzhng
 * @Date 2021/3/24
 **/
@Repository
public class UserDaoImpl implements UserDao {
    @Autowired
    MongoTemplate mongoTemplate;

    private final String COLLECTION_NAME = "users";

    @Override
    public JsonResult createUser(User user) {
        try {
            String email = user.getEmail();
            JsonResult userObj = findUserById(email);
            if (userObj.getCode() == 0) {
                return ResultUtils.objExists("Email has existed.");
            }
            User register = mongoTemplate.save(user, COLLECTION_NAME);
            register.setPassword("");
            return ResultUtils.success(register);
        } catch (MongoException e) {
            return ResultUtils.error(e.toString());
        }
    }

    @Override
    public JsonResult findUser(Query query) {
        try {
            List<User> users = mongoTemplate.find(query, User.class);
            if (users.size() == 0) {
                return ResultUtils.noObject();
            }
            return ResultUtils.success(users);
        } catch (MongoException e) {
            return ResultUtils.error("Query failed.");
        }
    }

    /**
     * Id 可以是 email 也可以是 userId
     *
     * @param id
     * @return
     */
    @Override
    public JsonResult findUserById(String id) {
        try {
            Criteria criteria = new Criteria().orOperator(Criteria.where("userId").is(id),
                    Criteria.where("email").is(id));
            Query query = new Query(criteria);
            User user = mongoTemplate.findOne(query, User.class, COLLECTION_NAME);
            if (user == null) {
                return ResultUtils.noObject();
            }
            user.setPassword("");
            return ResultUtils.success(user);
        } catch (MongoException e) {
            return ResultUtils.error(e.toString());
        }
    }

    @Override
    public JsonResult updateInfo(String id, Update update) {
        Criteria criteria = new Criteria().orOperator(Criteria.where("userId").is(id),
                Criteria.where("email").is(id));
        Query query = new Query(criteria);
        JsonResult userObj = findUser(query);
        if (userObj.getCode() == -1) {
            return ResultUtils.noObject();
        } else if (userObj.getCode() == -2) {
            return ResultUtils.error(userObj.getMsg());
        }
        try {
            if (mongoTemplate.updateFirst(query, update, User.class, COLLECTION_NAME).getModifiedCount() == 1) {
                User user = mongoTemplate.findOne(query, User.class, COLLECTION_NAME);
                user.setPassword("");
                return ResultUtils.success(user);
            }
            return ResultUtils.error("Update user information failed.");
        } catch (MongoException e) {
            return ResultUtils.error(e.toString());
        }
    }


    //验证码相关内容

    @Override
    public JsonResult insetCode(VerificationCode code) {
        try {
            return ResultUtils.success(mongoTemplate.insert(code));
        } catch (MongoException e) {
            return ResultUtils.error(e.toString());
        }
    }


    @Override
    public JsonResult delCode(String email) {
        Query query = new Query(Criteria.where("email").is(email));
        try {
            DeleteResult deleteResult = mongoTemplate.remove(query, "verificationCode");
            if (deleteResult.getDeletedCount() > 0) {
                return ResultUtils.success();
            } else {
                return ResultUtils.noObject();
            }

        } catch (MongoException e) {
            return ResultUtils.error(e.toString());
        }
    }

    @Override
    public JsonResult findCode(String email, String code) {
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("email").is(email),
                Criteria.where("code").is(code)
        );
        Query query = new Query(criteria);
        try {
            VerificationCode verificationCode = mongoTemplate.findOne(query, VerificationCode.class);
            if (verificationCode == null) {
                return ResultUtils.noObject();
            }
            return ResultUtils.success(verificationCode);
        } catch (MongoException e) {
            return ResultUtils.error(e.toString());
        }
    }

    @Override
    public JsonResult addClient(ClientDetails client) {
        return ResultUtils.success(mongoTemplate.save(client));
    }
}
