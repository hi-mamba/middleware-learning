package space.mamba.es.crud.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import space.mamba.es.crud.dao.ESMapper;
import space.mamba.es.crud.domain.User;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pannkui
 *
 */
@Slf4j
@Service
public class UserService {

    // @Resource
    // private UserDAO userDAO;

    @Resource
    private ESMapper esMapper;

    public void insert() {
        User user = new User();
        user.setId(System.currentTimeMillis());
        user.setName(RandomStringUtils.random(10));
        //  User result = userDAO.save(user);
        //  log.info("insert user ={}", result);
    }

    public Iterable<User> list() {
        //  return userDAO.findAll();
        return null;
    }

    public void customSql() {
        Map<String, String> map = new HashMap<>();
        map.put("sql", "select * from user limit 1");
        log.info("result=", esMapper.selectMap(map));
        // 输出： {eid=z, reason=}
    }
}
