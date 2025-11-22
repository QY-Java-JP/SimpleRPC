package qy.bean;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class DataEntry {

    // 类地址请求
    public static final int FIND_METHOD_TARGET_REQ = 1;
    public static final int FIND_METHOD_TARGET_RES = 2;

    // 调用方法
    public static final int INVOKE_METHOD_REQ = 3;
    public static final int INVOKE_METHOD_RES = 4;

    // 类地址注册
    public static final int CLASS_TARGET_REGISTER_REQ = 5;
    public static final int CLASS_TARGET_REGISTER_RES = 6;

    private static final AtomicInteger atomicId = new AtomicInteger(1);

    // id
    private Integer id = atomicId.getAndIncrement();
    // 数据类型
    private Integer type;
    // 数据
    private Object data;
    // 错误码
    private Integer errorCode = 0;

    public DataEntry(int type, Object data) {
        this.type = type;
        this.data = data;
    }

    public DataEntry() {
    }

    public DataEntry(int type, Object data, int errorCode) {
        this.type = type;
        this.data = data;
        this.errorCode = errorCode;
    }

    public DataEntry(Integer errorCode) {
        this.type = -1;
        this.errorCode = errorCode;
    }
}
