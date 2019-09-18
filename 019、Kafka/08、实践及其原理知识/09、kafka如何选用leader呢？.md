## [原文](https://juejin.im/post/5c46e729e51d452c8e6d5679)

# kafka如何选用leader呢?

选举leader常用的方法是多数选举法，比如Redis等，但是kafka没有选用多数选举法，kafka采用的是quorum（法定人数）。

quorum是一种在分布式系统中常用的算法，主要用来通过数据冗余来保证数据一致性的投票算法。
在kafka中该算法的实现就是ISR，在ISR中就是可以被选举为leader的法定人数。

- 在leader宕机后，只能从ISR列表中选取新的leader，无论ISR中哪个副本被选为新的leader，它都知道HW之前的数据，
可以保证在切换了leader后，消费者可以继续看到HW之前已经提交的数据。

- HW的截断机制：选出了新的leader，而新的leader并不能保证已经完全同步了之前leader的所有数据，
只能保证HW之前的数据是同步过的，此时所有的follower都要将数据截断到HW的位置，再和新的leader同步数据，来保证数据一致。
当宕机的leader恢复，发现新的leader中的数据和自己持有的数据不一致，
此时宕机的leader会将自己的数据截断到宕机之前的hw位置，然后同步新leader的数据。
宕机的leader活过来也像follower一样同步数据，来保证数据的一致性。
 

## [ISR是如何实现同步的呢]()

## [如果ISR内的副本挂掉怎么办？]()

 