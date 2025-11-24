package qy.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.DefaultPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qy.bean.DataEntry;
import qy.bean.net.ClassTargetFindEntry;
import qy.client.proxy.RemoteClassProxy;
import qy.constant.NetConstant;
import qy.netty.handle.InvokedMethodHandle;
import qy.netty.handle.JsonDecoder;
import qy.netty.handle.JsonEncode;
import qy.test.UserService;
import qy.util.NetUtil;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteClassFinder {

    private static final Logger log = LoggerFactory.getLogger(RemoteClassFinder.class);

    // 不同接口对应的连接表
    private static final Map<String, Channel> classChannelMap = new ConcurrentHashMap<>();
    // 异步任务表
    private static final Map<Integer, DefaultPromise<DataEntry>> remotePromise = new ConcurrentHashMap<>();

    // netty客户端
    private static final Bootstrap remoteBootStrap = new Bootstrap()
            .group(new NioEventLoopGroup())
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
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
                    ch.pipeline().addLast(new InvokedMethodHandle(remotePromise));
                }
            });

    static {
        // 注册通道关闭事件
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Channel channel : classChannelMap.values()) {
                try {
                    channel.close().await(3000);
                } catch (InterruptedException e) {
                    log.error("channel关闭异常");
                }
            }
        }));
    }

    // 生成一个可以调用远程方法的接口实现类
    public static <T> T create(Class<T> clazz) throws InterruptedException, ClassNotFoundException {
        // 需要是接口
        if (!clazz.isInterface()) {
            throw new InterruptedException("需要提供一个接口");
        }

        // 拿到链接
        final Channel channel = getClassTargetChannel(clazz.getName());
        if (channel == null) {
            throw new ClassNotFoundException("没有找到这个类的代理服务");
        }

        // 建立代理返回出去
        final Object proxyObj = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                new RemoteClassProxy(channel, remotePromise));
        return clazz.cast(proxyObj);
    }

    // 拿去一个类的链接
    private static Channel getClassTargetChannel(String className) throws InterruptedException {
        // 看看本地有没有
        if (classChannelMap.containsKey(className)) {
            return classChannelMap.get(className);
        }

        // 运行到这里没有 拿到这个类的地址
        final String targetAddr = findClassTargetAddr(className);
        if (targetAddr == null) {return null;}
        // 如果有这个链接了 则不需要新建了
        for (Channel channel : classChannelMap.values()) {
            InetSocketAddress addr = (InetSocketAddress) channel.remoteAddress();
            if (targetAddr.equals(addr.getHostName() + ":" + addr.getPort())) {
                classChannelMap.put(className, channel);
                return channel;
            }
        }

        // 运行到这里代表没有 真就要新建了
        final Channel newChannel = openChannel(targetAddr);
        classChannelMap.put(className, newChannel);
        return newChannel;
    }

    // 开启对于一台方法服务端的连接
    private static Channel openChannel(String addr) throws InterruptedException {
        final String[] addrAndPort = addr.split(":");
        log.info("建立与:{} 的长连接", addr);

        return remoteBootStrap.connect(addrAndPort[0], Integer.parseInt(addrAndPort[1])).sync().channel();
    }

    // 远程拿取一个类的地址
    private static String findClassTargetAddr(String className) {
        final ClassTargetFindEntry findEntry = new ClassTargetFindEntry(className);
        final DataEntry entry = new DataEntry(DataEntry.FIND_METHOD_TARGET_REQ, findEntry);

        final String res = NetUtil.sendTcpRequest("127.0.0.1", NetConstant.CLASS_FIND_SERVER_PORT,
                JSON.toJSONString(entry, JSONWriter.Feature.WriteClassName));
        if (res == null) {
            return null;
        }

        final DataEntry resEntry = JSON.parseObject(res, DataEntry.class, JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.SupportClassForName);
        if (resEntry.getErrorCode() > 0) {
            log.error("找寻某个类错误码:{}", resEntry.getErrorCode());
            return null;
        }

        log.info("从注册中心查询类:{} 地址为:{}", className, resEntry.getData());
        return resEntry.getData().toString();
    }
}
