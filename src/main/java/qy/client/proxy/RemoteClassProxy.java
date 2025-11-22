package qy.client.proxy;

import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qy.bean.DataEntry;
import qy.bean.net.MethodInvokeEntry;
import qy.bean.net.MethodReturnEntry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public class RemoteClassProxy implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(RemoteClassProxy.class);

    // 目标链接
    private final Channel channel;
    // 异步任务表
    private final Map<Integer, DefaultPromise<DataEntry>> remotePromise;

    public RemoteClassProxy(Channel channel, Map<Integer, DefaultPromise<DataEntry>> remotePromise) {
        this.channel = channel;
        this.remotePromise = remotePromise;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 过滤 Object 的方法，不做远程调用
        if (method.getDeclaringClass() == Object.class) {
            log.info("远程调用Object方法 不做代理");
            return method.invoke(this, args);
        }

        // 封装方法 然后发请求
        final DataEntry entry = new DataEntry(DataEntry.INVOKE_METHOD_REQ, null);
        final MethodInvokeEntry invokeEntry = new MethodInvokeEntry(
                entry.getId(),
                method.getDeclaringClass().getName(), method.getName(),
                method.getParameterTypes(), args
        );
        entry.setData(invokeEntry);

        // 创建异步任务 保存异步任务然后发起请求
        final DefaultPromise<DataEntry> promise = new DefaultPromise<>(channel.eventLoop());
        remotePromise.put(invokeEntry.getId(), promise);
        channel.writeAndFlush(entry);
        log.info("远程调用类:{} 方法:{} 等待回调", invokeEntry.getClassName(), invokeEntry.getMethodName());

        // 等待返回
        final DataEntry resEntry = promise.await().getNow();
        final MethodReturnEntry returnEntry = (MethodReturnEntry) resEntry.getData();
        log.info("远程调用:{} 方法:{} 成功", invokeEntry.getClassName(), invokeEntry.getMethodName());

        return returnEntry.getReturnData();
    }
}
