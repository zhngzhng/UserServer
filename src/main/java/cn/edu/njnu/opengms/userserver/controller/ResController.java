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


    //资源(文件夹)相关操作

    /**
     * 获取用户所有资源
     * @param principal
     * @return
     */
    @RequestMapping(value = "/res", method = RequestMethod.GET)
    public JsonResult getAllFile(Principal principal){
        return userService.getAllFileService(principal);
    }

    /**
     * 上传资源/新建文件夹
     * @param principal
     * @param resource
     * @param paths 由内到外写，
     * @return
     */
    @RequestMapping(value = "/res/{paths}", method = RequestMethod.POST)
    public JsonResult uploadRes(Principal principal, @RequestBody Resource resource, @PathVariable ArrayList<String> paths){
        return userService.addRes(principal, resource, paths);
    }

    /**
     * 删除资源
     * @param principal
     * @param uid 需要删除的资源的 id
     * @param paths
     * @return
     */
    @RequestMapping(value = "/res/{uid}/{paths}", method = RequestMethod.DELETE)
    public JsonResult delRes(Principal principal,
                             @PathVariable("uid") String uid,
                             @PathVariable("paths") ArrayList<String> paths){
        return userService.delRes(principal, uid, paths);
    }

    /**
     * 更新资源信息
     * @param principal
     * @param resource 需要将资源所有内容传过来
     * @param paths
     * @return
     */
    @RequestMapping(value = "/res/{paths}", method = RequestMethod.PUT)
    public JsonResult putRes(Principal principal,
                             @RequestBody Resource resource,
                             @PathVariable("paths") ArrayList<String> paths){
        return userService.putRes(principal, resource, paths);
    }

    /**
     * 修改资源路径
     * @param principal
     * @param resource 需要移动的资源
     * @param newPaths 新的资源路径
     * @param oldPaths 老的资源路径
     * @return
     */
    @RequestMapping(value = "/res/{newPaths}/{oldPaths}", method = RequestMethod.PUT)
    public JsonResult changeFolder(Principal principal,
                                   @RequestBody Resource resource,
                                   @PathVariable("newPaths") ArrayList<String> newPaths,
                                   @PathVariable("oldPaths") ArrayList<String> oldPaths){
        return userService.changeFolder(principal, resource, oldPaths, newPaths);
    }

    @RequestMapping(value = "/res/{paths}", method = RequestMethod.GET)
    public JsonResult getFileByPath(Principal principal, @PathVariable("paths") ArrayList<String> paths){
        return userService.getFileByPath(principal, paths);
    }


}
