package io.netty.example.jinzhanggui.zeroCopy;

import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 传统的io拷贝
 * 缓冲4096，拷贝1734548326字节文件时，大概10304毫秒，10-11秒
 */
public class ClassicCopy {


    @Test
    public void server() throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress("localhost",8888));
        Socket socket = serverSocket.accept();
        byte[] buffer = new byte[4096];
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        int total = 0;
        long start = System.currentTimeMillis();
        while (true){
            int read = dataInputStream.read(buffer);
            if(read == -1){
                break;
            }
            total += read;
        }
        long time = System.currentTimeMillis() - start;
        System.out.println("拷贝完毕,总字节大小：" + total + "时长：" + time);
        dataInputStream.close();
        socket.close();
        serverSocket.close();
    }


    @Test
    public void client() throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("localhost",8888));
        FileInputStream fileInputStream = new FileInputStream("e:\\journey_start.sql");
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        byte[] buffer = new byte[4096];
        while (true){
            int read = fileInputStream.read(buffer);
            if(read == -1){
                break;
            }
            dataOutputStream.write(buffer,0,read);
        }
        fileInputStream.close();
        dataOutputStream.close();
        socket.close();
    }


}
