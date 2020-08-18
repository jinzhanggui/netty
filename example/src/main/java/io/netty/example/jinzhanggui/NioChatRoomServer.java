package io.netty.example.jinzhanggui;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * 基于nio的群聊系统
 * 先启动server,仔启动client
 */
public class NioChatRoomServer {

    private Selector selector;
    private ServerSocketChannel listenChannel;
    private static final int PORT = 6667;

    public NioChatRoomServer(){
        try{
            // 1. 得到选择器
            selector = Selector.open();
            // 2. 开启通道
            listenChannel = ServerSocketChannel.open();
            // 3. 绑定端口
            listenChannel.socket().bind(new InetSocketAddress(PORT));
            // 4. 非阻塞模式
            listenChannel.configureBlocking(false);
            // 5. serverSocket注册到selector
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void listen(){
        try{
            while (true){
                int count = selector.select(2000);
                if(count > 0){
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()){
                        SelectionKey key = iterator.next();
                        if(key.isAcceptable()){// 接收客户端的连接
                            SocketChannel sc = listenChannel.accept();
                            sc.configureBlocking(false);// 忘记加会抛异常IllegalBlockingModeException
                            // 注册
                            sc.register(selector,SelectionKey.OP_READ);
                            // 提示
                            System.out.println(sc.getRemoteAddress() + "上线");
                        }
                        if(key.isReadable()){// 读取客户端的消息
                            readData(key);
                        }
                        iterator.remove();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {

        }
    }

    private void readData(SelectionKey key){
        SocketChannel channel = null;
        try {
            channel = (SocketChannel)key.channel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            int count = channel.read(byteBuffer);
            if(count > 0){
                String msg = new String(byteBuffer.array());
                System.out.println("from 客户端： " + msg);
                // 向其他客户端转发消息(记得排除自己)
                sendInfoToOtherClients(msg,channel);
            }
        } catch (IOException e) {
            try {
                System.out.println(channel.getRemoteAddress() + "离线了...");
                key.cancel();
                channel.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendInfoToOtherClients(String msg, SocketChannel self){
        for(SelectionKey key : selector.keys()){
            Channel targetChannel = key.channel();
            if(targetChannel instanceof SocketChannel && targetChannel != self){
                SocketChannel dest = (SocketChannel) targetChannel;
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                try {
                    dest.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        NioChatRoomServer nioChatRoomServer = new NioChatRoomServer();
        nioChatRoomServer.listen();
    }

}
