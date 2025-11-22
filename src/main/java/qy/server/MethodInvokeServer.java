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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qy.exception.net.CanUsePortNotFindException;
import qy.netty.handle.JsonDecoder;
import qy.netty.handle.JsonEncode;
import qy.netty.handle.MethodInvokerHandle;
import qy.util.NetUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MethodInvokeServer {

    // 路由表
    private final Map<String, Object> classNameObjMap;

    private static final Logger log = LoggerFactory.getLogger(MethodInvokeServer.class);

    // 端口
    private static int port = 8115;

    // 工作线程
    private final EventLoopGroup boosGroup = new NioEventLoopGroup();
    private final EventLoopGroup workGroup = new NioEventLoopGroup();

    public MethodInvokeServer(Map<String, Object> classNameObjMap) throws CanUsePortNotFindException {
        this.classNameObjMap = classNameObjMap;
        port = findCanUsePort();
    }

    // 创建服务器
    public void start(){
        start(false);
    }

    public void start(boolean block){
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
                            ch.pipeline().addLast(new MethodInvokerHandle(classNameObjMap));
                        }
                    });

            ChannelFuture f = bootstrap.bind(port).sync();
            if (block) {
                f.channel().closeFuture().sync();
            } else {
                f.channel().closeFuture();
            }

            log.info("MethodInvokeServer连接端口: {}", port);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // 查找一个空闲的端口
    private int findCanUsePort() throws CanUsePortNotFindException {
        // 从8115尝试
        int port = 8115;
        for (int i = 0; i < 100; i++) {
            if (!NetUtil.portIsUsed(port + i)) {
                return port + i;
            }
        }

        // 没找到
        throw new CanUsePortNotFindException();
    }

    // 获取端口
    public int getPort(){
        return port;
    }

}
