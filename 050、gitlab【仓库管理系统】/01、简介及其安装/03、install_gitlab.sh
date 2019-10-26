#!/bin/bash
# ps 安装社区版本
echo "##### 安装社区版本 ...需要 ROOT 账号执行  #####"

GIT_LAB_PORT=8780

rm -f /etc/yum.repos.d/*

curl https://packages.gitlab.com/install/repositories/gitlab/gitlab-ce/script.rpm.sh | sudo bash

wget -O /etc/yum.repos.d/CentOS-Base.repo http://mirrors.aliyun.com/repo/Centos-7.repo
yum clean all

echo "#### # 配置镜像完成 #####"
cat /etc/yum.repos.d/gitlab-ce.repo

LOCAL_IP=$(ip addr | grep 'state UP' -A2 | tail -n1 | awk '{print $2}' | cut -f1 -d '/')
echo "ip 地址：$LOCAL_IP"

yum install -y gitlab-ce

gitlab-ctl reconfigure

sed -i "s#external_url 'http://gitlab.example.com'#external_url 'http://${LOCAL_IP}'#" /etc/gitlab/gitlab.rb

# 中括号需要转义
sed -i "s?# nginx\['listen_port'\] = nil?nginx['listen_port'] = ${GIT_LAB_PORT}?" /etc/gitlab/gitlab.rb

sed -i "s?# unicorn\['port'\] = 8080?unicorn['port'] = ${GIT_LAB_PORT}1?" /etc/gitlab/gitlab.rb

sed -i "s#:80;#:${GIT_LAB_PORT};#" /var/opt/gitlab/nginx/conf/gitlab-http.conf

chmod -R 777 /var/log/gitlab
gitlab-ctl tail unicorn
echo "##### 重新配置并启动 #####"

gitlab-ctl reconfigure

gitlab-ctl restart

echo " 执行安装完成 端口: "${GIT_LAB_PORT}
