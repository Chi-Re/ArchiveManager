package chire.archivemanager.archive;

import arc.files.Fi;
import arc.struct.ArrayMap;
import arc.util.Log;
import arc.util.Nullable;
import chire.archivemanager.game.SectorSlot;
import chire.archivemanager.io.DataFile;
import mindustry.io.MapIO;
import mindustry.io.SaveIO;
import mindustry.maps.Map;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static chire.archivemanager.ArchiveManager.archiveDirectory;
import static chire.archivemanager.ArchiveManager.data;
/***/
public class LoadedArchive {
    private final String key;

    protected final DataFile settings;

    //Publishable, Disposable
    public LoadedArchive(String key){
        this.key = key;
        String d = saveFiles().get("/settings.bin");
        DataFile df = new DataFile(archiveDirectory
                .child(d.substring(0, 2))
                .child(d.substring(2)));
        try {
            df.loadValues();
        } catch (IOException e) {
            Log.warn("文件["+df.getFile()+"]加载失败！");
            Log.warn(e.toString());
        }
        this.settings = df;
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

    public HashMap<String, Object> setting(){
        return settings.getValues();
    }

    /**星球的地图*/
    public ArrayList<SectorSlot> sectors() {
        ArrayList<SectorSlot> maps = new ArrayList<>();
        ArrayMap<String, String> saveFiles = saveFiles();
        for (var save : saveFiles) {
            Fi mapFile = archiveDirectory
                    .child(save.value.substring(0, 2))
                    .child(save.value.substring(2));

            if (save.key.contains("saves/") && save.key.contains("sector-") && save.key.contains(".msav")) {
                boolean backup = save.key.contains(".msav-backup.msav");
                try{
                    maps.add(new SectorSlot(SaveIO.getMeta(SaveIO.getStream(mapFile)), mapFile, backup));
                }catch(Throwable e){
                    //TODO 之后添加显示相关的
                    //Log.err(e);
                }
            }
        }
        return maps;
    }

    /**自定义模式的地图*/
//    public ArrayList<SectorSlot> maps(){
//        ArrayList<SectorSlot> maps = new ArrayList<>();
//        ArrayMap<String, String> saveFiles = saveFiles();
//        for (var save : saveFiles) {
//            Fi mapFile = archiveDirectory
//                    .child(save.value.substring(0, 2))
//                    .child(save.value.substring(2));
//            String name;
//            int dotIndex = save.key.lastIndexOf('.');
//            int slaIndex = save.key.lastIndexOf('/');
//            name = save.key.substring(slaIndex+1, dotIndex);
//
//            if (SaveIO.isSaveValid(mapFile)) {
//                Log.info("存档["+mapFile.path()+"/"+save.key+"]不是有效存档！加载失败");
//                continue;
//            }
//            if (save.key.contains("saves/") && save.key.contains("sector-") && save.key.contains(".msav")) continue;
//            try{
//                maps.add(new SectorSlot(SaveIO.getMeta(SaveIO.getStream(mapFile)), name, settings));
//            }catch(Throwable e){
//                Log.err(e);
//                String saveBackup = saveFiles.get(save.key+"-backup.msav");
//                maps.add(new SectorSlot(SaveIO.getMeta((archiveDirectory
//                        .child(saveBackup.substring(0, 2))
//                        .child(saveBackup.substring(2)))), name, settings));
//            }
//
//        }
//        return maps;
//    }

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
