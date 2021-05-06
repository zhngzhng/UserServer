package cn.edu.njnu.opengms.userserver.service.impl;

import cn.edu.njnu.opengms.userserver.common.CommonUtil;
import cn.edu.njnu.opengms.userserver.common.JsonResult;
import cn.edu.njnu.opengms.userserver.common.ResultUtils;
import cn.edu.njnu.opengms.userserver.config.Md5PasswordEncoder;
import cn.edu.njnu.opengms.userserver.dao.impl.UserDaoImpl;
import cn.edu.njnu.opengms.userserver.entity.*;
import cn.edu.njnu.opengms.userserver.service.UserService;
import com.alibaba.fastjson.JSONObject;
import org.omg.PortableInterceptor.INACTIVE;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.BASE64Decoder;

import java.beans.PropertyDescriptor;
import java.io.*;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public JsonResult loginService(String email, String loginIp) {
        User user = userDao.searchUser(email);
        ArrayList<String> loginIp1 = user.getLoginIp();
        //初始化 loginIp,
        if (loginIp1 == null) {
            loginIp1 = new ArrayList<String>();
        }
        UserTo userTo = new UserTo();
        String lastLoginIp = "";
        /*
        >=1则之前登录过，比对此次登录与上次登录是否地址一样
        如果一样的话则不存，不同的话则存入
         */
        if (loginIp1.size() >= 1) {
            lastLoginIp = loginIp1.get(loginIp1.size() - 1);
            //loginIp一致，直接返回
            if (loginIp.equals(lastLoginIp)) {
                BeanUtils.copyProperties(user, userTo);
                return ResultUtils.success(userTo);
            }
        }
        //不一致，存入，采用更新
        loginIp1.add(loginIp);
        HashMap<String, Object> updateInfo = new HashMap<>();
        updateInfo.put("loginIp", loginIp1);
        Update update = commonUtil.setUpdate(updateInfo);
        userDao.updateInfo(email, update);
        BeanUtils.copyProperties(user, userTo);
        return ResultUtils.success(user);
    }

    @Override
    public JsonResult getUserInfo(String email) {
        JsonResult jsonResult = userDao.findUserById(email);
        if (jsonResult.getCode() != 0) {
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
        user.setCreatedTime(new Date());
        //初始化一个文件夹给他
        ArrayList<Resource> res = new ArrayList<Resource>();
        Resource defaultFolder = new Resource(UUID.randomUUID().toString(), "My Data", true, "public", "0", new ArrayList<Resource>());
        res.add(defaultFolder);
        user.setResource(res);
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
        if (upRes.getChildren() == null || upRes.getChildren().size() == 0) {
            upRes.setChildren(new ArrayList<Resource>());
        }
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
                    //支持增量更新
                    userRes.remove(j);
                    String[] nullPropertyNames = getNullPropertyNames(putRes);
                    BeanUtils.copyProperties(putRes, resource, nullPropertyNames);
                    userRes.add(j, resource);
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
    public JsonResult getAllResService(Principal principal) {
        String email = principal.getName();
        User user = (User) userDao.findUserById(email).getData();
        ArrayList<Resource> resource = user.getResource();
        if (resource == null || resource.size() == 0) {
            String defaultFolderUid = UUID.randomUUID().toString();
            resource = addDefaultFolder(email, defaultFolderUid);
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
                    folderList = gFileByPath(resource.getChildren(), paths);
                }
            }
        }
        return folderList;
    }

    @Override
    public JsonResult getUserBase(Principal principal) {
        String email = principal.getName();
        User user = (User) userDao.findUserById(email).getData();
        user.setResource(null);
        return ResultUtils.success(user);
    }


    //添加默认的文件夹
    private ArrayList<Resource> addDefaultFolder(String email, String uid) {
        ArrayList<Resource> resource = new ArrayList<Resource>();
        Resource defaultFolder = new Resource(uid, "My Data", true, "public", "0", new ArrayList<Resource>());
        resource.add(defaultFolder);
        HashMap<String, Object> putInfoMap = new HashMap<>();
        putInfoMap.put("resource", resource);
        Update update = commonUtil.setUpdate(putInfoMap);
        userDao.updateInfo(email, update);
        return resource;
    }


    @Override
    public JsonResult getAllFolder(Principal principal) {
        String email = principal.getName();
        User user = (User) userDao.findUserById(email).getData();
        ArrayList<Resource> resource = user.getResource();
        //如果没有内容，则新建一个给它
        //递归所有文件夹，去掉资源，只返回文件夹内容
        if (resource == null || resource.size() == 0) {
            ArrayList<JSONObject> allFolder = new ArrayList<>();
            JSONObject jsonObject = new JSONObject();
            //直接这样显然不行呀
            String defaultFolderId = UUID.randomUUID().toString();
            //新建默认的文件夹
            addDefaultFolder(email, defaultFolderId);
            jsonObject.put("uid", defaultFolderId);
            jsonObject.put("name", "My Data");
            jsonObject.put("folder", true);
            jsonObject.put("parent", "0");
            jsonObject.put("children", null);
            allFolder.add(jsonObject);
            return ResultUtils.success(allFolder);
        } else {
            ArrayList<JSONObject> allFolder = aFolder(resource);
            return ResultUtils.success(allFolder);
        }
    }

    public ArrayList<JSONObject> aFolder(ArrayList<Resource> userRes) {
        ArrayList<JSONObject> parent = new ArrayList<>();
        //不修改原内容，将捕获到的文件夹内容重写塞到一个新的ArrayList<Resource> 里面
        for (int i = 0; i < userRes.size(); i++) {
            Resource resource = userRes.get(i);
            if (resource.getFolder()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("uid", resource.getUid());
                jsonObject.put("name", resource.getName());
                jsonObject.put("folder", resource.getFolder());
                jsonObject.put("parent", resource.getParent());
                jsonObject.put("children", aFolder(resource.getChildren()));
                parent.add(jsonObject);
            }
        }
        return parent;
    }

    @Override
    public JsonResult delByUid(Principal principal, String uid) {
        String email = principal.getName();
        User user = (User) userDao.findUserById(email).getData();
        ArrayList<Resource> userRes = user.getResource();
        ArrayList<Resource> deletedRes = dByUid(userRes, uid);
        HashMap<String, Object> putInfoMap = new HashMap<>();
        putInfoMap.put("resource", deletedRes);
        Update update = commonUtil.setUpdate(putInfoMap);
        return userDao.updateInfo(email, update);
    }

    private ArrayList<Resource> dByUid(ArrayList<Resource> uRes, String uid) {
        for (int i = 0; i < uRes.size(); i++) {
            Resource resource = uRes.get(i);
            if (resource.getUid().equals(uid)) {
                uRes.remove(i);
                break;
            } else {
                if (resource.getFolder()) {
                    dByUid(resource.getChildren(), uid);
                } else {
                    continue;
                }
            }
        }
        return uRes;
    }


    @Override
    public JsonResult putByUid(Principal principal, Resource res) {
        String email = principal.getName();
        User user = (User) userDao.findUserById(email).getData();
        ArrayList<Resource> userRes = user.getResource();
        String putResId = res.getUid();
        ArrayList<Resource> putRes = pResByUid(userRes, res, putResId);
        Map<String, Object> updateInfoMap = new HashMap<>();
        updateInfoMap.put("resource", putRes);
        Update update = commonUtil.setUpdate(updateInfoMap);
        return userDao.updateInfo(email, update);
    }

    private ArrayList<Resource> pResByUid(ArrayList<Resource> uRes, Resource res, String uid) {
        if (uRes != null || uRes.size() != 0) {
            for (int i = 0; i < uRes.size(); i++) {
                Resource resource = uRes.get(i);
                if (resource.getUid().equals(uid)) {
                    uRes.remove(i);
                    String[] nullPropertyNames = getNullPropertyNames(res);
                    BeanUtils.copyProperties(res, resource, nullPropertyNames);
                    uRes.add(i, resource);
                    //查询到结束
                    break;
                } else {
                    //如果是文件夹再往底层搜索，如果是资源的话就不用往底层搜索了
                    if (resource.getFolder()) {
                        pResByUid(resource.getChildren(), res, uid);
                    } else {
                        continue;
                    }
                }
            }
        }
        return uRes;
    }

    /**
     * @Author zhngzhng
     * @Description 提取对象空字段
     * @Param [source]
     * @Date 2021/4/9
     */
    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object propertyValue = src.getPropertyValue(pd.getName());
            if (propertyValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    /**
     * 更强大的查询功能，可通过多个字段进行查询
     *
     * @param principal
     * @param field
     * @param value
     * @return
     */
    @Override
    public JsonResult findResByField(Principal principal, String field, String value) {
        //涉及到资源已经不是读写数据库的查询了，而是遍历查询对比
        String email = principal.getName();
        //dao 层使用 JsonResult 就是败笔
        User user = (User) userDao.findUserById(email).getData();
        ArrayList<Resource> userRes = user.getResource();

        //遍历 or 递归
        return null;
    }

    private ArrayList<Resource> fResByField(ArrayList<Resource> userRes, String field, String value) {

        return null;
    }

    @Override
    public JsonResult searchResByKeyword(Principal principal, String keyword) {
        String email = principal.getName();
        User user = (User) userDao.findUserById(email).getData();
        ArrayList<Resource> userRes = user.getResource();
        ArrayList<Resource> tempRes = new ArrayList<>();
        ArrayList<Resource> matchedRes = sResByKeyword(userRes, keyword, tempRes);
        if (matchedRes.size() > 0) {
            return ResultUtils.success(matchedRes);
        }
        return ResultUtils.noObject();
    }

    private ArrayList<Resource> sResByKeyword(ArrayList<Resource> userRes, String keyword, ArrayList<Resource> matchedRes) {
        //资源为空或者没内容的时候停止
        if (userRes != null || userRes.size() != 0) {
            for (int i = 0; i < userRes.size(); i++) {
                Resource resource = userRes.get(i);
                //正则表达式匹配, 名称或后缀
                Pattern pattern = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(resource.getName());
                //资源或文件夹都可以查询的
                if (resource.getFolder()) {
                    //为文件夹时
                    if (matcher.find()) {
                        matchedRes.add(resource);
                    } else {
                        sResByKeyword(resource.getChildren(), keyword, matchedRes);
                    }
                } else {
                    //为资源时
                    Matcher matcher1 = pattern.matcher(resource.getSuffix());
                    if (matcher.find() || matcher1.find()) {
                        matchedRes.add(resource);
                    }
                }
            }
            return matchedRes;
        }
        return matchedRes;
    }


    @Override
    public ArrayList<Resource> searchResByUid(Principal principal, ArrayList<String> uids) {
        String email = principal.getName();
        User user = (User) userDao.findUserById(email).getData();
        ArrayList<Resource> userRes = user.getResource();
        ArrayList<Resource> fileList = new ArrayList<>();
        for (String uid : uids) {
            Resource resource = sResByUid(userRes, uid, new Resource());
            fileList.add(resource);
        }
        return fileList;
    }

    private Resource sResByUid(ArrayList<Resource> userRes, String uid, Resource resource) {
        //等于空停止或值查找到就结束
        if (userRes.size() != 0) {
            for (Resource item : userRes) {
                if (item.getFolder()) {
                    sResByUid(item.getChildren(), uid, resource);
                } else {
                    if (item.getUid().equals(uid)) {
                        BeanUtils.copyProperties(item, resource);
                        return item;
                    }
                }
            }
        }
        return resource;
    }

    @Override
    public ArrayList<Resource> getAllResource(Principal principal) {
        String email = principal.getName();
        User user = (User) userDao.findUserById(email).getData();
        ArrayList<Resource> userRes = user.getResource();
        //递归将所有资源拿出来,初始化一个空的ArrayList传到递归函数用于存储
        ArrayList<Resource> fileList = new ArrayList<>();
        return gAllResource(userRes, fileList);
    }

    private ArrayList<Resource> gAllResource(ArrayList<Resource> userResList, ArrayList<Resource> fileList) {
        if (userResList.size() != 0) {
            for (Resource res : userResList) {
                if (res.getFolder()) {
                    gAllResource(res.getChildren(), fileList);
                } else {
                    fileList.add(res);
                }
            }
        }
        return fileList;
    }

    @Override
    public Object moveUserInDB(User user) {
        String email = user.getEmail();
        /*
        email 是唯一标识符，通过 email判断是否重复
        冲突处理原则：先入库优先级高
        1.基础字段中有内容字段保持原内容不变，字段为空情况使用后入库补充
            补充：
            字段类型为数组情况，使用并方式处理
            avatar 传 base64字符串过来，用户服务器转为图片进行存储，avatar 字段存储图片在用户服务器中的地址
            title 不一样的话，选级别高的存入
        2.资源处理方式
            参与式平台这边由于之前无文件夹结构，直接将用户资源全部转入My Data
            门户那边资源按照文件夹结构迁移过来，资源有重复的话也无所谓（唯一标识符不一样）
        3.密码问题
            采用门户那边的加密方式（md5+sha256），参与式平台用户全部需要修改密码
         */
        User localUser = userDao.queryUserByEmail(email);
        //无此用户
        if (localUser == null) {
            String avatarStr = user.getAvatar();
            if (avatarStr != null) {
                if (!avatarStr.equals("")) {
                    CommonUtil commonUtil = new CommonUtil();
                    String avatarPath = commonUtil.avatarBase64ToPath(avatarStr);
                    user.setAvatar(avatarPath);
                }
            }
            ArrayList<Resource> resource = user.getResource();
            ArrayList<Resource> rootList = new ArrayList<>();
            //如果资源为空，则新建一个 My data 文件夹给他
            if (resource == null || resource.size() == 0) {
                Resource defaultFolder = new Resource();
                defaultFolder.setUid(UUID.randomUUID().toString());
                defaultFolder.setName("My data");
                defaultFolder.setFolder(true);
                defaultFolder.setPrivacy("public");
                defaultFolder.setParent("0");
                rootList.add(defaultFolder);
                user.setResource(rootList);
            }
            //有资源的话，直接存入即可了
            // else {
                /*
                如果有资源的话，新建 portalResource 文件夹，将内容放进去
                徒增工作量
                 */
            // Resource portalResource = new Resource();
            // portalResource.setUid(UUID.randomUUID().toString());
            // portalResource.setName("portalResource");
            // portalResource.setFolder(true);
            // portalResource.setPrivacy("public");
            // portalResource.setParent("0");
            // portalResource.setChildren(resource);
            // rootList.add(portalResource);
            // }


        } else {
            //库中已经有此用户,localUser至少都有一个My data 文件夹
            ArrayList<Resource> portalResource = user.getResource();
            ArrayList<Resource> localUserResource = localUser.getResource();
            //将本地用户非空字段赋值给后导入的用户, resources已经被覆盖,password不覆盖，使用门户的密码
            String[] nullPropertyNames = getUserNullPropertyNames(localUser);
            BeanUtils.copyProperties(localUser, user, nullPropertyNames);

            //将本地用户 resource 添加到后导入的用户的resourceList中
            if (portalResource == null || portalResource.size() == 0) {
                portalResource = new ArrayList<Resource>();
            }
            for (Resource res : localUserResource) {
                portalResource.add(res);
            }
            user.setResource(portalResource);
            //头像也得单独处理
            String localUserAvatar = localUser.getAvatar();
            String portalUserAvatar = user.getAvatar();

            //本地用户avatar为空或""并且后导入用户有头像（已经转为base64字符串了）
            if (localUserAvatar == null || localUserAvatar.equals("")){
                if (portalUserAvatar != null){
                    if (!portalUserAvatar.equals("")){
                        CommonUtil commonUtil = new CommonUtil();
                        String avatarStr = commonUtil.avatarBase64ToPath(portalUserAvatar);
                        user.setAvatar(avatarStr);
                    }else {
                        user.setAvatar(null);
                    }
                }
            }
        }
        userDao.moveInDb(user);
        return "suc";
    }

    private String[] getUserNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object propertyValue = src.getPropertyValue(pd.getName());
            if (propertyValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        //直接不添加 resource 内容
        emptyNames.add("password");
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    public String gsmUserToUserServer(User user){
        String avatar = user.getAvatar();
        if (avatar != null &&  !avatar.equals("")){
            String avatarUrl = commonUtil.avatarBase64ToPath(avatar);
            user.setAvatar(avatarUrl);
        }
        if (avatar != null){
            if (avatar.equals("")){
                user.setAvatar(null);
            }
        }
        userDao.moveInDb(user);
        return "suc";
    }


}
