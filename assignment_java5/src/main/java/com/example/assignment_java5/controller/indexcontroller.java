package com.example.assignment_java5.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/index")
public class indexcontroller {
    @GetMapping("/")
    public String home() {
        return  "index";
    }
}
