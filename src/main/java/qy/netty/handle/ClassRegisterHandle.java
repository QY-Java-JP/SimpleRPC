package qy.netty.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qy.bean.DataEntry;
import qy.bean.net.ClassRegisterEntry;

import java.util.Map;

public class ClassRegisterHandle extends SimpleChannelInboundHandler<DataEntry> {

    private static final Logger log = LoggerFactory.getLogger(ClassRegisterHandle.class);

    // 路由表
    private final Map<String, String> router;

    public ClassRegisterHandle(Map<String, String> router) {
        this.router = router;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, DataEntry dataEntry) throws Exception {
        if (!dataEntry.getType().equals(DataEntry.CLASS_TARGET_REGISTER_REQ)) {
            context.fireChannelRead(dataEntry);
            return;
        }

        final ClassRegisterEntry registerEntry = (ClassRegisterEntry) dataEntry.getData();
        router.put(registerEntry.getClassName(), registerEntry.getAddr());
        log.info("{}已注册 地址:{}", registerEntry.getClassName(), registerEntry.getAddr());

        context.writeAndFlush(new DataEntry(DataEntry.CLASS_TARGET_REGISTER_RES, null));
    }
}
