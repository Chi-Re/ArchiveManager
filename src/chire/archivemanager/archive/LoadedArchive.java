package chire.archivemanager.archive;

import arc.files.Fi;
import arc.struct.ArrayMap;
import arc.util.Log;
import chire.archivemanager.io.DataFile;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/***/
public class LoadedArchive {
    private String name;
    private final LocalDateTime time;
    private final ArrayMap<String, String> data;

    //Publishable, Disposable
    public LoadedArchive(String name, LocalDateTime time, ArrayMap<String, String> data){
        this.name = name;
        this.time = time;
        this.data = data;
        load();
    }

    public void load() {

    }

    public String name(){
        return this.name;
    }

    public String time(){
        if (time == null) return "null";
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));
    }

    @Override
    public String toString() {
        return "LoadedArchive{" +
                "name='" + name + '\'' +
                ", time=" + time +
                ", data=" + data +
                '}';
    }
}
