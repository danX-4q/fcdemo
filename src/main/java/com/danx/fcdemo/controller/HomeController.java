package com.danx.fcdemo.controller;

import com.danx.fcdemo.service.FcWithRaw;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @RequestMapping("/")
    public String Index() {

        FcWithRaw fcinst = new FcWithRaw();
        fcinst.Do();

        return "hello danx by vscode";
    }
}
