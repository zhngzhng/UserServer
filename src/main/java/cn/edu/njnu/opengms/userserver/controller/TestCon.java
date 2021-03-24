package cn.edu.njnu.opengms.userserver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class TestCon {
    @RequestMapping(value = "/getInfo", method = RequestMethod.GET)
    public String getUser(Principal principal){
        return principal.getName();
    }
}
