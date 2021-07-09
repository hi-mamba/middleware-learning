package space.mamba.es.crud.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import space.mamba.es.crud.dao.UserDAO;
import space.mamba.es.crud.domain.User;

import javax.annotation.Resource;

/**
 *
 * @author pannkui
 *
 */
@Slf4j
@Service
public class UserService {

    @Resource
    private UserDAO userDAO;

    public void insert() {
        User user = new User();
        user.setId(System.currentTimeMillis());
        user.setName(RandomStringUtils.random(10));
        User result = userDAO.save(user);
        log.info("insert user ={}", result);
    }

    public Iterable<User> list() {
        return userDAO.findAll();
    }
}
