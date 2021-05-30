package space.mamba.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import space.mamba.model.Student;
import space.mamba.service.StudentService;

import javax.annotation.Resource;
import java.util.function.Function;

/**
 * @author pankui
 * @date 2021/5/29
 * <pre>
 *
 * </pre>
 */
@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Resource
    private StudentService studentService;


    @GetMapping("/test")
    public Object get(Integer id) {
        return studentService.getById(id);
    }

    @PostMapping("/insert")
    public Object insert() {
        Student student = new Student();
        student.setName(RandomStringUtils.randomAlphabetic(10, 20));
        // studentService.save(student);
        return get(student.getId());
    }

    @GetMapping("/name")
    public Object getByName(String name) {
        return studentService.getMap(new QueryWrapper<Student>().lambda().eq(Student::getName, name));
    }

    @GetMapping("/name2")
    public Object getByName2(String name) {
        return studentService.getObj(new QueryWrapper<Student>().lambda().eq(Student::getName, name), Function.identity());
    }

}
