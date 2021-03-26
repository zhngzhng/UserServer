package cn.edu.njnu.opengms.userserver.service.impl;

import cn.edu.njnu.opengms.userserver.common.CommonUtil;
import cn.edu.njnu.opengms.userserver.common.JsonResult;
import cn.edu.njnu.opengms.userserver.common.ResultUtils;
import cn.edu.njnu.opengms.userserver.config.Md5PasswordEncoder;
import cn.edu.njnu.opengms.userserver.dao.impl.UserDaoImpl;
import cn.edu.njnu.opengms.userserver.entity.ClientDetails;
import cn.edu.njnu.opengms.userserver.entity.Resource;
import cn.edu.njnu.opengms.userserver.entity.User;
import cn.edu.njnu.opengms.userserver.entity.VerificationCode;
import cn.edu.njnu.opengms.userserver.service.UserService;
import com.alibaba.fastjson.JSONObject;
import org.omg.PortableInterceptor.INACTIVE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;

/**
 * @ClassName UserServiceImpl
 * @Description 服务层接口实现
 * @Author zhngzhng
 * @Date 2021/3/24
 **/
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserDaoImpl userDao;

    @Autowired
    CommonUtil commonUtil;

    @Override
    public JsonResult getUserInfo(String email) {
        return userDao.findUserById(email);
    }

    @Override
    public JsonResult updateUserInfo(Principal principal, HashMap<String, Object> updateInfo) {
        String email = principal.getName();
        Update update = commonUtil.setUpdate(updateInfo);
        return userDao.updateInfo(email, update);
    }

    /**
     * 资源相关操作
     *
     * @param principal
     * @param resources
     * @return
     */
    @Override
    public JsonResult updateRes(Principal principal, ArrayList<Resource> resources, String operationType) {
        String email = principal.getName();
        User user = (User) userDao.findUserById(email).getData();
        ArrayList<Resource> userRes = user.getResource();
        if (userRes == null){
            userRes = new ArrayList<Resource>();
        }
        HashMap<String, Object> userInfoMap = new HashMap<>();
        switch (operationType) {
            case "push":
                for (int i=0; i<resources.size(); i++){
                    Resource resource = resources.get(i);
                    resource.getParent();
                    userRes.add(resource);
                }
                break;
            case "update":
                //具备批量修改功能
                ArrayList<Integer> delTag = new ArrayList<Integer>();
                for (int i=0; i < resources.size(); i++){
                    for (int j =0; j<userRes.size(); j++){
                        //传输过来的res id与 userRes相同则记录
                        if (resources.get(i).getUid().equals(userRes.get(j).getUid())){
                            delTag.add(j);
                            userRes.add(resources.get(i));
                            break;
                        }
                    }
                }
                //对所匹配的序号进行排序，后续删除的时候通过-1
                delTag.sort(Comparator.naturalOrder());
                for (int index = 0; index < delTag.size(); index++){
                    userRes.remove(delTag.get(index)-index);
                }
                break;
        }
        userInfoMap.put("resource", userRes);
        Update update = commonUtil.setUpdate(userInfoMap);
        //基本属于是只能成功
        userDao.updateInfo(email, update);
        return ResultUtils.success(userRes);
    }

    @Override
    public JsonResult delRes(Principal principal, String[] uids) {
        String email = principal.getName();
        User user =  (User)userDao.findUserById(email).getData();
        ArrayList<Resource> resource = user.getResource();
        ArrayList<Integer> delTag = new ArrayList<Integer>();
        for (int i=0; i < uids.length; i++){
            for (int j =0; j<resource.size(); j++){
                if (resource.get(j).getUid().equals(uids[i])){
                    delTag.add(j);
                    break;
                }
            }
        }
        //对所匹配的序号进行排序，后续删除的时候通过-1
        delTag.sort(Comparator.naturalOrder());
        for (int index = 0; index < delTag.size(); index++){
            resource.remove(delTag.get(index)-index);
        }
        HashMap<String, Object> userInfoMap = new HashMap<>();
        userInfoMap.put("resource", resource);
        Update update = commonUtil.setUpdate(userInfoMap);
        return userDao.updateInfo(email, update);
    }

    @Override
    public JsonResult addUser(User user) {
        user.setUserId(UUID.randomUUID().toString());
        return userDao.createUser(user);
    }

    @Override
    public JsonResult sendCodeEmail(String email) {
        //首先验证
        try {
            JsonResult userObj = userDao.findUserById(email);
            if (userObj.getCode() != 0) {
                return userObj;
            }
            User user = (User) userObj.getData();
            //生成验证码，并将其存入数据库
            Random random = new Random();
            String code = String.valueOf(random.nextInt(900000) + 100000);
            VerificationCode verificationCode = new VerificationCode();
            verificationCode.setEmail(email);
            verificationCode.setCode(code);
            verificationCode.setDate(new Date());
            userDao.insetCode(verificationCode);

            //将验证码发送给用户
            String subject = "OpenGMS Portal Verification Code";
            String content = "Hello " + user.getName() + ":<br/>" +
                    "You are trying to reset your password, the verification code is <b>" + code + "</b>. Please use this code in 30 minutes .<br/>" +
                    "Welcome to <a href='https://geomodeling.njnu.edu.cn' target='_blank'>OpenGMS</a> !";
            JsonResult sendEmailResult = commonUtil.sendEmail(email, subject, content);
            return sendEmailResult;

        } catch (Exception e) {
            //发送失败，回溯上一步操作
            userDao.delCode(email);
            return ResultUtils.error(e.toString());
        }
    }

    /**
     * 难道还要为记得密码的情况多写一个方法？
     *
     * @param email
     * @param code
     * @param newPwd
     * @return
     */
    @Override
    public JsonResult resetPwd(String email, String code, String newPwd) {
        //验证邮箱是否输入正确
        JsonResult userResult = userDao.findUserById(email);
        if (userResult.getCode() != 0) {
            return userResult;
        }
        User user = (User) userResult.getData();
        //验证验证码是否正确
        JsonResult jsonResult = userDao.findCode(email, code);
        if (jsonResult.getData() == null) {
            return ResultUtils.error("Verification code is wrong.");
        }
        VerificationCode verificationCode = (VerificationCode) jsonResult.getData();
        long between = new Date().getTime() - verificationCode.getDate().getTime();
        long day = between / (24 * 60 * 60 * 1000);
        long min = (between / (60 * 1000) - day * 24 * 60);
        //验证验证码是否过期
        if (min > 30) {
            return ResultUtils.error("The verification code is invalid");
        }
        HashMap<String, Object> userInfo = new HashMap<>();
        // String encodePwd = DigestUtils.sha256Hex(DigestUtils.md5Hex(newPwd.getBytes()));
        userInfo.put("password", newPwd);
        Update update = commonUtil.setUpdate(userInfo);
        JsonResult resetPwdResult = userDao.updateInfo(email, update);
        if (resetPwdResult.getCode() != 0) {
            return resetPwdResult;
        }
        //将验证码删除
        userDao.delCode(email);
        return resetPwdResult;
    }

    @Override
    public JsonResult newPwd(Principal principal, String oldPwd, String newPwd) {
        //从 token 中获取的信息，肯定是正确
        String email = principal.getName();
        User user = (User) userDao.findUserById(email).getData();
        String encodePwd = user.getPassword();
        if (!oldPwd.equals(encodePwd)) {
            return ResultUtils.error("Old password is not correct!");
        }
        //密码匹配情况
        HashMap<String, Object> userInfoMap = new HashMap<>();
        userInfoMap.put("password", newPwd);
        Update update = commonUtil.setUpdate(userInfoMap);
        return userDao.updateInfo(email, update);
    }

    @Override
    public JsonResult addClientService(ClientDetails client) {
        return userDao.addClient(client);
    }
}
