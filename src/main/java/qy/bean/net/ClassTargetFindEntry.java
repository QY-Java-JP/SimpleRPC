package qy.bean.net;

import lombok.Data;

@Data
public class ClassTargetFindEntry {
    // 全类名
    private String className;

    public ClassTargetFindEntry() {
    }

    public ClassTargetFindEntry(String className) {
        this.className = className;
    }
}
