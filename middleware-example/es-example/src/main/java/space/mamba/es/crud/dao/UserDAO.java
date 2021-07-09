package space.mamba.es.crud.dao;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import space.mamba.es.crud.domain.User;

/**
 * @author pankui
 * @date 2021/7/9 
 * <pre>
 *
 * </pre>  
 */

public interface UserDAO extends ElasticsearchRepository<User, Long> {
    
}
