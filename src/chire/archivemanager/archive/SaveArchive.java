package chire.archivemanager.archive;

import arc.files.Fi;
import arc.util.Log;
import chire.archivemanager.io.DataFile;
import chire.archivemanager.ui.tree.ArchiveNode;
import chire.archivemanager.ui.tree.ArchiveTree;
import mindustry.Vars;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static chire.archivemanager.ui.tree.ArchiveTree.*;
import static mindustry.Vars.ui;

public class SaveArchive {
    public static Fi archiveDirectory = Vars.dataDirectory.child("cache_archive");
    /**储存全部节点的配置文件*/
    public static DataFile config = new DataFile(archiveDirectory.child("archiveTree.dat"));

    public static ArchiveNode archiveTree;

    public static boolean current = false;

    public static void loadTree(){
        if (config.getFile().exists()) {
            archiveTree = config.getDataClass("tree", ArchiveNode.class);
        } else {
            archiveTree = nodeRoot("存档根节点", "开始存档吧，这个节点是用来回到初进游戏的地方。\n(空存档)", null,
                    node(null,
                            node(null, nodeNew())
                    ),
                    node(null)
            );
            archiveTree.current = true;
//            current = true;
//            saveTree();
        }
    }

    public static void saveTree(){
        if (current) {
            config.putClass("tree", ArchiveNode.class, archiveTree);
            config.saveValues();
        }
    }

    public static void save(){
        archiveDirectory.mkdirs();

        String archiveName = getTime();
        GameData gd = new GameData();
        Fi archiveFi = archiveDirectory.child(archiveName+".dat");

        try{
            Vars.ui.settings.exportData(archiveDirectory.child(archiveName+".zip"));

            GameData.save(archiveFi, gd);

            saveTree();
        }catch(Exception e){
            Log.err(e);
            archiveFi.delete();
            ui.showException(e);
        }

        Log.info(GameData.read(archiveFi));
    }

    public static String getTime(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        return currentDateTime.format(formatter);
    }
}
