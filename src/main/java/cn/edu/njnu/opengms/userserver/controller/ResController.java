package cn.edu.njnu.opengms.userserver.controller;

import cn.edu.njnu.opengms.userserver.common.JsonResult;
import cn.edu.njnu.opengms.userserver.entity.Resource;
import cn.edu.njnu.opengms.userserver.service.impl.UserServiceImpl;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @ClassName ResController
 * @Description 需要授权访问内容
 * @Author zhngzhng
 * @Date 2021/3/24
 **/
@RestController
@RequestMapping(value = "/auth")
public class ResController {
    @Autowired
    UserServiceImpl userService;

    @RequestMapping(value = "/userInfo", method = RequestMethod.GET)
    public JsonResult getUserInfo(Principal principal){
        String email = principal.getName();
        return userService.getUserInfo(email);
    }

    @RequestMapping(value = "/update", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    public JsonResult updateUserInfo(Principal principal, @RequestBody HashMap<String, Object> userInfo){
        return userService.updateUserInfo(principal,userInfo);
    }

    /**
     * 更改密码
     * @return
     */
    @RequestMapping(value = "/newPwd/{oldPwd}/{newPwd}", method = RequestMethod.GET)
    public JsonResult resetPassword(Principal principal,
                                    @PathVariable("oldPwd") String oldPwd,
                                    @PathVariable("newPwd") String newPwd
                                    ){
        return userService.newPwd(principal, oldPwd, newPwd);
    }

    // 资源相关操作，这是老版本废弃版本
    @RequestMapping(value = "/res", method = RequestMethod.POST)
    public JsonResult uploadRes(Principal principal, @RequestBody ArrayList<Resource> resources){
        return userService.updateRes(principal, resources, "push");
    }

    @RequestMapping(value = "/res", method = RequestMethod.PUT)
    public JsonResult updateRes(Principal principal, @RequestBody ArrayList<Resource> resources){
        return userService.updateRes(principal, resources, "update");
    }

    @RequestMapping(value = "/res/{uids}", method = RequestMethod.DELETE)
    public JsonResult delRes(Principal principal, @PathVariable("uids") String[] uids){
        return userService.delRes(principal, uids);
    }


    //资源(文件夹)相关操作

    @RequestMapping(value = "/res", method = RequestMethod.GET)
    public JsonResult getAllFile(Principal principal){
        return userService.getAllFileService(principal);
    }

    @RequestMapping(value = "/res/{paths}", method = RequestMethod.POST)
    public JsonResult uploadRes(Principal principal, @RequestBody Resource resource, @PathVariable ArrayList<String> paths){
        return userService.addRes(principal, resource, paths);
    }

    @RequestMapping(value = "/res/{uid}/{paths}", method = RequestMethod.DELETE)
    public JsonResult delRes(Principal principal,
                             @PathVariable("uid") String uid,
                             @PathVariable("paths") ArrayList<String> paths){
        return userService.delRes(principal, uid, paths);
    }

    @RequestMapping(value = "/res/{paths}", method = RequestMethod.PUT)
    public JsonResult putRes(Principal principal,
                             @RequestBody Resource resource,
                             @PathVariable("paths") ArrayList<String> paths){
        return userService.putRes(principal, resource, paths);
    }

    @RequestMapping(value = "/res/{newPaths}/{oldPaths}", method = RequestMethod.PUT)
    public JsonResult changeFolder(Principal principal,
                                   @RequestBody Resource resource,
                                   @PathVariable("newPaths") ArrayList<String> newPaths,
                                   @PathVariable("oldPaths") ArrayList<String> oldPaths){
        return userService.changeFolder(principal, resource, oldPaths, newPaths);
    }


}
