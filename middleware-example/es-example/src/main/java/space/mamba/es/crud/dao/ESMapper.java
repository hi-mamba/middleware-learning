package space.mamba.es.crud.dao;

import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * @author pankui
 * @date 2021/7/9 
 * <pre>
 *
 * </pre>  
 */
//@DS("es")
//@Mapper
public interface ESMapper {

    @Select("${sql}")
    Map<String, Object> selectMap(Map<String, String> params);
}
