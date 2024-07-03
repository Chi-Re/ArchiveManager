package chire.archivemanager.test;

import java.io.Serializable;

public class ClassData implements Serializable {
    public String test1;

    public Integer test2;

    @Override
    public String toString() {
        return "SaveClass{" +
                "test1='" + test1 + '\'' +
                ", test2=" + test2 +
                '}';
    }
}
