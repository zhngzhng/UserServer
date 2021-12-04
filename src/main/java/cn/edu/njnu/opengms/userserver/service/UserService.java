package cn.edu.njnu.opengms.userserver.service;

import cn.edu.njnu.opengms.userserver.common.JsonResult;
import cn.edu.njnu.opengms.userserver.entity.ClientDetails;
import cn.edu.njnu.opengms.userserver.entity.Resource;
import cn.edu.njnu.opengms.userserver.entity.User;
import com.alibaba.fastjson.JSONObject;
import org.joda.time.LocalDate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

public interface UserService {
    JsonResult getUserInfo(String email);

    JsonResult loginService(String email, String loginIp);

    JsonResult updateUserInfo(Principal principal,HashMap<String, Object> updateInfo);

    JsonResult addUser(User user);

    JsonResult sendCodeEmail(String email);

    JsonResult resetPwd(String email, String code, String newPwd);

    JsonResult newPwd(Principal principal, String oldPwd, String newPwd);

    JsonResult getUserBase(Principal principal);

    //添加、更新资源
    // JsonResult updateRes(Principal principal, ArrayList<Resource> resources, String operationType);
    //
    // //删除资源是单独的
    // JsonResult delRes(Principal principal, String[] uids);

    JsonResult addClientService(ClientDetails client);


    //文件夹结构相关内容
    JsonResult addRes(Principal principal, Resource upRes, ArrayList<String> paths);

    JsonResult delRes(Principal principal, String uid, ArrayList<String> paths);

    JsonResult putRes(Principal principal, Resource updateRes, ArrayList<String> paths);

    JsonResult changeFolder(Principal principal, Resource upRes, ArrayList<String> oldPaths, ArrayList<String> newPaths);

    //获取文件夹下内容
    JsonResult getFileByPath(Principal principal, ArrayList<String> paths);

    JsonResult getAllResService(Principal principal);

    JsonResult getAllFolder(Principal principal);

    JsonResult delByUid(Principal principal, String uid);

    JsonResult delByUids(Principal principal, ArrayList<String> uids);

    JsonResult putByUid(Principal principal, Resource res);

    JsonResult findResByField(Principal principal,  String field, String value);

    JsonResult searchResByKeyword(Principal principal, String keyword);

    ArrayList<Resource> searchResByUid(Principal principal, ArrayList<String> uid);

    ArrayList<Resource> getAllResource(Principal principal);

    HashMap<String, String> hasCapacity(Principal principal, long size);

    HashMap<String, Long> getUserCapacity(Principal principal);

    Object uploadFlag(Principal principal, String uploadingId);

    // JsonResult addFiles(Principal principal, Resource upRes, ArrayList<String> paths);

    //入库函数
    Object moveUserInDB(User user) throws IOException;

    Object validEmailIsRegistered(String email);

    JsonResult getAvatarUrl(String email);

    JsonResult getUserTag(String userId);

    JsonResult getUserTag(HashSet<String> userIds);

    JsonResult getUserInfo(String email, String client, String secret);

    JsonResult getUsersTag(HashSet<String> userIds);

    Long userCount(String client, String secret);

    TreeMap<LocalDate, Integer> userRegisterTime(String client, String secret);

}
