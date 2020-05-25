1.比较sentinel和Hystrix的比较
对比内容	Sentinel	Hystrix
隔离策略	信号量隔离	线程池隔离/信号量隔离
熔断降级策略	基于响应时间或失败比率	基于失败比率
实时指标实现	滑动窗口	滑动窗口（基于 RxJava）
规则配置	支持多种数据源	支持多种数据源
扩展性	多个扩展点	插件的形式
基于注解的支持	支持	支持
限流	基于 QPS，支持基于调用关系的限流	不支持
流量整形	支持慢启动、匀速器模式	不支持
系统负载保护	支持	不支持
控制台	开箱即用，可配置规则、查看秒级监控、机器发现等	不完善
常见框架的适配	Servlet、Spring Cloud、Dubbo、gRPC 等	Servlet、Spring Cloud Netflix


2.基本架构
sentinel-core 核心模块，限流、降级、系统保护等都在这里实现
sentinel-dashboard 控制台模块，可以对连接上的sentinel客户端实现可视化的管理
sentinel-transport 传输模块，提供了基本的监控服务端和客户端的API接口，以及一些基于不同库的实现
sentinel-extension 扩展模块，主要对DataSource进行了部分扩展实现
sentinel-adapter 适配器模块，主要实现了对一些常见框架的适配
sentinel-demo 样例模块，可参考怎么使用sentinel进行限流、降级等
sentinel-benchmark 基准测试模块，对核心代码的精确性提供基准测试


3.
QPS和并发量
QPS（q） ：每秒处理的请求数量
并发量 （c）：同时支持多少个用户在线。与服务器的请求处理模型有关，如果是BIO模型，则并发量就受限于最大能支持多少个线程，如果是NIO模型，则并发量与socket连接数相关
平均响应时间（t）：单位为毫秒

他们之间的关系是 q = （1000/t）* c