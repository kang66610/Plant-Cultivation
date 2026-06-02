package com.plantcultivation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping({"/", "/encyclopedia", "/plant/**", "/tools/**", "/about"})
    public String forward() {
        return "forward:/index.html";
    }
}
