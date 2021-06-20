

# 使用MyBatis的ScriptRunner执行SQL脚本

```java
public class RunScriptTest {
	private static String APPENDED_DB_INFO 
    = "?useUnicode=true&characterEncoding=UTF8" 
            + "&rewriteBatchedStatements=true" 
            + "&useLegacyDatetimeCode=false" 
            + "&serverTimezone=Asia/Shanghai"
            + "&useSSL=false";
	private static String className   	= "com.mysql.jdbc.Driver";
    private static String url         			= "jdbc:mysql://localhost:3306/testdb" + APPENDED_DB_INFO;
    private static String username		= "root";
    private static String password		= "password";
    private static Connection connection      = null;
    
    public static void main(String[] args) throws ClassNotFoundException, SQLException, FileNotFoundException {
		Class.forName(className);
		connection = DriverManager.getConnection(url, username, password);
		ScriptRunner scriptRunner = new ScriptRunner(connection);
		Resources.setCharset(Charset.forName("UTF8"));
//		scriptRunner.setLogWriter(null);
		scriptRunner.runScript(new FileReader(new File("D:/test_transaction.sql")));
		scriptRunner.closeConnection();
		connection.close();
	}
}

```

```java
ScriptRunner runner = new ScriptRunner(dataSource.getConnection());
runner.setAutoCommit(true);
runner.setStopOnError(true);
runner.runScript(getResourceAsReader("Filename.sql"));
runner.closeConnection();
```


在代码里执行SQL
<https://mybatis.org/mybatis-3/zh/xref-test/org/apache/ibatis/submitted/bringrags/SimpleObjectTest.html>

```java
     ScriptRunner runner = new ScriptRunner(conn);
     runner.setLogWriter(null);
     runner.runScript(new StringReader("DROP TABLE IF EXISTS SimpleObject;"));
     runner.runScript(new StringReader("DROP TABLE IF EXISTS SimpleChildObject;"));
```


