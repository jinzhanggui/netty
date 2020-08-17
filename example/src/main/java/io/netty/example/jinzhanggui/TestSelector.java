package io.netty.example.jinzhanggui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class TestSelector {

    /**
     * 启动完这个方法后，继续启动openSocketChannel发送数据
     */
    @Test
    public void openServerSocketChannel() throws IOException {

        // 得到一个selector
        Selector selector = Selector.open();

        // 开启serverSocketChannel并绑定端口6666
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(6666));

        // 设置非阻塞
        serverSocketChannel.configureBlocking(false);

        // 将serverSocketChannel注册到selector，selector关注该serverSocketChannel上的OP_ACCEPT事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // 循环等待客户端连接
        while (true){
            if(selector.select(1000) == 0){ // selector阻塞等待1秒，如果1秒内没有连接事件发生，则进入if块
                //System.out.println("服务器等待了1秒，无连接");
                continue;
            }

            // 承上的if,即发生了连接事件
            // 此时可以通过selectionKeys反向获取到通道
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
            while (keyIterator.hasNext()){
                // 获取到SelectionKey
                SelectionKey key = keyIterator.next();            // 为什么叫next方法这么奇怪？
                if(key.isAcceptable()){// OP_ACCEPT
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    // 将socketChannel注册到selector,关联事件OP_READ,同时给该socketChannel关联一个Buffer
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }
                if(key.isReadable()){  // OP_READ
                    // 通过key反向获取对应的channel
                    SocketChannel channel = (SocketChannel)key.channel();
                    //获取到该channel关联的buffer,也即我们刚刚在上面分配的ByteBuffer
                    ByteBuffer buffer = (ByteBuffer)key.attachment();
                    channel.read(buffer);
                    System.out.println("from 客户端" + new String(buffer.array()));
                }
                // 手动从集合中移动当前的selectionKey，防止重复操作
                keyIterator.remove();
            }

        }

    }


    /**
     * 连接上面的serverSocketChannel
     */
    @Test
    public void openSocketChannel() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 6666);
        // 连接服务器
        if(!socketChannel.connect(inetSocketAddress)){
            while (!socketChannel.finishConnect()){
                System.out.println("因为连接需要事件，客户端不会阻塞，可以做其他事情");
            }
        }
        // 连接成功，发送数据
        String str = "你好，金掌柜";
        ByteBuffer byteBuffer = ByteBuffer.wrap(str.getBytes());// 该方法不需要指定byteBuffer的大小，由内容自身决定大小，不多不少
        // 发送数据
        socketChannel.write(byteBuffer);
        System.in.read();
    }

}
