package chire.archivemanager.archive;

import arc.Core;
import arc.files.Fi;
import arc.struct.ArrayMap;
import arc.struct.Seq;
import arc.util.Log;
import chire.archivemanager.io.DataFile;
import mindustry.Vars;

import java.io.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static mindustry.Vars.*;

public class Archives {
    public Fi archiveDirectory = Vars.dataDirectory.child("archives");

    public DataFile data = new DataFile(archiveDirectory.child("setting.dat"));

    private final Seq<LoadedArchive> archives = new Seq<>();

    public Archives(){
        try {
            if (data.getFile().exists()) {
                data.loadValues();
                //checksums = (ArrayMap<String, byte[]>) data.getMap("checksums");
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
        load();
    }

    public void load(){
        archives.clear();

        var files = archiveDirectory.seq().filter(
            f-> f.isDirectory() || f.child("current.dat").exists()
        );

        for (var file : files){
            //var content = new DataFile(file.child("current.dat"));
            //content.loadValues();

            archives.add(new LoadedArchive(
                file.child("current.dat")
            ));
        }
    }

    public void save(){
        archiveDirectory.mkdirs();

        LocalDateTime currentDateTime = LocalDateTime.now();
        String archiveName = currentDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        Fi data = archiveDirectory.child(archiveName);
        Log.info("archiveName "+archiveName);

        //TODO name为null
        exportBaseData(null, data, currentDateTime);

        archives.add(new LoadedArchive(data.child("current.dat")));
        //GameData.save(data.child("content.dat"), gameData);
    }

//    public void delete(LoadedArchive archive){
//        archives.remove(archive);
//    }

    /**
     * 使用这个函数导出的数据为文件夹的形式，无法在游戏中以压缩包的形式导入
     *
     * @param file 保存的文件夹
     * @param time 时间
     */
    public void exportBaseData(String name, Fi file, LocalDateTime time){
        //String archiveName = file.path().substring(file.path().lastIndexOf("/")+1);
        String archiveName = file.name();
        ArrayMap<String, byte[]> current = new ArrayMap<>();
        DataFile currentData = new DataFile(file.child("current.dat"));

        Seq<Fi> files = new Seq<>();
        files.add(Core.settings.getSettingsFile());
        files.add(customMapDirectory);
        files.add(saveDirectory);
        files.add(modDirectory);
        files.add(schematicDirectory);

        for (var f : files){
            if (!f.exists()) continue;
            if (!f.isDirectory()) {
                current.put(f.path().replace(dataDirectory.path(), ""), getFileHash(f));
            } else {
                for (var d : f.list()) {
                    if (d.isDirectory()) continue;
                    current.put(d.path().replace(dataDirectory.path(), ""), getFileHash(d));
                }
            }
        }

        ArrayMap<String, String> same = new ArrayMap<>();
        if (data.getFile().exists() && data.has("last") && data.getObject("last") != null) {
            currentData.putObject("last", data.getObject("last").toString());
            Fi last = archiveDirectory.child(data.getObject("last").toString());
            DataFile lastConfig = new DataFile(last.child("current.dat"));
            ArrayMap<String, String> lastSame = null;
            if (lastConfig.getFile().exists()) {
                try {
                    lastConfig.loadValues();
                    lastSame = (ArrayMap<String, String>) lastConfig.getMap("same");
                } catch (IOException e) {
                    //TODO 这里写退出和错误补救操作
                    throw new RuntimeException(e);
                }
            }

            Log.info("lastSame["+last.name()+"]  "+lastSame);

            for (var c : current) {
                var fc = dataDirectory.child(c.key);
                var fl = last.child(c.key);
                if (lastSame != null && !fl.exists() && lastSame.containsKey(c.key)){
                    if (!Arrays.equals(getFileHash(archiveDirectory.child(lastSame.get(c.key))), getFileHash(fc))){
                        fc.copyTo(file.child(c.key));
                    } else {
                        same.put(c.key, lastSame.get(c.key));
                    }
                } else{
                    if (!fl.exists() || !Arrays.equals(getFileHash(fl), getFileHash(fc))) {
                        fc.copyTo(file.child(c.key));
                    } else {
                        same.put(c.key, last.name() + "/" + c.key);
                    }
                }
            }
        } else {
            for (var c : current) {
                var fc = dataDirectory.child(c.key);
                fc.copyTo(file.child(c.key));
            }
        }

        currentData.putMap("same", same);
        currentData.putObject("time", time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")));
        currentData.putObject("name", name == null ? archiveName : name);
        Log.info("same["+archiveName+"]"+same);
        currentData.saveValues();

        data.putObject("last", archiveName);
        data.saveValues();
    }

    public byte[] getFileHash(Fi file) {
        try(ByteArrayInputStream fis = file.readByteStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] dataBytes = new byte[1024];
            int nread;
            while ((nread = fis.read(dataBytes)) != -1) {
                digest.update(dataBytes, 0, nread);
            }
            return digest.digest();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Seq<LoadedArchive> list(){
        //return archiveDirectory.seq().filter(f-> f.isDirectory() || f.child("current.dat").exists());
        return archives;
    }

    public String getTime(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        return currentDateTime.format(formatter);
    }
}
