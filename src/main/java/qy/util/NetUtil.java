package qy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class NetUtil {

    private static final Logger log = LoggerFactory.getLogger(NetUtil.class);

    // 端口是否被占用
    public static boolean portIsUsed(int port){
        try {
            new ServerSocket(port).close();
            return false; // 能绑定，说明没被占用
        } catch (Exception e) {
            return true; // 绑定失败，说明被占用
        }
    }

    // 发送一个tcp请求
    public static String sendTcpRequest(String host, int port, String data){
        try (Socket socket = new Socket(host, port);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream())
        {
            // [长度][数据]
            final byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            out.write(ByteBuffer.allocate(4).putInt(dataBytes.length).array());
            out.write(dataBytes);
            out.flush();

            // 同样读取到以后也要把长度去掉
            final byte[] lenBytes = in.readNBytes(4);
            final int len = ByteBuffer.wrap(lenBytes).getInt();

            // 再读真实数据
            return new String(in.readNBytes(len), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.info("NetUtil发送请求IO异常: {}", e.getMessage());
        }

        return null;
    }

}
