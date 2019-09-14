## [](https://blog.whsir.com/post-1749.html)

# 修改gitlab仓库地址

## 编辑gitlab.yml文件
```bash
vim /opt/gitlab/embedded/service/gitlab-rails/config/gitlab.yml
```
将host: gitlab.example.com改成host: 192.168.0.80

```
  gitlab:
    ## Web server settings (note: host is the FQDN, do not include http://)
    host: 172.23.3.19
    # 注意这个端口和你 gitlab 配置的nginx 的端口要一样！！！
    port: 18081
    https: false
```

PS：当然你也可以把192.168.0.80改成你需要的域名

配置好后，重启gitlab
```bash
gitlab-ctl restart
```

刚重启好后，马上访问可能会出现502，耐心等个几秒刷新几次就好了。



如果遇到使用 ssh 提交不了，提示下面的。那么暂时我找不到解决的办法。我使用http的方式来提交
```bash
➜  mamba-forever-lakers git:(master) git push kobe8 master
git@172.23.3.19's password: 
fatal: 'learning/mamba-forever-lakers.git' does not appear to be a git repository
fatal: Could not read from remote repository.

Please make sure you have the correct access rights
and the repository exists.

```

http 的方式 用户名就是gitlab 的用户名，密码是gitlab这个用户名的密码

