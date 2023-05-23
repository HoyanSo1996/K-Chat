# 多用户即时通讯系统

## 1. 用户登录

### a. client 端:

1. 创建 socket，连接服务端的 ServerSocket。

2. 使用 socket 获取 ObjectOutputStream，封装带有 user 信息的 message，使用 oos.write() 发送到服务端进行验证。

3. 使用 socket 获取 ObjectInputStream，获取服务端返回的带有 "登录验证 成功/失败" 标志的消息。

4. 如果信息标志为 "登录失败"：

   打印登录失败的消息，并返回操作菜单。

5. 如果信息标志为 "登录成功"： 

   5.1  创建一条线程 clientConnectServerThread，线程的成员变量放置 userId 和 与 server 端连接上的 socket，使用 socket 获取 ObjectInputStream，循环读取 server 端发送回来的信息。（相当于使用一条线程放置 "client 端连接 server 端" 的数据通道）

   5.2  将线程放置在一个 ConcurrentHashMap 集合中进行管理，key 为 userId，Value 为 clientConnectServerThread。

   

### b. server 端:

1. 创建 ServerSocket，等待用户端 socket 连接。

2. 当连接上后获得一个 socket，使用 socket 获取 ObjectInputStream，获取用户端发送过来的 user 信息并进行验证。

3. 如果验证失败：

   使用 socket 获取 ObjectOutputStream，并发送条 "登录失败" 标志的消息给 client 端。

4. 如果验证成功：

   4.1  创建一条线程 serverConnectClientThread，线程的成员变量放置 userId 和 与指定 client 端连接的 socket，使用 socket 获取 ObjectInputStream 循环读取 client 端发送过来的消息。（相当于使用一条线程放置 "server 端连接 client 端消息" 的数据通道）

   4.2  将 serverConnectClientThread 放置在一个 ConcurrentHashMap 集合中进行管理，key 为 client 端 userId，Value 为 serverConnectClientThread 。 

   4.3  使用 socket 获取 ObjectOutputStream，并发送一条带有 "登录成功" 标志的消息给 client 端。




## 2. 拉取在线用户列表
### a. client端:

1. 使用 socket 获取 ObjectOutputStream，发送一条带有 "请求在线用户列表" 标志的消息给 server 端。

2. 在  clientConnectServerThread 里设置判断条件，如果服务端返回带有 "请求在线用户列表" 标志的消息，将消息内容进行处理，然后打印出来。

   

### b. server 端:

1. 在 serverConnectClientThread 里设置判断条件，如果 client 端返回带有 "请求在线用户列表" 标志的消息。

   1.1  从 线程管理器集合 里获取当前所有线程的 userId，将其拼接起来封装为 message 内容。

   1.2  由于 serverConnectClientThread 里放置了与指定 client 端连接的 socket，所以使用 socket 获取 ObjectOutputStream 发送消息，接收方就是原来的 client 端。 




## 3. 无异常退出(用户端，服务器端)
### a. client 端:

1. 使用 socket 获取 ObjectOutputStream，发送一条带有 "用户登出" 标志的消息给 server 端。

2. 在 clientConnectServerThread 里设置判断条件，如果 server 端返回带有 "登出成功" 标志的消息，进行以下处理：

   2.1  关闭 client 端 socket。

   2.2  将 clientConnectServerThread 从 线程管理器集合 中移除。

3. 考虑服务器宕机的异常退出。由于 clientConnectServerThread 在读取消息时 处于阻塞状态，突然服务器socket 断开，会因为循环调用 ObjectInputStream.readObject() 而循环报 SocketException 异常，所以需要捕获到 SocketException，并进行以下处理：

   3.1  关闭 client 端 socket.

   3.2  将 clientConnectServerThread 从 线程管理器集合 中移除。

   3.3  打印服务端宕机 log。

   3.4  结束 clientConnectServerThread 里 readObject() 的循环。
   
   

### b. server 端:

1. 在 serverConnectClientThread 里设置判断条件，如果 client 端发送带有 "用户登出" 标志的消息，进行以下处理：

   1.1  关闭 server 端 socket。

   1.2  将 serverConnectClientThread 从 线程管理器集合 中移除。

2. 考虑用户端强制退出情况。由于 serverConnectClientThread 在读取消息时，处于阻塞状态，突然用户端socket 断开，会因为循环调用 ObjectInputStream.readObject() 而循环报 SocketException 异常，所以需要捕获到 SocketException，并进行以下处理：

   2.1  关闭 client 端 socket。

   2.2  将 serverConnectClientThread 从 线程管理器集合 中移除。

   2.3  打印 client 端异常退出 log。

   2.4  结束 serverConnectClientThread 里 readObject() 的循环。




## 4.私聊

### a. client 端

1. 设置一条 message，里面包含 sender id 和 receiver id。
2. 使用 socket 获取 ObjectOutputStream，发送一条消息给客户端带有 "用户登出" 标志的消息.



### b. server 端





## 5.群聊

### a. client 端





### b. server 端





## 6.发文件

### a. client 端





### b. server 端





## 7.服务器推送新闻

### a. client 端





### b. server 端