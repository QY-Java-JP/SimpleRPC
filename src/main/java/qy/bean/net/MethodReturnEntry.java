package qy.bean.net;

import lombok.Data;

@Data
public class MethodReturnEntry {
    // 本次调用id
    private Integer id;
    // 返回参数
    private Object returnData;

    public MethodReturnEntry(int id, Object returnData) {
        this.id = id;
        this.returnData = returnData;
    }
}
