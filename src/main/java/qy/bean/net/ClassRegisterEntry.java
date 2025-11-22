package qy.bean.net;

import lombok.Data;

@Data
public class ClassRegisterEntry {
    // 类名
    private String className;
    // 地址
    private String addr;

    public ClassRegisterEntry() {
    }

    public ClassRegisterEntry(String className, String addr) {
        this.className = className;
        this.addr = addr;
    }
}
