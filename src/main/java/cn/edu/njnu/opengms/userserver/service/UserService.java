package cn.edu.njnu.opengms.userserver.service;

import cn.edu.njnu.opengms.userserver.common.JsonResult;
import cn.edu.njnu.opengms.userserver.entity.ClientDetails;
import cn.edu.njnu.opengms.userserver.entity.Resource;
import cn.edu.njnu.opengms.userserver.entity.User;
import com.alibaba.fastjson.JSONObject;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;

public interface UserService {
    JsonResult getUserInfo(String email);

    JsonResult updateUserInfo(Principal principal,HashMap<String, Object> updateInfo);

    JsonResult addUser(User user);

    JsonResult sendCodeEmail(String email);

    JsonResult resetPwd(String email, String code, String newPwd);

    JsonResult newPwd(Principal principal, String oldPwd, String newPwd);

    //添加、更新资源
    JsonResult updateRes(Principal principal, ArrayList<Resource> resources, String operationType);

    //删除资源是单独的
    JsonResult delRes(Principal principal, String[] uids);

    JsonResult addClientService(ClientDetails client);


    //文件夹结构相关内容
    JsonResult addRes(Principal principal, Resource upRes, ArrayList<String> paths);

    JsonResult delRes(Principal principal, String uid, ArrayList<String> paths);

    JsonResult putRes(Principal principal, Resource updateRes, ArrayList<String> paths);

    JsonResult getAllFileService(Principal principal);

    JsonResult changeFolder(Principal principal, Resource upRes, ArrayList<String> oldPaths, ArrayList<String> newPaths);

    // JsonResult addFiles(Principal principal, Resource upRes, ArrayList<String> paths);
}
