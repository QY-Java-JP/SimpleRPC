package qy.netty.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qy.bean.DataEntry;
import qy.bean.net.MethodInvokeEntry;
import qy.bean.net.MethodReturnEntry;
import qy.constant.NetErrorCode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class MethodInvokerHandle extends SimpleChannelInboundHandler<DataEntry> {

    private static final Logger log = LoggerFactory.getLogger(MethodInvokerHandle.class);

    // 路由表
    private final Map<String, Object> router;

    public MethodInvokerHandle(Map<String, Object> router) {
        this.router = router;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, DataEntry dataEntry) {
        // 看看有没有 如果没有则返回错误
        final MethodInvokeEntry invokeEntry = (MethodInvokeEntry) dataEntry.getData();
        if (!router.containsKey(invokeEntry.getClassName())) {
            context.writeAndFlush(new DataEntry(NetErrorCode.METHOD_OBJECT_NOT_FIND));
            return;
        }

        final Object o = router.get(invokeEntry.getClassName());
        final Class<?> oClass = o.getClass();
        try {
            final Method method = oClass.getMethod(invokeEntry.getMethodName(), invokeEntry.getArgTypes());
            final Object retObject = method.invoke(o, invokeEntry.getArgs());
            final MethodReturnEntry returnEntry = new MethodReturnEntry(dataEntry.getId(), retObject);

            context.writeAndFlush(new DataEntry(DataEntry.INVOKE_METHOD_RES, returnEntry));
            log.info("方法:{} 远程调用成功", method.getName());

        } catch (NoSuchMethodException e) {
            log.error("服务端找不到类名:{} 方法:{}", invokeEntry.getClassName(), invokeEntry.getMethodName());
            context.writeAndFlush(new DataEntry(NetErrorCode.METHOD_NOT_FIND));
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("服务端未知异常: {}", e.getMessage());
            context.writeAndFlush(new DataEntry(NetErrorCode.SERVER_ERROR));
        }
    }
}
