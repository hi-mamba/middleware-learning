package space.mamba.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import space.mamba.domain.Student;
import space.mamba.mq.producer.StudentProducer;

import javax.annotation.Resource;

/**
 * @author pankui
 * @date 2021/7/12 
 * <pre>
 *
 * </pre>  
 */
@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Resource
    private StudentProducer studentProducer;

    @GetMapping("/producer")
    public void producer() {
        Student student = new Student();
        student.setId(System.currentTimeMillis());
        student.setName(RandomStringUtils.random(10));
        studentProducer.producer(student);
    }
}
