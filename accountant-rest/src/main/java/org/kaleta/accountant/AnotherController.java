package org.kaleta.accountant;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AnotherController {

    @RequestMapping(value="/home")
    public String index () {
        return "";
    }
}
