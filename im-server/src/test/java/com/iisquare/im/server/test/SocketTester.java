package com.iisquare.im.server.test;

import org.junit.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;

public class SocketTester {

    @Test
    public void blockClientTest() throws IOException {
        Socket client = new Socket("127.0.0.1", 8888);
        System.out.println("远程主机地址：" + client.getRemoteSocketAddress());
        OutputStream outToServer = client.getOutputStream();
        DataOutputStream out = new DataOutputStream(outToServer);

        out.writeUTF("Hello from " + client.getLocalSocketAddress());
        InputStream inFromServer = client.getInputStream();
        DataInputStream in = new DataInputStream(inFromServer);
        System.out.println("服务器响应： " + in.readUTF());
        client.close();
    }

    @Test
    public void blockServerTest() throws IOException {
        ServerSocket serverSocket = new ServerSocket(8888);
        serverSocket.setSoTimeout(10000);
        Socket server = serverSocket.accept();
        System.out.println("远程主机地址：" + server.getRemoteSocketAddress());
        DataInputStream in = new DataInputStream(server.getInputStream());
        System.out.println(in.readUTF());
        DataOutputStream out = new DataOutputStream(server.getOutputStream());
        out.writeUTF("谢谢连接我：" + server.getLocalSocketAddress() + "\nGoodbye!");
        server.close();
    }

    @Test
    public void nioClientTest() throws IOException {
        // 创建一个信道，并设为非阻塞模式
        SocketChannel clntChan = SocketChannel.open();
        clntChan.configureBlocking(false);
        // 向服务端发起连接
        if (!clntChan.connect(new InetSocketAddress("127.0.0.1", 8888))){
            // 不断地轮询连接状态，直到完成连接
            while (!clntChan.finishConnect()){
                // 在等待连接的时间里，可以执行其他任务，以充分发挥非阻塞IO的异步特性
                // 这里为了演示该方法的使用，只是一直打印"."
                System.out.print(".");
            }
        }
        // 为了与后面打印的"."区别开来，这里输出换行符
        System.out.print("\n");
        // 分别实例化用来读写的缓冲区
        byte[] argument = "test for nio".getBytes();
        ByteBuffer writeBuf = ByteBuffer.wrap(argument);
        ByteBuffer readBuf = ByteBuffer.allocate(argument.length);
        // 接收到的总的字节数
        int totalBytesRcvd = 0;
        // 每一次调用read（）方法接收到的字节数
        int bytesRcvd;
        // 循环执行，直到接收到的字节数与发送的字符串的字节数相等
        while (totalBytesRcvd < argument.length){
            // 如果用来向通道中写数据的缓冲区中还有剩余的字节，则继续将数据写入信道
            if (writeBuf.hasRemaining()){
                clntChan.write(writeBuf);
            }
            // 如果read（）接收到-1，表明服务端关闭，抛出异常
            if ((bytesRcvd = clntChan.read(readBuf)) == -1){
                throw new SocketException("Connection closed prematurely");
            }
            // 计算接收到的总字节数
            totalBytesRcvd += bytesRcvd;
            // 在等待通信完成的过程中，程序可以执行其他任务，以体现非阻塞IO的异步特性
            // 这里为了演示该方法的使用，同样只是一直打印"."
            System.out.print(".");
        }
        // 打印出接收到的数据
        System.out.println("Received: " +  new String(readBuf.array(), 0, totalBytesRcvd));
        // 关闭信道
        clntChan.close();
    }

    @Test
    public void nioServerTest() throws IOException {
        // 创建一个选择器
        Selector selector = Selector.open();
        for (String arg : Arrays.asList("8888")){
            // 实例化一个信道
            ServerSocketChannel listnChannel = ServerSocketChannel.open();
            // 将该信道绑定到指定端口
            listnChannel.socket().bind(new InetSocketAddress(Integer.parseInt(arg)));
            // 配置信道为非阻塞模式
            listnChannel.configureBlocking(false);
            // 将选择器注册到各个信道
            listnChannel.register(selector, SelectionKey.OP_ACCEPT);
        }
        // 不断轮询select方法，获取准备好的信道所关联的Key集
        while (true){
            // 一直等待,直至有信道准备好了I/O操作
            if (selector.select(3000) == 0){
                // 在等待信道准备的同时，也可以异步地执行其他任务，
                // 这里只是简单地打印"."
                System.out.print(".");
                continue;
            }
            // 获取准备好的信道所关联的Key集合的iterator实例
            Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
            // 循环取得集合中的每个键值
            while (keyIter.hasNext()){
                SelectionKey key = keyIter.next();
                // 如果服务端信道感兴趣的I/O操作为accept
                if (key.isAcceptable()){
                    SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
                    clntChan.configureBlocking(false);
                    // 将选择器注册到连接到的客户端信道，并指定该信道key值的属性为OP_READ，同时为该信道指定关联的附件
                    clntChan.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(256));
                }
                // 如果客户端信道感兴趣的I/O操作为read
                if (key.isReadable()){
                    SocketChannel clntChan = (SocketChannel) key.channel();
                    // 获取该信道所关联的附件，这里为缓冲区
                    ByteBuffer buf = (ByteBuffer) key.attachment();
                    long bytesRead = clntChan.read(buf);
                    // 如果read（）方法返回-1，说明客户端关闭了连接，那么客户端已经接收到了与自己发送字节数相等的数据，可以安全地关闭
                    if (bytesRead == -1){
                        clntChan.close();
                    } else if (bytesRead > 0){
                        // 如果缓冲区总读入了数据，则将该信道感兴趣的操作设置为为可读可写
                        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    }
                }
                // 如果该键值有效，并且其对应的客户端信道感兴趣的I/O操作为write
                if (key.isValid() && key.isWritable()) {
                    // 获取与该信道关联的缓冲区，里面有之前读取到的数据
                    ByteBuffer buf = (ByteBuffer) key.attachment();
                    // 重置缓冲区，准备将数据写入信道
                    buf.flip();
                    SocketChannel clntChan = (SocketChannel) key.channel();
                    // 将数据写入到信道中
                    clntChan.write(buf);
                    if (!buf.hasRemaining()){
                        // 如果缓冲区中的数据已经全部写入了信道，则将该信道感兴趣的操作设置为可读
                        key.interestOps(SelectionKey.OP_READ);
                    }
                    // 为读入更多的数据腾出空间
                    buf.compact();
                }
                // 这里需要手动从键集中移除当前的key
                keyIter.remove();
            }
        }
    }

}
