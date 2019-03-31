package io.docbot.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "test")
public class MyController {
    @RequestMapping(value = "helloworld",method = {RequestMethod.GET,RequestMethod.POST})
    public void test(){

    }
}
