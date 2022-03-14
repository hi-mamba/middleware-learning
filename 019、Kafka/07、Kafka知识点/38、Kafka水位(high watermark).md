
<https://www.cnblogs.com/huxi2b/p/7453543.html>

# Kafka水位(high watermark)

LEO：即日志末端位移(log end offset)，记录了该副本底层日志(log)中下一条消息的位移值。
注意是下一条消息！也就是说，如果LEO=10，那么表示该副本保存了10条消息，
位移值范围是[0, 9]。另外，leader LEO和follower LEO的更新是有区别的。我们后面会详细说

HW：即上面提到的`水位值`。对于同一个副本对象而言，其HW值`不会大于`LEO值。
`小于等于HW值`的所有消息都被认为是`“已备份”的（replicated）`。
同理，leader副本和follower副本的HW更新是有区别的



> consumer无法消费分区下leader副本中位移值大于分区HW的任何消息。
这里需要特别注意分区HW就是leader副本的HW值


## follower副本何时更新HW？

follower更新HW发生在其更新LEO之后，一旦`follower向log写完数据`，
它会`尝试更新`它自己的HW值。具体算法就是比较`当前LEO值`与FETCH响应中leader的HW值，
取两者的小者作为`新的HW值`。这告诉我们一个事实：如果follower的LEO值超过了leader的HW值，
那么follower HW值是不会越过leader HW值的。


