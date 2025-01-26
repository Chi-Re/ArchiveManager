package chire.archivemanager.archive;

import arc.Core;
import arc.files.Fi;
import arc.struct.ArrayMap;
import arc.struct.Seq;
import arc.util.ArcRuntimeException;
import arc.util.Log;
import chire.archivemanager.ArchiveManager;

import javax.annotation.processing.FilerException;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static arc.Core.settings;
import static chire.archivemanager.ArchiveManager.archiveDirectory;
import static chire.archivemanager.ArchiveManager.data;
import static mindustry.Vars.*;
import static mindustry.Vars.schematicDirectory;

public class Archives {
    public void init(){
        //Log.info(data.getList("archive-list", String.class));
        //Log.info(data.getMap("archive-file-length", String.class, Integer.class));
        ArrayMap<String, Integer> fl = data.getMap("archive-file-length", String.class, Integer.class);

        for (var key : data.getList("archive-list", String.class)) {
            Log.info(key);
            for (var c : data.getMap(key +"-saveFiles", String.class, String.class)) {
                Log.info(c.key+"="+c.value+": "+fl.get(c.value));
            }
        }
    }

    public void load(LoadedArchive loaded){
        if ((!loaded.keyHas("saveFiles"))) {
            ui.showErrorMessage("存档加载失败！你的saveFiles不存在，无法获取存档的数据。");
            return;
        }

        //校验文件是否存在
        ArrayMap<String, String> dataFiles = loaded.saveFiles();
        for (var d : dataFiles){
            Fi contentFi = archiveDirectory
                    .child(d.value.substring(0, 2))
                    .child(d.value.substring(2));

            if (!contentFi.exists())  {
                ui.showErrorMessage("存档文件加载失败！你存档"+loaded.key()+"的"+d.key+"不存在，存档数据损坏。");
                return;
            }
        }

        //删除原数据
        Seq<Fi> files = getCopyFiles();
        for (var f : files) {
            f.delete();
        }

        for (var d : dataFiles) {
            archiveDirectory
                    .child(d.value.substring(0, 2))
                    .child(d.value.substring(2))
                    .copyTo(dataDirectory.child(d.key));
        }

        data.putObject("archive-load", loaded.key());

        settings.clear();
        settings.load();

        data.saveValues();
    }

    public void save(SaveConfig config){
        LocalDateTime time = time();
        String key = time.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String name = key;
        if (config.name != null && !config.name.equals("")) {
            name = config.name;
        }
        ArrayMap<String, Integer> newFileLength = new ArrayMap<>();
        ArrayMap<String, Integer> fileLength = new ArrayMap<>();
        if (data.has("archive-file-length")) {
            fileLength = data.getMap("archive-file-length", String.class, Integer.class);
        }
        Seq<Fi> files = getCopyFiles();
        ArrayMap<String, String> saveFiles = disposalFile(files);

        for (var f : saveFiles) {
            Fi contentFi = archiveDirectory
                    .child(f.value.substring(0, 2))
                    .child(f.value.substring(2));

            if (!contentFi.exists()) {
                dataDirectory.child(f.key).copyTo(contentFi);
            }

            if (fileLength.containsKey(f.value)) {
                int fl = fileLength.get(f.value);
                fl ++;
                newFileLength.put(f.value, fl);
                fileLength.removeKey(f.value);
            } else {
                newFileLength.put(f.value, 1);
            }
        }

        if (fileLength.size != 0) {
            for (var flk : fileLength) {
                int fl = fileLength.get(flk.key);
                fl --;
                newFileLength.put(flk.key, fl);
                fileLength.removeKey(flk.key);
            }
        }

        data.putObject(key +"-time", time.toString());
        data.putObject(key +"-name", name);
        data.putMap(key +"-saveFiles", saveFiles);
        data.putObject("archive-load", key);

        if (data.has("archive-list")) {
            List<String> list = data.getList("archive-list", String.class);
            list.add(key);
            data.putList("archive-list", list);
        } else {
            List<String> list = new ArrayList<>();
            list.add(key);
            data.putList("archive-list", list);
        }
        data.putMap("archive-file-length", newFileLength);

        ArchiveManager.data.saveValues();
    }

