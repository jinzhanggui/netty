package io.netty.example.jinzhanggui.zeroCopy;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * 和传统io拷贝做对比
 * 去他妈的，更慢了，1734548326需要17847毫秒
 * 换了一个小的文件，也是这样
 * */
public class NioCopy {

    @Test
    public void server() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress("localhost",8888));
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        SocketChannel socketChannel = serverSocketChannel.accept();
        int total = 0;
        long start = System.currentTimeMillis();
        while (true){
            int read = socketChannel.read(buffer);
            if(read == -1){
                break;
            }
            total += read;
            buffer.clear();// 不加这一句，因为文件大于4096，需要多次读取，而第二次读的时候因为buffer没地方写了，read=0,无法退出循环
//            System.out.println(total);
        }
        long time = System.currentTimeMillis() - start;
        System.out.println("拷贝完毕,总字节大小：" + total + "时长：" + time);
        socketChannel.close();
        serverSocketChannel.close();
    }

    @Test
    public void client() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 8888));
        FileChannel fileChannel = new FileInputStream("e:\\journey_start.sql").getChannel();
        // transferTo底层用了0拷贝技术，为了和ClassicCopy做对比，此处需要手动计算发送次数
        // 另外，在linux下用transferTo可以直接发送一个大文件，但window默认只发送8m,需要多次发送
        int count = (int)(fileChannel.size() / 4096L);
        int mod = (int)(fileChannel.size() % 4096L);
        for(int i = 0; i < count; i++){
            fileChannel.transferTo(i*4096,4096,socketChannel);
        }
        if(mod > 0){
            fileChannel.transferTo(count*4096,mod,socketChannel);
        }
        fileChannel.close();
        socketChannel.close();
    }

}
