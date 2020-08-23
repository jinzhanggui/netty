package io.netty.example.jinzhanggui.simple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * 说明
 * 1. 自定义一个Handler需要继承netty规定好的某个HandlerAdapter
 * 2. 这时我们自定义一个Handler,才能称为一个Handler
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 读取数据（这里我们可以读取客户端发送的消息）
     * 1. ChannelHandlerContext ctx : 上下文对象，含有 管道pipeline ，通道channel，地址
     * 2. Object msg : 就是客户端发送的数据 默认Object
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("当前线程：" + Thread.currentThread().getName());
        System.out.println("server ctx = " + ctx);
        // 将 msg 转成一个 ByteBuf
        // 这个ByteBuf 是 Netty 提供的，不是NIO 的 ByteBuffer，且ByteBuf性能更高
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("客户端发送消息是：" + buf.toString(CharsetUtil.UTF_8));
        System.out.println("客户端地址：" + ctx.channel().remoteAddress());
    }


    // 数据读取完毕，用于给客户端发送消息
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello,客户端",CharsetUtil.UTF_8));
    }

    // 异常处理
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
