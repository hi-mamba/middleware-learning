package space.mamba.es;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author pankui
 * @date 2021/7/10 
 * <pre>
 *
 * </pre>  
 */
public class ESJdbcTest {

    private final static String driver = "org.elasticsearch.xpack.sql.jdbc.jdbc.JdbcDriver";
    private final static String elasticsearchAddress = "127.0.0.1:9200";

    public static Properties connectionProperties() {
        Properties properties = new Properties();
        //如果集群设置了密码
        //properties.put("user", "test_admin");
        //properties.put("password", "x-pack-test-password");
        return properties;
    }

    public static void main(String[] args) {

        String address = "jdbc:es://" + elasticsearchAddress + "?user=elastic&password=elastic";
        Properties connectionProperties = connectionProperties();
        try {
            Connection connection = DriverManager.getConnection(address, connectionProperties);
            Statement statement = connection.createStatement();

            //目前执行 创建有问题
            //    String sql = "create table persons ( id int,name varchar(255)))";
            //    statement.execute(sql);
            // 查询这个 索引必须存在，　
            ResultSet results = statement.executeQuery("SELECT * FROM persons");
            while (results.next()) {
                System.out.println(results.getString("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
