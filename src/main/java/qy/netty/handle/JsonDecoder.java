package qy.netty.handle;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import qy.bean.DataEntry;

import java.util.List;

public class JsonDecoder extends ByteToMessageDecoder {

    static {
        initJsonReaderAccept();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);

        out.add(JSON.parseObject(bytes, DataEntry.class, JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.SupportClassForName));
    }

    // 反序列化白名单
    private static void initJsonReaderAccept(){
        JSONFactory.getDefaultObjectReaderProvider().addAutoTypeAccept("qy.bean.*");
    }

}

