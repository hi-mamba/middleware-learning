
<https://www.zhihu.com/question/443114952>

<https://blog.csdn.net/jack_shuai/article/details/109986355#:~:text=kafka%E7%9A%84%E6%AF%8F%E4%B8%AAtopic,%E6%95%B0%E9%87%8F%E5%8F%AF%E4%BB%A5%E9%9A%8F%E5%BF%83%E6%89%80%E6%AC%B2%E7%9A%84%E8%AE%BE%E7%BD%AE%E3%80%82>

# kafka如何选择合适的分区数？

> numPartitions = Tt / max(Tp, Tc)

根据某个topic日常"接收"的数据量等经验确定分区的`初始值`，
然后测试这个topic的producer吞吐量和consumer吞吐量。
假设它们的值分别是`Tp和Tc`，单位可以是MB/s。然后假设总的`目标吞吐量是Tt`，
那么numPartitions = Tt / max(Tp, Tc)

说明：
Tp表示`producer的吞吐量`。测试producer通常是很容易的，因为它的逻辑非常简单，就是直接发送消息到Kafka就好了。
Tc表示`consumer的吞吐量`。测试Tc通常与应用消费消息后进行什么处理的关系更大，相对复杂一些。


## 出现问题
一般来说如果有3个broker，那么至少就应该有3个分区.

不过根据数据量的写入量，磁盘io的消耗占比，网络带宽的承载能力，
我们可以适当增加每个broker的分区数，
可以用每台broker分区最小数的倍数进行设置并测试吞吐，例如3,6,9,12...

但总之不应调得过多，因为过多的分区数，会带来资源管理上的消耗，
清除日志时间变长，集群broker故障后分区leader重选时间变长，
客户端消费端线程数需求增加，甚至导致连接所需的socket消耗增加。

- 越多的分区需要打开更多的文件句柄
- 更多的分区会导致端对端的延迟
- 越多的partition意味着需要更多的内存
- 越多的partition会导致更长时间的恢复期


