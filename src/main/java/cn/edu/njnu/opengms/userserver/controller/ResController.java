package cn.edu.njnu.opengms.userserver.controller;

import cn.edu.njnu.opengms.userserver.common.JsonResult;
import cn.edu.njnu.opengms.userserver.common.ResultUtils;
import cn.edu.njnu.opengms.userserver.entity.Resource;
import cn.edu.njnu.opengms.userserver.service.impl.UserServiceImpl;
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

    /**
     * @param principal
     * @return
     */
    @RequestMapping(value = "/userInfo", method = RequestMethod.GET)
    public JsonResult getUserInfo(Principal principal){
        String email = principal.getName();
        return userService.getUserInfo(email);
    }

    @RequestMapping(value = "/login/{ip}", method = RequestMethod.GET)
    public JsonResult login(Principal principal, @PathVariable String ip){
        String email = principal.getName();
        return userService.loginService(email, ip);
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

    /**
     * 获取用户除资源外的所有字段
     * @param principal
     * @return
     */
    @RequestMapping(value = "/userBase", method = RequestMethod.GET)
    public JsonResult getUserBase(Principal principal){
        return userService.getUserBase(principal);
    }


    //资源(文件夹)相关操作

    /**
     * 获取用户资源
     * @param principal
     * @return
     */
    @RequestMapping(value = "/res", method = RequestMethod.GET)
    public JsonResult getAllRes(Principal principal){
        return userService.getAllResService(principal);
    }

    /**
     * 上传资源/新建文件夹
     * 再添加一次容量
     * @param principal
     * @param resource
     * @param paths 由内到外写，
     * @return
     */
    @RequestMapping(value = "/res/{paths}", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
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
     * 删除资源，将需要删除的资源的 id 传过来即可
     */
    @RequestMapping(value = "/res/{uid}",method = RequestMethod.DELETE)
    public JsonResult delResByUid(Principal principal, @PathVariable String uid){
        return userService.delByUid(principal, uid);
    }

    /**
     * 批量删除
     * @param principal
     * @param uids
     * @return
     */
    @RequestMapping(value = "/res/batch/{uids}", method = RequestMethod.DELETE)
    public JsonResult delResByUids(Principal principal, @PathVariable ArrayList<String> uids){
        return userService.delByUids(principal, uids);
    }

    /**
     * 更新资源信息，可以只传入修改的字段
     * @param principal
     * @param resource
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


    //===========================2021/4/8=================
    /**
     * 获取用户文件夹
     * @param principal
     * @return
     */
    @RequestMapping(value = "/res/folder",method = RequestMethod.GET)
    public JsonResult getAllFolder(Principal principal){
        return userService.getAllFolder(principal);
    }

    /**
     * 更新资源信息，只需要将需要更新的资源传过来，不需要路径
     * 支持仅修改部分字段
     * @param principal
     * @param resource
     * @return
     */
    @RequestMapping(value = "/res",produces = "application/json;charset=utf-8", method = RequestMethod.PUT)
    public JsonResult updateResByUid(Principal principal, @RequestBody Resource resource){
        return userService.putByUid(principal, resource);
    }




    
    /**
    * @Author zhngzhng
    * @Description 通过关键字或后缀名查询资源，支持模糊查询
    * @Param [principal, keyword]
    * @Date 2021/4/8
    */
    @RequestMapping(value = "/res/search/{keyword}", method = RequestMethod.GET)
    public JsonResult searchResByKeyword(Principal principal, @PathVariable String keyword){
        return userService.searchResByKeyword(principal, keyword);
    }

    /**
     * 根据 id 查询内容,多个内容
     * @param principal
     * @param uid
     * @return
     */
    @RequestMapping(value = "/res/s/{uid}", method = RequestMethod.GET)
    public JsonResult getResByUid(Principal principal, @PathVariable ArrayList<String> uid){
        ArrayList<Resource> fileList = userService.searchResByUid(principal, uid);
        return ResultUtils.success(fileList);
    }

    @RequestMapping(value = "/res/file/all", method = RequestMethod.GET)
    public JsonResult getAllFileList(Principal principal){
        ArrayList<Resource> allResource = userService.getAllResource(principal);
        return ResultUtils.success(allResource);
    }

    /**
     * 上传文件预检接口
     * 检查是否有空间
     * 9223372036854775807
     * @param principal
     * @param size
     * @return
     */
    @RequestMapping(value = "/res/upload/{size}", method = RequestMethod.GET)
    public JsonResult checkFileCanBeUpload(Principal principal, @PathVariable long size){
        HashMap<String, String> uploadMap = userService.hasCapacity(principal, size);
        String canUpload = uploadMap.get("canUpload");
        if (canUpload.equals("false")){
            return ResultUtils.error(-2, uploadMap.get("remainCapacity"));
        }
        return ResultUtils.success(uploadMap);
    }

    /**
     * 获取用户空间
     * 返回两个字段
     * usedCapacity
     * capacity
     * @param principal
     * @return
     */
    @RequestMapping(value = "/res/capacity", method = RequestMethod.GET)
    public JsonResult getUserCapacity(Principal principal){
        HashMap<String, Long> userCapacity = userService.getUserCapacity(principal);
        return ResultUtils.success(userCapacity);
    }

    /**
     * 删除上传标记
     * 用于上传失败后将容量复原
     * 上传成功不用处理
     * @param principal
     * @param id
     * @return
     */
    @RequestMapping(value = "/res/uploadFlag/{id}", method = RequestMethod.GET)
    public JsonResult delUploadFlag(Principal principal, @PathVariable String id){
        return userService.uploadFlag(principal, id);
    }

}
