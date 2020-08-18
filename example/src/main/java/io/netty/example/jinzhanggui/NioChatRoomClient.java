package io.netty.example.jinzhanggui;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

/**
 * 基于nio的群聊系统
 * 先启动server,仔启动client
 */
public class NioChatRoomClient {
    private final String HOST = "127.0.0.1";
    private final int PORT = 6667;
    private Selector selector;
    private SocketChannel socketChannel;
    private String userName;

    public NioChatRoomClient(){
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(HOST, PORT);
            socketChannel.connect(inetSocketAddress);
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
            userName = socketChannel.getLocalAddress().toString().substring(1);
            System.out.println(userName + "is ok...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInfo(String info){
        info = userName + "说：" + info;
        try {
            socketChannel.write(ByteBuffer.wrap(info.getBytes()));
        } catch (IOException e) {
            System.out.println("客户端发送消息失败，server可能挂了，也有可能是网络异常");
        }
    }

    public void readInfo(){
        try {
            int readChannels = selector.select();
            if(readChannels > 0){
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    if(key.isReadable()){
                        SocketChannel channel = (SocketChannel)key.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        channel.read(byteBuffer);
                        System.out.println(new String(byteBuffer.array()));
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        NioChatRoomClient nioChatRoomClient = new NioChatRoomClient();
        new Thread(() -> {
            while (true){
                nioChatRoomClient.readInfo();
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()){
            String line = scanner.nextLine();
            nioChatRoomClient.sendInfo(line);
        }
    }

}
