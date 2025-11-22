package qy.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import qy.constant.NetConstant;
import qy.netty.handle.ClassRegisterHandle;
import qy.netty.handle.JsonDecoder;
import qy.netty.handle.JsonEncode;
import qy.netty.handle.ClassFindHandle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassFindServer {
    // 工作线程
    private final EventLoopGroup boosGroup = new NioEventLoopGroup();
    private final EventLoopGroup workGroup = new NioEventLoopGroup();

    // 路由表
    private final Map<String, String> classRouter = new ConcurrentHashMap<>();

    // 开启一个监听
    public void start(){
        start(false);
    }

    public void start(boolean block) {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boosGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(
                                    1024 * 1024,
                                    0,
                                    4,
                                    0,
                                    4));

                            // 解码
                            ch.pipeline().addLast(new JsonDecoder());
                            // 长度
                            ch.pipeline().addLast(new LengthFieldPrepender(4));
                            // 编码
                            ch.pipeline().addLast(new JsonEncode());
                            // 业务
                            ch.pipeline().addLast(new ClassRegisterHandle(classRouter));
                            ch.pipeline().addLast(new ClassFindHandle(classRouter));
                        }
                    });

            ChannelFuture f = bootstrap.bind(NetConstant.CLASS_FIND_SERVER_PORT).sync();
            if (block) {
                f.channel().closeFuture().sync();
            } else {
                f.channel().closeFuture();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
