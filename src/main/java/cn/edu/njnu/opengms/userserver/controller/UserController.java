package cn.edu.njnu.opengms.userserver.controller;

import cn.edu.njnu.opengms.userserver.common.JsonResult;
import cn.edu.njnu.opengms.userserver.entity.ClientDetails;
import cn.edu.njnu.opengms.userserver.entity.User;
import cn.edu.njnu.opengms.userserver.service.impl.UserServiceImpl;
import org.springframework.web.bind.annotation.*;


/**
 * @ClassName UserController
 * @Description 用户相关 无需授权 Mapping
 * @Author zhngzhng
 * @Date 2021/3/24
 **/
@RestController
@RequestMapping(value = "/user")
public class UserController {
    final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public JsonResult addUser(@RequestBody User user){
        return userService.addUser(user);
    }

    /**
     * 忘记密码
     * 发送修改密码邮件
     * @param email
     * @return
     */
    @RequestMapping(value = "/resetPwd/{email}", method = RequestMethod.GET)
    public JsonResult sendEmail(@PathVariable String email){
        return userService.sendCodeEmail(email);
    }

    /**
     * 忘记密码，验证码更改密码
     * @param email
     * @param code
     * @param newPwd
     * @return
     */
    @RequestMapping(value = "/resetPwd/{email}/{code}/{newPwd}", method = RequestMethod.GET)
    public JsonResult updatePassword(@PathVariable("email") String email,
                                     @PathVariable("code") String code,
                                     @PathVariable("newPwd") String newPwd){
        return userService.resetPwd(email, code, newPwd);
    }

    @RequestMapping(value = "/client", method = RequestMethod.POST)
    public JsonResult addClient(@RequestBody ClientDetails client){
        return userService.addClientService(client);
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
