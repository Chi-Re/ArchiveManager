package chire.archivemanager.game;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Strings;
import mindustry.game.Gamemode;
import mindustry.io.MapIO;
import mindustry.io.SaveMeta;
import mindustry.maps.Map;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SectorSlot {
    public SaveMeta meta;

    public Fi file;

    public Map map;

    public Texture texture;

    public boolean backup;

    private static final DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();

    public SectorSlot(SaveMeta meta, Fi file, boolean backup) {
        this.meta = meta;
        this.file = file;
        this.backup = backup;
        try {
            map = MapIO.createMap(file, false);
            texture = new Texture(MapIO.generatePreview(map));
        } catch (Exception e) {
            Log.info(e);
        }
    }

    public String get(String key) {
        return meta.tags.get(key);
    }

    public String getDate(){
        return dateFormat.format(new Date(meta.timestamp));
    }

    public String name(){
        return meta.rules != null ? meta.rules.sector.name() : "";
    }

    public @Nullable Texture pixmap(){
        return texture;
    }

    public long getTimestamp(){
        return meta.timestamp;
    }

    public String getPlayTime(){
        return Strings.formatMillis(meta.timePlayed);
    }

    public int getWave(){
        return meta.wave;
    }

    public Gamemode mode(){
        return meta.rules.mode();
    }
    /**区块地图一般都会自动存档*/
    public boolean isAutosave(){
        return true;
    }

    public boolean isBackup(){
        return backup;
    }

    public void exportFile(Fi to) throws IOException{
        try{
            file.copyTo(to);
        }catch(Exception e){
            throw new IOException(e);
        }
    }
}
