package space.mamba.es.crud.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import space.mamba.es.crud.dao.ESMapper;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pankui
 * @date 2021/7/10 
 * <pre>
 *
 * </pre>  
 */
@Service
@Slf4j
public class EsService {

    @Resource
    private ESMapper esMapper;

    public void customSql() {
        Map<String, String> map = new HashMap<>();
        map.put("sql", "select * from user");
        log.info("result={}", esMapper.selectMap(map));
    }

}
