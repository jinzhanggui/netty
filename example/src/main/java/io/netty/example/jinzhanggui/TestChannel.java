package io.netty.example.jinzhanggui;


import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;


/**
 * 常见的channel是FileChannel SocketChannel ServerSocketChannel DatagramChannel(UDP读写)
 * transferFrom
 * flip
 * put
 * get
 * clear
 * array
 * size
 * BufferUnderflowException 通常发生在put的类型和get的类型不一致，和序列化一样，需要前后一致
 * ReadOnlyBufferException 通常在asReadOnlyBuffer方法之后又做了put操作
 */
public class TestChannel {

    /**
     * 将数据写到文件去
     */
    @Test
    public void testFileChannel() throws IOException {
        String temp = "你好，金掌柜";
        FileOutputStream fileOutputStream = new FileOutputStream("e:\\nio.txt");
        FileChannel fileOutputChannel = fileOutputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put(temp.getBytes());
        byteBuffer.flip();// 如果没有这一步，后面channel的write方法是无法将数据写进文件的
        fileOutputChannel.write(byteBuffer);
        fileOutputStream.close();
    }

    /**
     * 从文件中读取数据
     */
    @Test
    public void readFileByNio() throws IOException {
        File file = new File("e:\\nio.txt");
        FileInputStream fileInputStream = new FileInputStream(file);
        FileChannel fileInputStreamChannel = fileInputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate((int)file.length());
        int read = fileInputStreamChannel.read(byteBuffer);
        if(read != -1){
            System.out.println(new String(byteBuffer.array()));
        }
        fileInputStream.close();
    }

    /**
     * 将一个文件里面的内容读到另一个文件里面去，要求只能用一个ByteBuffer
     */
    @Test
    public void fileToFile() throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);

        FileInputStream fileInputStream = new FileInputStream("e:\\nio.txt");
        FileChannel fileInputStreamChannel = fileInputStream.getChannel();

        FileOutputStream fileOutputStream = new FileOutputStream("e:\\nio_copy.txt");
        FileChannel fileOutputStreamChannel = fileOutputStream.getChannel();
        int readNum = -1;
        do{
            readNum = fileInputStreamChannel.read(byteBuffer);
            if(readNum != -1){
                byteBuffer.flip();
                fileOutputStreamChannel.write(byteBuffer);
                byteBuffer.clear();//没有这句话会死循环，因为byteBuffer满了，下一次读到的数量就是0，然后出不来循环
            }
        }while (readNum != -1);
        fileOutputStream.close();
        fileInputStream.close();
    }

    /**
     * 复制文件可以用transferFrom的方法，更方便
     */
    @Test
    public void fileToFile_2() throws IOException {
        FileInputStream fileInputStream = new FileInputStream("e:\\nio.txt");
        FileChannel sourceChannel = fileInputStream.getChannel();

        FileOutputStream fileOutputStream = new FileOutputStream("e:\\nio_copy_2.txt");
        FileChannel destinationChannel = fileOutputStream.getChannel();

        destinationChannel.transferFrom(sourceChannel,0,sourceChannel.size());

        fileInputStream.close();
        fileOutputStream.close();
    }

    /**
     * 直接在堆外内存对文件进行修改，不用拷贝文件到堆内存里面，是操作系统级别的操作
     */
    @Test
    public void mappedByteBuffer() throws IOException {
        RandomAccessFile rwFile = new RandomAccessFile("e:\\map.txt", "rw");
        FileChannel rwFileChannel = rwFile.getChannel();
        MappedByteBuffer mappedByteBuffer = rwFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 5);
        mappedByteBuffer.put(0,(byte)'A');//这里是对buffer的读写即影响了文件，以前是对channel的读写才影响了文件
        rwFile.close();
    }

    /**
     * ByteBuffer数组的应用
     * 先运行这个，再运行testSocketChannel方法
     */
    @Test
    public void testServerSocketChannel() throws IOException {
        // 1. 打开通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(7000);
        // 2. 绑定端口
        serverSocketChannel.socket().bind(inetSocketAddress);

        ByteBuffer[] byteBuffers = new ByteBuffer[2];
        byteBuffers[0] = ByteBuffer.allocate(5);
        byteBuffers[1] = ByteBuffer.allocate(3);

        SocketChannel socketChannel = serverSocketChannel.accept();

        while (true){
            // 3. 获取socketChannel的数据
            long read = socketChannel.read(byteBuffers);
            if(read == -1){
                break;
            }

            // 4. 反转，准备输出
            Arrays.asList(byteBuffers).forEach(Buffer::flip);

            // 5. 输出，并复位
            Arrays.asList(byteBuffers).forEach(byteBuffer -> {
                System.out.println(new String(byteBuffer.array()));
                byteBuffer.clear();// 并不是将内容清空，只是复位，所以第二次输出的时候内容多了
            });
        }

        // 6. 关闭链接
        serverSocketChannel.close();
    }

    /**
     * 给ServerSocketChannel发送数据
     */
    @Test
    public void testSocketChannel() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(7000);
        socketChannel.connect(inetSocketAddress);
        ByteBuffer byteBuffer = ByteBuffer.allocate(9);
        byteBuffer.put((byte) '1');
        byteBuffer.put((byte) '2');
        byteBuffer.put((byte) 'a');
        byteBuffer.put((byte) 'a');
        byteBuffer.put((byte) 'a');
        byteBuffer.put((byte) 'a');
        byteBuffer.put((byte) 'a');
        byteBuffer.put((byte) 'b');
        byteBuffer.put((byte) 'c');// serverSocketChannel无法接收到第九个字符
        byteBuffer.flip();// 如果没有这句，下面的write方法将发送不了任何数据给serverSocketChannel
        socketChannel.write(byteBuffer);
        byteBuffer.clear();
        socketChannel.close();
    }


}
