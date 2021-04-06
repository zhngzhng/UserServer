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
        JsonResult jsonResult = userDao.findUserById(email);
        if (jsonResult.getCode() != 0){
            return jsonResult;
        }
        User user = (User) jsonResult.getData();
        user.setPassword("");
        return ResultUtils.success(user);
    }

    @Override
    public JsonResult updateUserInfo(Principal principal, HashMap<String, Object> updateInfo) {
        String email = principal.getName();
        Update update = commonUtil.setUpdate(updateInfo);
        return userDao.updateInfo(email, update);
    }

    // /**
    //  * 资源相关操作
    //  *
    //  * @param principal
    //  * @param resources
    //  * @return
    //  */
    // @Override
    // public JsonResult updateRes(Principal principal, ArrayList<Resource> resources, String operationType) {
    //     String email = principal.getName();
    //     User user = (User) userDao.findUserById(email).getData();
    //     ArrayList<Resource> userRes = user.getResource();
    //     if (userRes == null) {
    //         userRes = new ArrayList<Resource>();
    //     }
    //     HashMap<String, Object> userInfoMap = new HashMap<>();
    //     switch (operationType) {
    //         case "push":
    //             for (int i = 0; i < resources.size(); i++) {
    //                 Resource resource = resources.get(i);
    //                 userRes.add(resource);
    //             }
    //             break;
    //         case "update":
    //             //具备批量修改功能
    //             ArrayList<Integer> delTag = new ArrayList<Integer>();
    //             for (int i = 0; i < resources.size(); i++) {
    //                 for (int j = 0; j < userRes.size(); j++) {
    //                     //传输过来的res id与 userRes相同则记录
    //                     if (resources.get(i).getUid().equals(userRes.get(j).getUid())) {
    //                         delTag.add(j);
    //                         userRes.add(resources.get(i));
    //                         break;
    //                     }
    //                 }
    //             }
    //             //对所匹配的序号进行排序，后续删除的时候通过-1
    //             delTag.sort(Comparator.naturalOrder());
    //             for (int index = 0; index < delTag.size(); index++) {
    //                 userRes.remove(delTag.get(index) - index);
    //             }
    //             break;
    //     }
    //     userInfoMap.put("resource", userRes);
    //     Update update = commonUtil.setUpdate(userInfoMap);
    //     //基本属于是只能成功
    //     userDao.updateInfo(email, update);
    //     return ResultUtils.success(userRes);
    // }


    @Override
    public JsonResult addUser(User user) {
        user.setUserId(UUID.randomUUID().toString());
        if (user.getResource() == null) {
            String defaultFolderUid = UUID.randomUUID().toString();
            Resource defaultFolder = new Resource(defaultFolderUid, "My Data", true, "public", "0", new ArrayList<Resource>());
            ArrayList<Resource> resources = new ArrayList<>();
            resources.add(defaultFolder);
            user.setResource(resources);
        }
        user.setCreatedTime(new Date());
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
        // // String encodePwd = DigestUtils.sha256Hex(DigestUtils.md5Hex(newPwd.getBytes()));
        Update update = new Update();
        update.set("password", newPwd);
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
        Update update = new Update();
        update.set("password", newPwd);
        return userDao.updateInfo(email, update);
    }

    @Override
    public JsonResult addClientService(ClientDetails client) {
        return userDao.addClient(client);
    }


    /**
     * 添加文件夹
     * 文件夹也应该有私有或公有
     * uid, date等内容都在各平台生成
     * 文件夹在后台构建
     *
     * @param principal
     * @param upRes
     * @param paths
     * @return
     */
    @Override
    public JsonResult addRes(Principal principal, Resource upRes, ArrayList<String> paths) {
        String email = principal.getName();
        User user = (User) userDao.findUserById(email).getData();
        ArrayList<Resource> userRes = user.getResource();
        //初始化用于更新的内容 map
        HashMap<String, Object> updateInfoMap = new HashMap<>();
        // 为根节点情况，直接 push 即可,此情况作为递归出口了
        // if (paths.size() == 0) {
        //     userRes.add(upRes);
        //     updateInfoMap.put("resource", userRes);
        //     Update update = commonUtil.setUpdate(updateInfoMap);
        //     return userDao.updateInfo(email, update);
        // }
        //不是根节点情况，需要按深度遍历获得父节点，然后将其 push 到父节点的 children 节点中
        upRes.setChildren(new ArrayList<Resource>());
        ArrayList<Resource> uploadedRes = aRes(paths, userRes, upRes, "0");
        updateInfoMap.put("resource", uploadedRes);
        Update update = commonUtil.setUpdate(updateInfoMap);
        return userDao.updateInfo(email, update);
    }

    //遍历到父节点，然后将资源放进去
    public ArrayList<Resource> aRes(ArrayList<String> paths, ArrayList<Resource> userRes, Resource upRes, String parent) {
        // ”0“ 为根目录的指示，这也是为特殊情况准备
        if (paths.size() == 0 || paths.get(0).equals("0")) {
            upRes.setParent(parent);
            userRes.add(upRes);
        } else {
            //取出最后一个元素，paths 中是由内到外的
            String path = paths.remove(paths.size() - 1);
            for (int i = 0; i < userRes.size(); i++) {
                Resource resource = userRes.get(i);
                if (resource.getUid().equals(path)) {
                    //递归出口条件，可将递归出口条件同根目录合并，其实根目录也就是为 0 的时候。
                    // if (paths.size() == 0) {
                    //     ArrayList<Resource> updateRes = new ArrayList<>();
                    //     updateRes.add(upRes);
                    //     resource.setChildren(updateRes);
                    // }
                    resource.setChildren(aRes(paths, resource.getChildren(), upRes, path));
                    userRes.set(i, resource);
                    break;
                }
            }
        }
        return userRes;
    }

    @Override
    public JsonResult delRes(Principal principal, String uid, ArrayList<String> paths) {
        String email = principal.getName();
        User user = (User) userDao.findUserById(email).getData();
        ArrayList<Resource> userRes = user.getResource();
        ArrayList<Resource> deletedRes = dRes(userRes, uid, paths);
        HashMap<String, Object> delInfoMap = new HashMap<>();
        delInfoMap.put("resource", deletedRes);
        Update update = commonUtil.setUpdate(delInfoMap);
        return userDao.updateInfo(email, update);
    }

    public ArrayList<Resource> dRes(ArrayList<Resource> userRes, String uid, ArrayList<String> paths) {
        //根目录或遍历到父节点，此为结束条件
        if (paths.size() == 0 || paths.get(0).equals("0")) {
            //此时就只有一层
            for (int j = 0; j < userRes.size(); j++) {
                Resource resource = userRes.get(j);
                if (resource.getUid().equals(uid)) {
                    userRes.remove(j);
                }
            }
        } else {
            String path = paths.remove(paths.size() - 1);
            for (int i = 0; i < userRes.size(); i++) {
                Resource resource = userRes.get(i);
                if (resource.getUid().equals(path)) {
                    dRes(resource.getChildren(), uid, paths);
                    userRes.set(i, resource);
                }
            }
        }
        return userRes;
    }

    /**
     * 更新内容
     *
     * @param principal
     * @param updateRes
     * @param paths
     * @return
     */
    @Override
    public JsonResult putRes(Principal principal, Resource updateRes, ArrayList<String> paths) {
        String email = principal.getName();
        User user = (User) userDao.findUserById(email).getData();
        ArrayList<Resource> userRes = user.getResource();
        ArrayList<Resource> resources = pRes(userRes, updateRes, paths);
        HashMap<String, Object> putInfoMap = new HashMap<>();
        putInfoMap.put("resource", resources);
        Update update = commonUtil.setUpdate(putInfoMap);
        return userDao.updateInfo(email, update);
    }

    public ArrayList<Resource> pRes(ArrayList<Resource> userRes, Resource putRes, ArrayList<String> paths) {
        if (paths.size() == 0 || paths.get(0).equals("0")) {
            String putResUid = putRes.getUid();
            for (int j = 0; j < userRes.size(); j++) {
                Resource resource = userRes.get(j);
                if (resource.getUid().equals(putResUid)) {
                    //将原来位置内容替换为新的内容
                    userRes.remove(j);
                    userRes.add(j, putRes);
                }
            }
        } else {
            String path = paths.remove(paths.size() - 1);
            for (int i = 0; i < userRes.size(); i++) {
                Resource resource = userRes.get(i);
                if (resource.getUid().equals(path)) {
                    pRes(resource.getChildren(), putRes, paths);
                    userRes.set(i, resource);
                }
            }
        }
        return userRes;
    }

    @Override
    public JsonResult getAllFileService(Principal principal) {
        String email = principal.getName();
        User user = (User) userDao.findUserById(email).getData();
        ArrayList<Resource> resource = user.getResource();
        if (resource == null) {
            resource = new ArrayList<Resource>();
        }
        return ResultUtils.success(resource);
    }

    @Override
    public JsonResult changeFolder(Principal principal, Resource upRes, ArrayList<String> oldPaths, ArrayList<String> newPaths) {
        String resUid = upRes.getUid();
        String email = principal.getName();
        User user = (User) userDao.findUserById(email).getData();
        ArrayList<Resource> userRes = user.getResource();
        ArrayList<Resource> deletedRes = dRes(userRes, resUid, oldPaths);
        ArrayList<Resource> addedRes = aRes(newPaths, deletedRes, upRes, "0");
        HashMap<String, Object> changedInfoMap = new HashMap<>();
        changedInfoMap.put("resource", addedRes);
        Update update = commonUtil.setUpdate(changedInfoMap);
        return userDao.updateInfo(email, update);
    }

    @Override
    public JsonResult getFileByPath(Principal principal, ArrayList<String> paths) {
        String email = principal.getName();
        User user = (User) userDao.findUserById(email).getData();
        ArrayList<Resource> userRes = user.getResource();
        ArrayList<Resource> resources = gFileByPath(userRes, paths);
        return ResultUtils.success(resources);
    }

    public ArrayList<Resource> gFileByPath(ArrayList<Resource> userRes, ArrayList<String> paths) {
        ArrayList<Resource> folderList = new ArrayList<>();
        if (paths.size() == 0 || paths.get(0).equals("0")) {
            return userRes;
        } else {
            String path = paths.remove(paths.size() - 1);
            for (int i = 0; i < userRes.size(); i++) {
                Resource resource = userRes.get(i);
                if (resource.getUid().equals(path)) {
                    folderList =  gFileByPath(resource.getChildren(), paths);
                }
            }
        }
        return folderList;
    }
}
