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




## 4. 私聊

### a. client 端

1. 封装一条 message，里面包含 sender id 和 receiver id，"私聊" 标志。使用 socket 获取 ObjectOutputStream，将消息发送给 server 端。

2. 在 clientConnectServerThread 中设置判断消息

   2.1  如果读取到带有 "私聊成功" 标志的消息，receiver 视角：打印 [timeStamp] 成功接收 sender 消息。

   2.2  如果读取到带有 "私聊失败" 标志的消息，sender 视角：打印 [timeStamp]  receiver 接收消息失败，原因为：不在线/用户不存在/...



### b. server 端

1. 在 serverConnectClientThread 里设置判断条件，如果接收到 client 端发送带有 "私聊" 标志的消息，进行下一步

2. 从消息里取出 receiver id，然后去 线程管理器中 查找是否存对应的  serverConnectClientThread。

   2.1  如果 thread 不存在，证明用户 不在线/不存在，封装一条带有 "私聊失败" 的消息返回给原 client。

   2.2  如果 thread 存在，则从该 thread 中获取 socket 再获取 ObjectOutputStream，为消息设置 "私聊成功" 标志，使用这个 oos 去发送消息，这样就能实现向目标 client 发送消息的功能。



## 5. 群聊

### a. client 端

1. 封装一条 message，里面包含 "群聊" 标志。使用 socket 获取 ObjectOutputStream，将消息发送给 server 端。

2. 在 clientConnectServerThread 中设置判断消息：

   如果读取到带有 "群聊成功" 标志的消息，receiver 视角：打印 [timeStamp] 成功接收 sender 的群发消息。



### b. server 端

1. 在 serverConnectClientThread 中设置判断条件，如果接收到 client 端发送带有 "群聊" 标志的消息，进行下一步。
2. 遍历取出 线程管理器 中的所有 serverConnectClientThread，取出其 socket，再获取对应的 ObjectOutputStream，为消息设置 "群聊成功" 标志，再将消息发送出去。



## 6. 发文件

### a. client 端

1. 创建 FileInputStream，从磁盘中读取文件到一个字节数组 FileBytes 中，记录下数组长度 FileLen，将这两个参数封装到消息中。然后关闭 FileInputStream。

2. 在消息里设置 "发送文件" 标志。使用 socket 获取 ObjectOutputStream，将消息发送给 server 端。

3. 在 clientConnectServerThread 中设置判断消息，如果读取带有 "发送文件成功" 标志的消息，进行以下操作：

   3.1  创建 FileOutputStream，使用消息里的参数 FileBytes 和 FileLen，将文件写出到磁盘中。

   3.2  receiver 视角：打印 [timeStamp] 成功接收 sender 的文件。

   3.3  关闭 FileOutputStream。

4. 在 clientConnectServerThread 中设置判断消息，如果读取带有 "发送文件失败" 标志的消息，receiver 视角：打印 [timeStamp]  receiver 接收消息失败，原因为：不在线/用户不存在/...



### b. server 端

1. 在 serverConnectClientThread 中设置判断条件，如果接收到 client 端发送带有 "发送文件" 标志的消息，进行下一步。

2. 从消息里取出 receiver id，然后去 线程管理器中 查找是否存对应的  serverConnectClientThread。

   2.1  如果 thread 不存在，证明用户 不在线/不存在，封装一条带有 "发送文件失败" 的消息返回给原 client。

   2.2  如果 thread 存在，则从该 thread 中获取 socket 再获取 ObjectOutputStream，为消息设置 "发送文件成功" 标志，使用这个 oos 去发送消息，这样就能实现向目标 client 发送消息的功能。



### c. 优化

​		上述操作传输的文件大小不超过 2^31 Byte, 即 2GB，如果超过，就需要新建线程，使用边读边写的方式来传输文件了。



## 7. 服务器推送新闻

### server 端

1. server 端的 serverSocket 在连接上 client 端的 socket 后，就创建一条 serverSendMsgThread。
2. 在 serverSendMsgThread 中，设置循环读取需要发送的消息。
3. 封装消息，设置 sender 为 "管理员"。
4. 遍历取出 线程管理器 中的所有 serverConnectClientThread，取出其 socket，再获取对应的 ObjectOutputStream，为消息设置 "群聊成功" 标志，再将消息发送出去。（client 端会通过 "群聊" 的逻辑实现 读取新闻的功能）





## 8. 离线发送消息思路

### server 端

1. 创建一个 ConcurrentHashMap<String, ArrayList<Message>> 集合 messageMap，用来存放离线 message，key 为 receiver id，value 为 ArrayList 集合，集合可以存放多条 message 信息。
2. 在 serverConnectClientThread 中的 群发消息 和 私聊消息 条件中，先使用 线程管理器 判断用户是否在线，如果不在线，就将消息存放入 messageMap 中。
3. 在 serverConnectClientThread 中的 登录请求 条件中，当用户登录成功后，就去 messageMap 中读取离线消息，然后设置 “私聊/群聊" 标志，发送消息给对应的 client 端。



### c. tips

​	条件允许就用 database 来实现消息的存储功能。