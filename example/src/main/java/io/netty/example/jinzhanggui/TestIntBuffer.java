package io.netty.example.jinzhanggui;

import java.nio.IntBuffer;

/**
 * Buffer是IntBuffer的基类，里面有几个重要的属性
 * position相当于操作索引
 * capacity相当于数组长度
 * limit相当于读写时的限制，读的时候不能超过已经写入的范围，写的时候不能写超最大容量
 */
public class TestIntBuffer {
    public static void main(String[] args) {
        IntBuffer intBuffer = IntBuffer.allocate(5);// 拿到的是HeapIntBuffer
        for(int i = 0; i < intBuffer.capacity(); i++){
            intBuffer.put(i*2);
        }
        intBuffer.put(1,9);// position不会加1，因为是指定的
        intBuffer.flip();  // 读写切换，不加这一句话时直接没有写功能，也不报错，很恶劣
        intBuffer.position(1);// 读写切换之后，可以跳着读
        intBuffer.limit(3); // 类似的道理，也可以设置读写的上限在哪里
        while (intBuffer.hasRemaining()){
            System.out.println(intBuffer.get());
        }
    }
}
