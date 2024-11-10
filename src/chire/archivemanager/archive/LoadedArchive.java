package chire.archivemanager.archive;

import arc.files.Fi;
import arc.struct.ArrayMap;
import arc.util.Log;
import chire.archivemanager.io.DataFile;

import java.io.IOException;

/***/
public class LoadedArchive {
    private final Fi file;
    private final DataFile data;

    //Publishable, Disposable
    public LoadedArchive(Fi file){
        this.file = file;
        this.data = new DataFile(file);
        load();
    }

    public void load() {
        if (!file.exists() || file.isDirectory()) {
            Log.err("存档"+ file.path()+"加载错误: 文件不存在");
            return;
        }
        try {
            this.data.loadValues();
        } catch (IOException e){
            Log.err("存档"+ file.path()+"加载错误", e);
        }
    }

    public Fi file(){
        return file;
    }

    public String name(){
        return get("name", "None", String.class);
    }

    public String time(){
        return get("time", "None", String.class);
    }

    public <T> T get(String key, T def, Class<T> type){
        return type.cast(data.get(key, def));
    }

    public <T> T get(String key, Class<T> type){
        return this.get(key, null, type);
    }
}
