package com.ideaflow.parseshortvideo.parseshortvideo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页控制器
 */
@Controller
public class HomeController {

    /**
     * 首页
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "无水印解析");
        return "index";
    }

}

