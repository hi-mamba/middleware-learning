## [CentOS 7 下 GitLab安装部署教程](https://ken.io/note/centos7-gitlab-install-tutorial)

## [centos7安装gitlab](https://juejin.im/post/5c00b1546fb9a049be5d364d)

## CentOS 7 下 GitLab安装部署教程

## 部署过程
本次我们部署的是社区版:gitlab-ce，如果要部署商业版可以把关键字替换为：gitlab-ee

### 1、Yum安装GitLab
添加GitLab社区版Package
```bash
curl https://packages.gitlab.com/install/repositories/gitlab/gitlab-ce/script.rpm.sh | sudo bash
```

#### 安装GitLab社区版
```bash
sudo yum install -y gitlab-ce
```
安装成功后会看到gitlab-ce打印了以下图形

```
依赖关系解决

==========================================================================================================
 Package               架构               版本                         源                            大小
==========================================================================================================
正在安装:
 gitlab-ee             x86_64             12.2.4-ee.0.el7              gitlab_gitlab-ee             680 M

事务概要
==========================================================================================================
安装  1 软件包

总下载量：680 M
安装大小：1.6 G
Downloading packages:
警告：/var/cache/yum/x86_64/7/gitlab_gitlab-ee/packages/gitlab-ee-12.2.4-ee.0.el7.x86_64.rpm: 头V4 RSA/SHA1 Signature, 密钥 ID f27eab47: NOKEY
gitlab-ee-12.2.4-ee.0.el7.x86_64.rpm 的公钥尚未安装
gitlab-ee-12.2.4-ee.0.el7.x86_64.rpm                                               | 680 MB  00:05:09
从 https://packages.gitlab.com/gitlab/gitlab-ee/gpgkey 检索密钥
导入 GPG key 0xE15E78F4:
 用户ID     : "GitLab B.V. (package repository signing key) <packages@gitlab.com>"
 指纹       : 1a4c 919d b987 d435 9396 38b9 1421 9a96 e15e 78f4
 来自       : https://packages.gitlab.com/gitlab/gitlab-ee/gpgkey
从 https://packages.gitlab.com/gitlab/gitlab-ee/gpgkey/gitlab-gitlab-ee-3D645A26AB9FBD22.pub.gpg 检索密钥
导入 GPG key 0xF27EAB47:
 用户ID     : "GitLab, Inc. <support@gitlab.com>"
 指纹       : dbef 8977 4ddb 9eb3 7d9f c3a0 3cfc f9ba f27e ab47
 来自       : https://packages.gitlab.com/gitlab/gitlab-ee/gpgkey/gitlab-gitlab-ee-3D645A26AB9FBD22.pub.gpg
Running transaction check
Running transaction test
Transaction test succeeded
Running transaction
  正在安装    : gitlab-ee-12.2.4-ee.0.el7.x86_64                                                      1/1
It looks like GitLab has not been configured yet; skipping the upgrade script.

       *.                  *.
      ***                 ***
     *****               *****
    .******             *******
    ********            ********
   ,,,,,,,,,***********,,,,,,,,,
  ,,,,,,,,,,,*********,,,,,,,,,,,
  .,,,,,,,,,,,*******,,,,,,,,,,,,
      ,,,,,,,,,*****,,,,,,,,,.

```

### 2、配置GitLab站点Url
GitLab默认的配置文件路径是

> /etc/gitlab/gitlab.rb

默认的站点Url配置项是：
> external_url 'http://gitlab.example.com'

这里我将GitLab站点Url修改为
> http://192.168.1.1

也可以用域名代替IP，这里根据自己需求来即可

- 修改配置文件
sudo vi /etc/gitlab/gitlab.rb

#配置首页地址（大约在第15行）
> external_url 'http://192.168.1.1'

### 3、启动并访问GitLab

启动GitLab
- 重新配置并启动
```bash
sudo gitlab-ctl reconfigure
```
> 可能花费几分钟...

- 完成后将会看到如下输出
```
Running handlers complete
Chef Client finished, 432/613 resources updated in 03 minutes 43 seconds
gitlab Reconfigured!
```

访问GitLab

如果访问出现502，可能的问题 1、端口被占用。2、权限问题【我使用root 账号执行导致权限问题】 

PS:如果遇到问题可以使用 `gitlab-ctl tail unicorn`  来跟踪unicorn的状态.

> 查看日志   
>  gitlab-ctl restart sidekiq   
>  gitlab-ctl status

##  修改gitlab默认端口号

- 修改文件 修改 /etc/gitlab/gitlab.rb 文件如下，然后执行重新配置，重启命令后完成。
```bash
[root@localhost gitlab]# vim /etc/gitlab/gitlab.rb
```
修改的内容如下
```xml
external_url 'http://172.23.3.19'
nginx['listen_port'] = 18081
unicorn['port'] = 18082
```

- 修改 gitlab 自带nginx配置 /var/opt/gitlab/nginx/conf/gitlab-http.conf
```nginx
server {
  listen *:18081;
  ...
 }
```
修改接听端口就可以了。。。然后重启gitlab
```bash
[root@localhost gitlab]# gitlab-ctl reconfigure

[root@localhost gitlab]# gitlab-ctl restart
```

## 权限问题

如果是权限问题，那么需要赋予权限，然后重启.

```bash

[root@localhost gitlab]# chmod -R 777 /var/log/gitlab

[root@localhost gitlab]# gitlab-ctl tail unicorn
```
然后重启

```bash
[root@localhost gitlab]# gitlab-ctl restart
```


## 访问gitlab web

http://公网ip:18081/

用户名：root 密码：首次访问会要求更改密码，把密码更改下即可。


## Gitlab常用命令

```bash
sudo gitlab-ctl reconfigure 当修改了gitlab.rb需要重置配置才可生效

sudo gitlab-ctl restart 重新启动gitlab服务

sudo gitlab-ctl start 启动gitlab服务

sudo gitlab-ctl stop 停止gitlab服务

sudo gitlab-ctl status 查看gitlab服务状态

sudo gitlab-ctl tail 查看所有日志

sudo gitlab-ctl tail postgresql 查看postgresql日志
```
## 参考

[gitlab服务器502恢复过程](https://blog.51cto.com/chenhao6/2089727)

[我所遇到的GitLab 502问题的解决](https://blog.csdn.net/wangxicoding/article/details/43738137)

[How to restart GitLab](https://docs.gitlab.com/ee/administration/restart_gitlab.html)

[centos安装Gitlab及常见问题](https://www.xiejiahe.com/blog/detail/5c3752b211e9b8380aaaa4b1)






