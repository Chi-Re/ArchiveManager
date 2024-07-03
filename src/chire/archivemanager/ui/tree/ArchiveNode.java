package chire.archivemanager.ui.tree;

import arc.files.Fi;
import arc.scene.style.Drawable;
import arc.struct.Seq;
import arc.util.Nullable;
import chire.archivemanager.archive.GameData;
import chire.archivemanager.archive.SaveArchive;
import chire.archivemanager.ui.ArchiveInfoDialog;
import chire.archivemanager.ui.ShowNodeDialog;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;

import java.awt.*;

public class ArchiveNode {
    /**emm�����ƺ�û�����壿*/
    public @Nullable Drawable icon = Icon.save;
    /**����ҿ��Ĵ浵���ƣ�ע�⣬�ⲻ���ļ�����*/
    public @Nullable String name;

    public String description = "����ǲ��Կ���û��д����";
    /**�浵�ı�����֧��*/
    public final Seq<ArchiveNode> children = new Seq<>();

    public GameData config;

    public Fi data;

    public boolean current = false;

    public ShowNodeDialog dialog = new ArchiveInfoDialog();
    /**md����Ӧ����㿼��*/
    public NodeType type = NodeType.node;

    public ArchiveNode(String fileName, ArchiveNode... children){
        if (children.length != 0) this.children.add(children);

        if (fileName != null) {
            config = GameData.read(SaveArchive.archiveDirectory.child(fileName + ".dat"));
            data = SaveArchive.archiveDirectory.child(fileName + ".zip");
        }
    }

    public void setDialog(ShowNodeDialog bd){
        this.dialog = bd;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ArchiveNode{" +
                "icon=" + icon +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", children=" + children +
                '}';
    }
}
