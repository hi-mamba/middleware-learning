
<https://www.cnblogs.com/jpfss/p/10242955.html>

# nginx解决超长请求串(413 request Entity too Large错误解决办法)

nginx作为反向代理服务器，小文件上传可以，大文件上传会报413,400，414(如 413 request Entity too Large)等状态码，这是因为请求长度超过了nginx默认的缓存大小和最大客户端最大请求大小。


## 针对post请求解决办法：
```
修改nginx.conf里面的几个相关的配置参数
client_body_buffer_size 10m(配置请求体缓存区大小, 不配的话)
client_max_body_size 20m(设置客户端请求体最大值)
client_body_temp_path /data/temp (设置临时文件存放路径。只有当上传的请求体超出缓存区大小时，才会写到临时文件中,注意临时路径要有写入权限)
```

如果上传文件大小超过client_max_body_size时，会报413 entity too large的错误。


## 针对get请求解决办法：

针对get请求，我们可以通过修改另外两个配置来解决请求串超长的问题：
client_header_buffer_size 语法：client_header_buffer_size size 默认值：1k 使用字段：http, server 这个指令指定客户端请求的http头部缓冲区大小绝大多数情况下一个头部请求的大小不会大于1k不过如果有 来自于wap客户端的较大的cookie它可能会大于1k，Nginx将分配给它一个更大的缓冲区，这个值可以在 large_client_header_buffers里面设置。 large_client_header_buffers 语法：large_client_header_buffers number size 默认值：large_client_header_buffers 4 4k/8k 使用字段：http, server 指令指定客户端请求的一些比较大的头文件到缓冲区的最大值，如果一个请求的URI大小超过这个值，服务 器将返回一个"Request URI too large" (414)，同样，如果一个请求的头部字段大于这个值，服务器 将返回"Bad request" (400)。 缓冲区根据需求的不同是分开的。 默认一个缓冲区大小为操作系统中分页文件大小，通常是4k或8k，如果一个连接请求将状态转换为 keep-alive，这个缓冲区将被释放。


那么有人就会觉得奇怪了，为什么修改http header的大小就能解决get请求串过长的问题呢， 这就要从http协议的get请求说起了，其实GET提交，请求的数据会附在URL之后（就是把数据放置在 HTTP协议头中）。

大家看到没，其实get请求的参数就是存放在http header中的，所以修改header的大小限制 当然可以解决请求串过长的问题啦。此外还有给大家澄清一点啦，HTTP协议没有对传输的数据大小进行限 制，HTTP协议规范也没有对URL长度进行限制，我们日常生活中遇到的长度限制都是各个浏览器或者http 请求工具自己干的。

