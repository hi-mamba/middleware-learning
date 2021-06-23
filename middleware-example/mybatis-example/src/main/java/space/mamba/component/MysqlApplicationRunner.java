package space.mamba.component;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static org.apache.ibatis.io.Resources.getResourceAsReader;

/**
 * @author pankui
 * @date 2021/5/29
 * <pre>
 *    初始化表
 * </pre>
 */
@Component
public class MysqlApplicationRunner implements ApplicationRunner {

    @Resource
    private HikariDataSource dataSource;

    @Override

    public void run(ApplicationArguments args) throws Exception {
        ScriptRunner runner = new ScriptRunner(dataSource.getConnection());
        runner.setAutoCommit(true);
        runner.setStopOnError(true);
        runner.runScript(getResourceAsReader("scripts/init_table.sql"));
        runner.closeConnection();
    }
}
