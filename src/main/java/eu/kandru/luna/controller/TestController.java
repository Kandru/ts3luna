package eu.kandru.luna.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Just for testing purposes since there's no content here yet.
 *
 * @author jko
 */
@RestController
@Slf4j
public class TestController {
    // TODO: remove me!

    /**
     * Random text
     */
    @RequestMapping(value = "/protected/test")
    public String testSecurity(HttpServletRequest request) {

        return "yay";
    }

    /**
     * Random text
     */
    @RequestMapping(value = "/admin/test")
    public String testSecurityAdmin(HttpServletRequest request) {

        return "yay";
    }
}
