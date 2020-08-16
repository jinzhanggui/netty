package io.netty.example.jinzhanggui;


import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


/**
 * 常见的channel是FileChannel SocketChannel ServerSocketChannel DatagramChannel(UDP读写)
 * transferFrom
 * flip
 * put
 * get
 * clear
 * array
 * size
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
     * @throws IOException
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


}
