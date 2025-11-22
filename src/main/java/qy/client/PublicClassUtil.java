package qy.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qy.bean.DataEntry;
import qy.bean.net.ClassRegisterEntry;
import qy.constant.NetConstant;
import qy.exception.net.CanUsePortNotFindException;
import qy.server.ClassFindServer;
import qy.server.MethodInvokeServer;
import qy.util.NetUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PublicClassUtil {

    private static final Logger log = LoggerFactory.getLogger(PublicClassUtil.class);

    // 全局静态路径和类的map
    private static final Map<String, Object> classNameObjMap = new ConcurrentHashMap<>();

    // 服务提供服务端
    private static MethodInvokeServer methodInvokeServer;
    // 服务器端口
    private static int misPort;

    // 服务发现服务端
    private static ClassFindServer classFindServer;

    static {
        checkAndStartClassFindServer();
        try {
            methodInvokeServer = new MethodInvokeServer(classNameObjMap);
            misPort = methodInvokeServer.getPort();
            methodInvokeServer.start();
            log.info("服务提供服务器已开启 端口:{}", misPort);
        } catch (CanUsePortNotFindException e) {
            log.error("服务提供服务器初始化失败 没有可用的端口");
        }
    }

    // 公开一个类
    public static void publishClass(Object o){
        // 首先注册
        if (!registerClass(o.getClass())) {
            return;
        }

        // 加入到集合
        classNameObjMap.put(o.getClass().getInterfaces()[0].getName(), o);
        log.info("类:{} 已可向外提供服务", o.getClass());
    }

    // 向注册中心注册此类
    private static boolean registerClass(Class<?> clazz){
        // 注册的是第一个接口名
        final Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length < 1) {return false;}

        final Class<?> registerInter = interfaces[0];
        final ClassRegisterEntry registerEntry = new ClassRegisterEntry(registerInter.getName(), "127.0.0.1:" + misPort);
        final DataEntry entry = new DataEntry(DataEntry.CLASS_TARGET_REGISTER_REQ, registerEntry);
        final String jsonReq = JSON.toJSONString(entry, JSONWriter.Feature.WriteClassName);

        final String res = NetUtil.sendTcpRequest("127.0.0.1", NetConstant.CLASS_FIND_SERVER_PORT, jsonReq);
        if (res == null){
            log.error("注册类失败 res为Null");
            return false;
        }

        final DataEntry resEntry = JSON.parseObject(res, DataEntry.class, JSONReader.Feature.SupportAutoType);
        assert resEntry != null;
        if (resEntry.getErrorCode() > 0) {
            log.error("注册类失败 errorCode: {}", resEntry.getErrorCode());
            return false;
        }

        log.info("接口:{} 向注册中心注册成功", registerInter.getName());
        return true;
    }

    // 检查和开启服务发现服务器
    private static void checkAndStartClassFindServer(){
        if (NetUtil.portIsUsed(NetConstant.CLASS_FIND_SERVER_PORT)) {
            log.info("服务发现端已被其他服务注册");
            return;
        }

        classFindServer = new ClassFindServer();
        classFindServer.start();
        log.info("本服务将同时成为 服务发现端");
    }

}
