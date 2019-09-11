

# 07、kafka 监控系统 Kafka Eagle 


## 安装

- 安装参考 https://docs.kafka-eagle.org/2.env-and-install/2.installing

- 注意事项，如果你使用mysql,记得修改配置文件,创建ke数据库.

- 创建数据库表 
>   mysql>CREATE DATABASE IF NOT EXISTS ke default charset utf8 COLLATE utf8_general_ci; 


## 遇到的问题

> kafka eagle The database has been closed


目前自己安装的mysql 服务器版本是 mysql-8.0.16,而由于ke.war 里面的mysql 驱动版本较低导致在连接mysql 的时候报错
> kafka eagle Could not create connection to database server

升级ke.war 的mysql 驱动就可以解决。

参考：https://stackoverflow.com/questions/50351956/unable-to-run-a-jdbc-source-connector-with-confluent-rest-api


## 如何替换 war 包里面的jar 文件

查看 war 里面的文件
> jar -tvf ke.war

删除版本低的mysql驱动 
>zip -d ke.war WEB-INF/lib/mysql-connector-java-5.1.30.jar

替换jar,替换之前先把war 给解压出来
> jar -xvf ke.war

复制mysql 新驱动到 lib文件夹里
> cp mysql-connector-java-8.0.17.jar WEB-INF/lib/

然后打包多个文件夹【当前ke.war不能删除,在解压的时候】
> jar -uvf ke.war WEB-INF/ && META-INF/ && media/

## 参考

[kafka eagle安装部署](https://www.jianshu.com/p/552ab3e23c96)

[Env & Install](https://docs.kafka-eagle.org/2.env-and-install/2.installing)

[Kafka监控系统Kafka Eagle剖析](https://www.cnblogs.com/smartloli/p/9371904.html)

[Unable to run a JDBC Source connector with Confluent REST API](https://stackoverflow.com/questions/50351956/unable-to-run-a-jdbc-source-connector-with-confluent-rest-api)

[Linux: Using zip/unzip to add, update, and remove files from a Java jar/war](https://fabianlee.org/2018/10/30/linux-using-zip-unzip-to-add-update-and-remove-files-from-a-java-jar-war/)

[Linux替换jar或war中的文件](https://blog.csdn.net/yyhjava/article/details/53895537)

