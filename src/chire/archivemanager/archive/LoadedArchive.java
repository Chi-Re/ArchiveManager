package chire.archivemanager.archive;

import arc.files.Fi;
import arc.struct.ArrayMap;
import arc.util.Log;
import arc.util.Nullable;
import chire.archivemanager.ArchiveManager;
import chire.archivemanager.io.DataFile;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static chire.archivemanager.ArchiveManager.data;
/***/
public class LoadedArchive {
    private final String key;

    //Publishable, Disposable
    public LoadedArchive(String key){
        this.key = key;
        load();
    }

    public void load() {

    }

    public String name(){
        return keyGet("name").toString();
    }

    /**上一个被加载的存档*/
    public boolean last(){
        return data.has("archive-load") && Objects.equals(data.getString("archive-load"), key());
    }

    public ArrayMap<String, String> saveFiles(){
        return data.getMap(this.key+"-saveFiles", String.class, String.class);
    }

    public String time(){
        if (!keyHas("time")) return "null";
        return toTime(keyGet("time").toString()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));
    }

    public String key(){
        return key;
    }

    public boolean keyHas(String value){
        return data.has(this.key+"-"+value);
    }

    public @Nullable Object keyGet(String value){
        return data.get(this.key+"-"+value, null);
    }

    public LocalDateTime toTime(String name) {
        return LocalDateTime.parse(name);
    }
}
