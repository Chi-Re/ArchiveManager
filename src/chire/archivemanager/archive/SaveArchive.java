package chire.archivemanager.archive;

import arc.files.Fi;
import arc.util.Log;
import chire.archivemanager.ArchiveManager;
import chire.archivemanager.io.DataFile;
import chire.archivemanager.ui.ArchiveDialog;
import chire.archivemanager.ui.tree.ArchiveNode;
import chire.archivemanager.ui.tree.ArchiveTree;
import chire.archivemanager.ui.tree.NodeType;
import mindustry.Vars;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static chire.archivemanager.ui.tree.ArchiveTree.*;
import static mindustry.Vars.ui;

public class SaveArchive {
    public static Fi archiveDirectory = Vars.dataDirectory.child("cache_archive");
    /**����ȫ���ڵ�������ļ�*/
    public static DataFile config = new DataFile(archiveDirectory.child("archiveTree.dat"));

    public static ArchiveNode archiveTree;

    public static String current = null;

    public static void loadTree(){
        if (config.getFile().exists()) {
            archiveTree = config.getDataClass("tree", ArchiveNode.class);
        } else {
            archiveTree = nodeRoot(
                    "�浵���ڵ�",
                    "��ʼ�浵�ɣ�����ڵ��������ص�������Ϸ�ĵط���\n(�մ浵)",
                    null);
            archiveTree.current = true;
            current = archiveTree.getKey();

            archiveTree.addNode(ArchiveTree.nodeNew());
//            saveTree();
        }
    }

    public static void saveTree(){
        config.putClass("tree", ArchiveNode.class, archiveTree);
        config.saveValues();
    }

    public static void upNode(GameData gd, Fi file){
        var treeCurrent = ArchiveNode.nodes.get(ArchiveNode.nodes.get("new").parents);
        ArchiveNode.nodes.removeKey(treeCurrent.getKey());
        ArchiveNode.nodes.removeKey("new");
        treeCurrent.children.remove("new");

        var n = node(gd, file);
        n.addNode(nodeNew());
        n.current = true;
        treeCurrent.addNode(n);
        ArchiveNode.nodes.put(treeCurrent.getKey(), treeCurrent);
    }

    public static void save(){
        archiveDirectory.mkdirs();

        String archiveName = getTime();
        GameData gd = new GameData();
        Fi archiveFi = archiveDirectory.child(archiveName+".dat");

        try{
            Vars.ui.settings.exportData(archiveDirectory.child(archiveName+".zip"));

            GameData.save(archiveFi, gd);
            upNode(gd, archiveFi);

            //saveTree();
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
