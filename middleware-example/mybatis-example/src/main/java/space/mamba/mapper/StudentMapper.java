package space.mamba.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import space.mamba.model.Student;

/**
 * @author pankui
 */
public interface StudentMapper extends BaseMapper<Student> {


    @Select("select * from student where id=#{id}")
    public Student findById(Integer id);

}