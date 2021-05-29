package space.mamba.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import space.mamba.mapper.StudentMapper;
import space.mamba.model.Student;
import space.mamba.service.StudentService;

/**
 * @author pankui
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

}
