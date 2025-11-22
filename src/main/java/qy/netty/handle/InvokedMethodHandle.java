package qy.netty.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.DefaultPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qy.bean.DataEntry;
import qy.bean.net.MethodReturnEntry;

import java.util.Map;

public class InvokedMethodHandle extends SimpleChannelInboundHandler<DataEntry> {

    private static final Logger log = LoggerFactory.getLogger(InvokedMethodHandle.class);

    // 异步任务表
    private final Map<Integer, DefaultPromise<DataEntry>> remotePromise;

    public InvokedMethodHandle(Map<Integer, DefaultPromise<DataEntry>> remotePromise) {
        this.remotePromise = remotePromise;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, DataEntry dataEntry) throws Exception {
        // 接受即可
        if (!dataEntry.getType().equals(DataEntry.INVOKE_METHOD_RES)) {
            context.fireChannelRead(dataEntry);
            return;
        }

        final MethodReturnEntry returnEntry = (MethodReturnEntry) dataEntry.getData();
        final DefaultPromise<DataEntry> promise = remotePromise.get(returnEntry.getId());
        if (promise == null) {return;}

        promise.setSuccess(dataEntry);
        log.info("远程方法Id: {} 调用成功", returnEntry.getId());
    }
}
