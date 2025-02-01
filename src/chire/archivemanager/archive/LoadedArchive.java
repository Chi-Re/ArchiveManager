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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static chire.archivemanager.ArchiveManager.archiveDirectory;
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

    /**是否为上一个被加载的存档*/
    public boolean last(){
        return data.has("archive-load") && Objects.equals(data.getString("archive-load"), key());
    }

    public boolean arc(){
        return keyHas("arc") && (boolean) keyGet("arc");
    }

    public String gameVersion(){
        return keyHas("game-version") ? keyGet("game-version").toString() : "null";
    }

    public ArrayMap<String, Integer> itemStorage(){
        return keyHas("game-items") ? data.getMap(this.key + "-game-items", String.class, Integer.class) : new ArrayMap<>();
    }

    public ArrayMap<String, String> saveFiles(){
        return data.getMap(this.key+"-saveFiles", String.class, String.class);
    }

    public void putSaveFiles(ArrayMap<String, String> fileMap){
        data.putMap(this.key+"-saveFiles", fileMap);
    }

    public void putSaveFiles(String key, String value) {
        ArrayMap<String, String> sfs = this.saveFiles();
        sfs.put(key, value);
        putSaveFiles(sfs);
    }

    public void delete() {
        List<String> aList = data.getList("archive-list", String.class);

        data.remove(this.key + "-time");
        data.remove(this.key + "-name");
        data.remove(this.key + "-saveFiles");
        data.remove(this.key + "-arc");
        data.remove(this.key + "-game-version");
        data.remove(this.key + "-game-items");
        if (last()) data.remove("archive-load");

        if (aList.contains(this.key)) aList.remove(this.key);

        data.putList("archive-list", aList);

        data.saveValues();
    }

    public @Nullable LocalDateTime time(){
        if (!keyHas("time")) return null;
        return toTime(keyGet("time").toString());
    }

    public String parseTime(){
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
