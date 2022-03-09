
<https://www.jianshu.com/p/1c2596d2d235>

# offset在哪里维护？

在kafka中，提供了一个`consumer_offsets_* `的一个topic，
把offset信息写入到这个topic中。
`consumer_offsets`——按保存了每个consumer group某一时刻`提交的offset`信息。



