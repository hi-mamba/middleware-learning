package space.mamba.es.crud.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import space.mamba.es.crud.service.EsService;
import space.mamba.es.crud.service.UserService;

import javax.annotation.Resource;

/**
 * @author pankui
 * @date 2021/7/9 
 * <pre>
 *
 * </pre>  
 */
@RestController
@RequestMapping("/api")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private EsService esService;

    @GetMapping("/list")
    public Object list() {
        return userService.list();
    }

    @GetMapping("/sql")
    public void sql() {
        esService.customSql();
    }
}
