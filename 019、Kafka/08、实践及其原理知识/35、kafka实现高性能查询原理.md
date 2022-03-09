
<https://www.jianshu.com/p/255de4d3874b>

<https://tech.meituan.com/2015/01/13/kafka-fs-design-theory.html>

#  kafka实现高性能查询原理

kafka`分区`、`分段`、`稀疏索引`实现高性能查询

Kafka的Message存储采用了`分区(partition)`，`分段(LogSegment)`和`稀疏索引`这几个手段来达到了`高效性`。


## 


在partition中如何通过offset查找message
例如读取offset=368776的message，需要通过下面2个步骤查找。

第一步查找segment file 上述图2为例，其中00000000000000000000.index表示最开始的文件，起始偏移量(offset)为0.第二个文件00000000000000368769.index的消息量起始偏移量为368770 = 368769 + 1.同样，第三个文件00000000000000737337.index的起始偏移量为737338=737337 + 1，其他后续文件依次类推，以起始偏移量命名并排序这些文件，只要根据offset **二分查找**文件列表，就可以快速定位到具体文件。 当offset=368776时定位到00000000000000368769.index|log

第二步通过segment file查找message 通过第一步定位到segment file，当offset=368776时，依次定位到00000000000000368769.index的元数据物理位置和00000000000000368769.log的物理偏移地址，然后再通过00000000000000368769.log顺序查找直到offset=368776为止。

从上述图3可知这样做的优点，segment index file采取稀疏索引存储方式，它减少索引文件大小，通过mmap可以直接内存操作，稀疏索引为数据文件的每个对应message设置一个元数据指针,它比稠密索引节省了更多的存储空间，但查找起来需要消耗更多的时间。
