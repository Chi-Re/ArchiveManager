package chire.archivemanager.archive;

import arc.Core;
import arc.files.Fi;
import arc.struct.ArrayMap;
import arc.struct.Seq;
import arc.util.Log;
import chire.archivemanager.ArchiveManager;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static chire.archivemanager.ArchiveManager.archiveDirectory;
import static chire.archivemanager.ArchiveManager.data;
import static mindustry.Vars.*;
import static mindustry.Vars.schematicDirectory;

public class Archives {
    /**
     * key - 为时间字符串(不可修改，自动生成)
     * value - LoadedArchive
     */
    private final ArrayMap<String, LoadedArchive> archives = new ArrayMap<>();

    public void load(){
        //Log.info(data.getMap("saveFiles", String.class, String.class));
        //Log.info(toTime(data.getString("time")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH mm:ss:SSS")));

        for (var a : data.getList("archive-list", String.class)) {
            String name = data.getString(a+"-name");
            LocalDateTime time = toTime(data.getString(a+"-time"));
            ArrayMap<String, String> saveFiles = data.getMap(a+"-saveFiles", String.class, String.class);

            //TODO 总觉得有些多余
            archives.put(time.toString(), new LoadedArchive(name, time, saveFiles));
        }
    }

    public void save(){
        LocalDateTime time = time();
        String name = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));
        Seq<Fi> files = getCopyFiles();
        ArrayMap<String, String> saveFiles = disposalFile(files);

        for (var f : saveFiles) {
            Fi contentFi = archiveDirectory
                    .child(f.value.substring(0, 2))
                    .child(f.value.substring(2));

            if (!contentFi.exists()) {
                dataDirectory.child(f.key).copyTo(contentFi);
            }
        }

        data.putObject(time +"-time", time.toString());
        data.putObject(time +"-name", name);//TODO 提高拓展
        data.putMap(time +"-saveFiles", saveFiles);
        //archives.add(new LoadedArchive(time.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")), saveFiles));
        archives.put(time.toString(), new LoadedArchive(name, time, saveFiles));

        if (data.has("archive-list")) {
            List<String> list = data.getList("archive-list", String.class);
            list.add(time.toString());
            data.putList("archive-list", list);
        } else {
            List<String> list = new ArrayList<>();
            list.add(time.toString());
            data.putList("archive-list", list);
        }

        ArchiveManager.data.saveValues();
    }

    public Seq<Fi> getCopyFiles(){
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
        return archives.values().toArray().list();
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

    public LocalDateTime toTime(String name) {
        return LocalDateTime.parse(name);
    }
}
