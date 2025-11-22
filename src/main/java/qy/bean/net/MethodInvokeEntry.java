package qy.bean.net;

import lombok.Data;

import java.util.Arrays;

@Data
public class MethodInvokeEntry {
    // 本次调用id
    private Integer id;
    // 目标全类名
    private String className;
    // 目标方法名
    private String methodName;
    // 参数全类型
    private Class<?>[] argTypes;
    // 参数
    private Object[] args;

    public MethodInvokeEntry() {
    }

    public MethodInvokeEntry(int id, String className, String methodName, Class<?>[] argTypes, Object[] args) {
        this.id = id;
        this.className = className;
        this.methodName = methodName;
        this.argTypes = argTypes;
        this.args = args;
    }

    @Override
    public String toString() {
        return "MethodInvokeEntry{" +
                "id=" + id +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", argTypes=" + Arrays.toString(argTypes) +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
