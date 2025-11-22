package qy.netty.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qy.bean.DataEntry;
import qy.bean.net.ClassTargetFindEntry;
import qy.constant.NetErrorCode;

import java.util.Map;

// 查询调用方法地址的handle
public class ClassFindHandle extends SimpleChannelInboundHandler<DataEntry> {

    private static final Logger log = LoggerFactory.getLogger(ClassFindHandle.class);

    // 路由表
    private final Map<String, String> router;

    public ClassFindHandle(Map<String, String> router) {
        this.router = router;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, DataEntry dataEntry) throws Exception {
        if (!dataEntry.getType().equals(DataEntry.FIND_METHOD_TARGET_REQ)) {
            context.fireChannelRead(dataEntry);
            return;
        }

        // 去路由表中查
        final ClassTargetFindEntry findEntry = (ClassTargetFindEntry) dataEntry.getData();
        if (!router.containsKey(findEntry.getClassName())) {
            log.warn("未找到 {} 的对应节点", findEntry.getClassName());
            context.writeAndFlush(new DataEntry(-1, null, NetErrorCode.METHOD_TARGET_NOT_FIND));
            return;
        }

        final String addr = router.get(findEntry.getClassName());
        context.writeAndFlush(new DataEntry(DataEntry.FIND_METHOD_TARGET_RES, addr));
    }
}
