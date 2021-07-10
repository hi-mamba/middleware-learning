package space.mamba.es.crud;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import space.mamba.es.crud.service.UserService;

import javax.annotation.Resource;

/**
 * @author pankui
 * @date 2021/7/9 
 * <pre>
 *
 * </pre>  
 */
@Slf4j
//@Component
public class InitDataContext implements CommandLineRunner {

    @Resource
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        log.info("初始化数据");
        // userService.insert();
    }
}
