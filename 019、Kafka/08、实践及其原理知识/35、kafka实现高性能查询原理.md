
<https://www.jianshu.com/p/255de4d3874b>

<https://tech.meituan.com/2015/01/13/kafka-fs-design-theory.html>

#  kafka实现高性能查询原理

kafka`分区`、`分段`、`稀疏索引`实现高性能查询

Kafka的Message存储采用了`分区(partition)`，`分段(LogSegment)`和`稀疏索引`这几个手段来达到了`高效性`。


## partition中segment文件存储结构

- segment file组成：由2大部分组成，分别为`index file`和`data file`，
此2个文件一一对应，成对出现，后缀`”.index”`和`“.log”`分别表示为`segment索引文件`、`数据文件`.

- segment文件命名规则：partition全局的`第一个segment从0开始`，
后续每个segment文件名为上一个segment文件`最后一条消息的offset值`。
数值最大为64位long大小，19位数字字符长度，没有数字用0填充。

下面文件列表是笔者在Kafka broker上做的一个实验，创建一个`topicXXX`包含`1 partition`，
设置每个segment大小为500MB,并启动producer向Kafka broker写入大量数据,
如下图2所示segment文件列表形象说明了上述2个规则：





在partition中如何通过offset查找message
例如读取offset=368776的message，需要通过下面2个步骤查找。

第一步查找segment file 上述图2为例，其中00000000000000000000.index表示最开始的文件，起始偏移量(offset)为0.第二个文件00000000000000368769.index的消息量起始偏移量为368770 = 368769 + 1.同样，第三个文件00000000000000737337.index的起始偏移量为737338=737337 + 1，其他后续文件依次类推，以起始偏移量命名并排序这些文件，只要根据offset **二分查找**文件列表，就可以快速定位到具体文件。 当offset=368776时定位到00000000000000368769.index|log

第二步通过segment file查找message 通过第一步定位到segment file，当offset=368776时，依次定位到00000000000000368769.index的元数据物理位置和00000000000000368769.log的物理偏移地址，然后再通过00000000000000368769.log顺序查找直到offset=368776为止。

从上述图3可知这样做的优点，segment index file采取稀疏索引存储方式，它减少索引文件大小，通过mmap可以直接内存操作，稀疏索引为数据文件的每个对应message设置一个元数据指针,它比稠密索引节省了更多的存储空间，但查找起来需要消耗更多的时间。


`segment index file`采取`稀疏索引`存储方式，它减少索引文件大小，
通过mmap可以直接内存操作，`稀疏索引`为数据文件的每个对应message设置一个`元数据指针`,
它比稠密索引节省了更多的存储空间，但查找起来需要消耗更多的时间。
