

# JMS协议介绍

JMS（Java Messaging Service）是Java平台上有关面向消息中间件的技术规范，
它便于消息系统中的Java应用程序进行消息交换,并且通过提供标准的产生、发送、接收消息的接口简化企业应用的开发。

JMS本身只定义了一系列的接口规范，是一种与厂商无关的 API，用来访问消息收发系统。
它类似于 JDBC(Java Database Connectivity)：这里，JDBC 是可以用来访问许多不同关系数据库的 API，
而 JMS 则提供同样与厂商无关的访问方法，以访问消息收发服务。

许多厂商目前都支持 JMS，包括 IBM 的 MQSeries、BEA的 Weblogic JMS service和 Progress 的 SonicMQ，这只是几个例子。 
JMS 使您能够通过消息收发服务（有时称为消息中介程序或路由器）从一个 JMS 客户机向另一个 JML 客户机发送消息。
消息是 JMS 中的一种类型对象，由两部分组成：报头和消息主体。报头由路由信息以及有关该消息的元数据组成。

消息主体则携带着应用程序的数据或有效负载。
根据有效负载 的类型来划分，可以将消息分为几种类型，它们分别携带：简单文本 (TextMessage)、
可序列化的对象 (ObjectMessage)、属性集合 (MapMessage)、字节流 (BytesMessage)、
原始值流 (StreamMessage)，还有无有效负载的消息 (Message)。

