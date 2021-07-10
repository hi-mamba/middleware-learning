[原文](https://www.cnblogs.com/sanduzxcvbnm/p/12858474.html)

# Logstash同步mysql数据到es

## 下载logstash <https://www.elastic.co/cn/logstash>

## 解压后进入logstash目录

## 新建导入的配置文件

> vim mysql-to-elasticsearch.conf

```conf
input {

  jdbc {
  jdbc_connection_string => "jdbc:mysql://127.0.0.1:3786/datacapture?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8&allowMultiQueries=true"
  jdbc_user => "root"
  jdbc_password => "123456"
  jdbc_driver_library => "/Users/hongyanma/repo/mysql/mysql-connector-java/5.1.46/mysql-connector-java-5.1.46.jar"
  jdbc_driver_class => "com.mysql.jdbc.Driver"
  jdbc_paging_enabled => "true"
  jdbc_page_size => "50000"
  jdbc_default_timezone => "Asia/Shanghai"
  parameters => { "id" => "1" }
  statement => "SELECT * FROM student WHERE id = :id"
  type => "jdbc"
  }
}

output {
  elasticsearch {
  hosts => "127.0.0.1:9200"
  # protocol => "http"
  index => "studet" # 指定索引名[表名称]
  document_type => "_doc"
  user => "elastic"
  password => "GmSjOkL8Pz8IwKJfWgLT"
  }

}
```

## 运行Logstash来加载我们的MySQL里的数据到Elasticsearch中

> ./bin/logstash --debug -f /data/mysql-to-elasticsearch.conf

## 说明：

1. jdbc_connection_string => "jdbc:mysql://192.168.0.145:
   3306/db_example?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC"
   连接的数据库地址，端口号，数据库名，字符编码，时区等,db_example为数据库名

2. 连接数据库使用的用户名和密码，根据自己的实际情况而定 jdbc_user => "root"
   jdbc_password => "root"


3. jdbc_driver_library 驱动包路径，若是在logstash指定目录下则留空，若不是则需要指定绝对路径

4. jdbc_driver_class 最新使用的驱动包类

5. parameters 设置一个参数 id，其值是 1 【如果是全表，那么去掉条件】

6. statement sql语句，结合上面的理解，是查询student 表数据表中条件 id 的值是 1 的数据

## 参考

<http://webcache.googleusercontent.com/search?q=cache:nuMJJ9qHfh8J:www.dreamwu.com/post-1195.html+&cd=5&hl=en&ct=clnk>
