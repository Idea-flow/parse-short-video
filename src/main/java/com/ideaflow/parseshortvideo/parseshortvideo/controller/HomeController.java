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

    /**
     * Thymeleaf 测试页面
     */
    @GetMapping("/test")
    public String test(Model model) {
        model.addAttribute("title", "Thymeleaf 测试页面");
        return "test";
    }

    /**
     * Vue 调试页面
     */
    @GetMapping("/vue-debug")
    public String vueDebug(Model model) {
        model.addAttribute("title", "Vue 调试测试");
        return "vue-debug";
    }
}

