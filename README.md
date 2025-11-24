声明: 本工具用于本人练习 并非给其他人使用 如选择使用有BUG修复可能不会很及时
## 介绍
在平时的开发中 如果在同一台机器上开发了两个Java应用之间需要通信 虽然可以考虑http但RPC显然是更好的选择
这个工具就是用于在同一台机器上不同Java进程之间的通信

## 服务注册方使用方式
```java
PublicClassUtil.publishClass(new UserServiceImpl());
```
若这个进程为第一个服务注册的进程 则此进程同样为注册中心

## 服务调用方使用方式
```java
UserService userService = RemoteClassFinder.create(UserService.class);
System.out.println(userService.getUserId());
```
像其他RPC框架一样 前后的接口需要是同一个
