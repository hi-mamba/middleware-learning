package space.mamba;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import space.mamba.mapper.StudentMapper;
import space.mamba.model.Student;

import javax.annotation.Resource;

/**
 * @author pankui
 * @date 2021/6/23
 * <pre>
 *
 * </pre>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = MybatisApplication.class)
public class MyBatisTest {

    @Resource
    private StudentMapper studentMapper;

    @Test
    public void pluginTest() {
        Student student = studentMapper.findById(1);
        System.out.println(student);
    }
}
