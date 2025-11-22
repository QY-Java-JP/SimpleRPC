package qy.constant;

public interface NetErrorCode {
    // 不合适的请求参数
    int BAD_REQUEST = 1001;

    // 找不到该类对应的节点
    int METHOD_TARGET_NOT_FIND = 1002;

    // 找不到对应的类
    int METHOD_OBJECT_NOT_FIND = 1003;

    // 找不到对应的方法
    int METHOD_NOT_FIND = 1004;

    // 服务端异常
    int SERVER_ERROR = 1999;
}
