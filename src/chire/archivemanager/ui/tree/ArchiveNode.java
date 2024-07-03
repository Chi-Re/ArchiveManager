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
    /**emm，这似乎没有意义？*/
    public @Nullable Drawable icon = Icon.save;
    /**给玩家看的存档名称，注意，这不是文件名称*/
    public @Nullable String name;

    public String description = "这个是测试看看没有写简介的";
    /**存档的保存子支点*/
    public final Seq<ArchiveNode> children = new Seq<>();

    public GameData config;

    public Fi data;

    public boolean current = false;

    public ShowNodeDialog dialog = new ArchiveInfoDialog();
    /**md，我应该早点考虑*/
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