    public void delete(LoadedArchive archive) {
        ArrayMap<String, Integer> saveFilesLength = data.getMap("archive-file-length", String.class, Integer.class);

        for (var save : archive.saveFiles()) {
            int saveLength = saveFilesLength.get(save.value);
            if (saveLength <= 1) {
                Fi saveFi = archiveDirectory
                        .child(save.value.substring(0, 2))
                        .child(save.value.substring(2));
                if (saveFi.exists()) {
                    saveFi.delete();
                }
                saveFilesLength.removeKey(save.value);
            } else {
                saveLength --;
                saveFilesLength.put(save.value, saveLength);
            }
        }

        data.putMap("archive-file-length", saveFilesLength);
        //delete时data就会保存一次
        archive.delete();
        data.saveValues();
    }

    public Seq<Fi> getCopyFiles(){
        //TODO 之后添加可拓展存档文件的功能
        Seq<Fi> files = new Seq<>();
        files.add(Core.settings.getSettingsFile());
        files.addAll(customMapDirectory.list());
        files.addAll(saveDirectory.list());
        files.addAll(modDirectory.list());
        files.addAll(schematicDirectory.list());
        return files;
    }

    /**
     * @return key: 文件的相对路径; value: 文件的哈希值(特殊处理)
     */
    public ArrayMap<String, String> disposalFile(Seq<Fi> fis){
        ArrayMap<String, String> disposals = new ArrayMap<>();

        for (var f: fis){
            if (f.isDirectory() || !f.exists()) {
                Log.warn("文件 "+f.path()+" 错误，请及时修改避免损坏存档。");
                continue;
            }
            String rpath = f.path().replace(dataDirectory.path(), "");
            disposals.put(rpath, getFileHash(f, rpath));
        }

        return disposals;
    }

    public ArrayList<LoadedArchive> list(){
        ArrayList<LoadedArchive> list = new ArrayList<>();
        if (data.has("archive-list")) {
            for (String a : data.getList("archive-list", String.class)) {
                list.add(new LoadedArchive(a));
            }
        }

        //使获取list中以时间由大到小排列(原来的是由小到大)
        Collections.reverse(list);

        return list;
    }

    /**
     * 校验文件是否变更
     * @param file 文件
     * @param args 附加参数，防止文件相同其他不同(例如路径)
     */
    public String getFileHash(Fi file, String args) {
        MessageDigest md;

        try(ByteArrayInputStream fis = file.readByteStream()) {
            md = MessageDigest.getInstance("SHA-256");

            int bufferSize = 8192; // 8KB buffer
            byte[] buffer = new byte[bufferSize];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                // 处理已读取的字节块，例如更新哈希函数
                if (bytesRead == buffer.length) {
                    // 如果读取了完整的缓冲区大小，则直接处理
                    md.update(buffer);
                } else {
                    // 如果是最后一块，可能是不完整的，所以复制到新数组
                    byte[] lastChunk = new byte[bytesRead];
                    System.arraycopy(buffer, 0, lastChunk, 0, bytesRead);
                    md.update(lastChunk);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (args != null) md.update(args.getBytes());

        StringBuilder builder = new StringBuilder();
        for (byte b : md.digest()) {
            builder.append(String.format("%02x", b));
        }

        return builder.toString();
    }

    public String getFileHash(Fi file) {
        return getFileHash(file, null);
    }

    /**
     * 缩小字符串的...方法?
     */
    public String shortenHash(String hash) {
        // 使用Base62编码作为字符集
        final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        // 将原始哈希值转换为一个大的整数
        BigInteger bigIntHash = new BigInteger(hash, 16);

        // 缩写长度
        int length = 18;

        StringBuilder shortened = new StringBuilder();

        // 循环生成缩写
        for (int i = 0; i < length; i++) {
            // 取模运算得到当前位的索引
            int index = bigIntHash.mod(BigInteger.valueOf(chars.length())).intValue();
            shortened.append(chars.charAt(index));

            // 更新bigIntHash为除以字符集长度后的商
            bigIntHash = bigIntHash.divide(BigInteger.valueOf(chars.length()));
        }

        return shortened.toString();
    }

    public LocalDateTime time(){
        return LocalDateTime.now();
    }
}
