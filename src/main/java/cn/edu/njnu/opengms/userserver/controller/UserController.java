package cn.edu.njnu.opengms.userserver.controller;

import cn.edu.njnu.opengms.userserver.common.JsonResult;
import cn.edu.njnu.opengms.userserver.common.ResultUtils;
import cn.edu.njnu.opengms.userserver.entity.ClientDetails;
import cn.edu.njnu.opengms.userserver.entity.User;
import cn.edu.njnu.opengms.userserver.service.impl.UserServiceImpl;
import org.joda.time.LocalDate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;


/**
 * @ClassName UserController
 * @Description 用户相关 无需授权 Mapping
 * @Author zhngzhng
 * @Date 2021/3/24
 **/
@RestController
@RequestMapping(value = "/user")
public class UserController {
    private final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public JsonResult addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    /**
     * 忘记密码
     * 发送修改密码邮件
     *
     * @param email
     * @return
     */
    @RequestMapping(value = "/resetPwd/{email}", method = RequestMethod.GET)
    public JsonResult sendEmail(@PathVariable String email) {
        return userService.sendCodeEmail(email);
    }

    /**
     * 忘记密码，验证码更改密码
     *
     * @param email
     * @param code
     * @param newPwd
     * @return
     */
    @RequestMapping(value = "/resetPwd/{email}/{code}/{newPwd}", method = RequestMethod.GET)
    public JsonResult updatePassword(@PathVariable("email") String email,
                                     @PathVariable("code") String code,
                                     @PathVariable("newPwd") String newPwd) {
        return userService.resetPwd(email, code, newPwd);
    }

    @RequestMapping(value = "/client", method = RequestMethod.POST)
    public JsonResult addClient(@RequestBody ClientDetails client) {
        return userService.addClientService(client);
    }


    //========================= Portal 补充=============================================================================
    @RequestMapping(value = "/getAvatar/{email}", method = RequestMethod.GET)
    public JsonResult getAvatarStr(@PathVariable String email) {
        return userService.getAvatarUrl(email);
    }


    @RequestMapping(value = "/{email}/{client}/{secret}", method = RequestMethod.GET)
    public JsonResult getUserInfo(@PathVariable String email, @PathVariable String client, @PathVariable String secret) {
        return userService.getUserInfo(email, client, secret);
    }

    @RequestMapping(value = "/count/{clientId}/{secret}", method = RequestMethod.GET)
    public JsonResult getUserCount(@PathVariable String clientId, @PathVariable String secret){
        Long userNum = userService.userCount(clientId, secret);
        if (userNum == null) return ResultUtils.error(-2, "Client is wrong.");
        return ResultUtils.success(userNum);
    }

    @RequestMapping(value = "/rTime/{clientId}/{secret}", method = RequestMethod.GET)
    public JsonResult getUserRegisterTime(@PathVariable String clientId, @PathVariable String secret){
        TreeMap<LocalDate, Integer> dateIntegerHashMap = userService.userRegisterTime(clientId, secret);
        if (dateIntegerHashMap == null) return ResultUtils.error(-2, "Client is wrong.");
        return ResultUtils.success(dateIntegerHashMap);
    }






    //========================= 参与式平台补充=============================================================================
    /**
     * 获取用户tag
     * @param userId
     * @return
     */
    @RequestMapping(value = "/tag/{userId}", method = RequestMethod.GET)
    public JsonResult getUserTag(@PathVariable String userId) {
        return userService.getUserTag(userId);
    }

    /**
     * 获取这些用户的所有标签
     * @param userIds
     * @return
     */
    @RequestMapping(value = "/tag/batch/{userIds}", method = RequestMethod.GET)
    public JsonResult getUsersTag(@PathVariable HashSet<String> userIds){
        return userService.getUserTag(userIds);
    }

    /**
     * 获取每个用户单独的标签
     * @param userIds
     * @return
     */
    @RequestMapping(value = "/tags/{userIds}", method = RequestMethod.GET)
    public JsonResult getUsersTags(@PathVariable HashSet<String> userIds){
        return userService.getUsersTag(userIds);
    }


    /**
     *
     * @param email
     * @return
     */
    @RequestMapping(value = "/registered/{email}", method = RequestMethod.GET)
    public JsonResult validEmailRegistered(@PathVariable String email){
        Integer integer = userService.validEmailIsRegistered(email);
        if (integer == 0){
            return ResultUtils.noObject("Email isn't be registered.");
        }
        return ResultUtils.success("Email has been registered.");
    }

    @RequestMapping(value = "/capacity", method = RequestMethod.GET)
    public void refreshUserCapacityCon(){
        userService.refreshUserCapacity();
    }

    @RequestMapping(value = "/in", method = RequestMethod.POST)
    public Object portalIn(@RequestBody User user){
        return userService.moveUserInDB(user);
    }

    @RequestMapping(value = "/gsmIn", method = RequestMethod.POST)
    public Object gsmIn(@RequestBody User user){
        return userService.gsmUserToUserServer(user);
    }

}
