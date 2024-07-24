package chire.archivemanager.ui.tree;

import arc.files.Fi;
import arc.scene.style.Drawable;
import arc.struct.ArrayMap;
import arc.struct.Seq;
import arc.util.Nullable;
import chire.archivemanager.archive.GameData;
import chire.archivemanager.archive.SaveArchive;
import chire.archivemanager.ui.ArchiveInfoDialog;
import chire.archivemanager.ui.ShowNodeDialog;
import mindustry.gen.Icon;

public class ArchiveNode {
    public static ArrayMap<String, ArchiveNode> nodes = new ArrayMap<>();

    /**emm�����ƺ�û�����壿*/
    public @Nullable Drawable icon = Icon.save;
    /**����ҿ��Ĵ浵���ƣ�ע�⣬�ⲻ���ļ�����*/
    public @Nullable String name;

    public String description = "����ǲ��Կ���û��д����";

    public GameData config;

    public Fi data;

    public boolean current = false;

    public ShowNodeDialog dialog = new ArchiveInfoDialog();
    /**md����Ӧ����㿼��*/
    public NodeType type;

    public String parents;

    public Seq<String> children = new Seq<>();

    public ArchiveNode(String fileName, NodeType type){
        if (fileName != null) {
            //config = GameData.read(SaveArchive.archiveDirectory.child(fileName + ".dat"));
            data = SaveArchive.archiveDirectory.child(fileName + ".zip");
        }
        this.type = type;
        nodes.put(getKey(), this);
    }

    public ArchiveNode(String fileName){
        this(fileName, NodeType.node);
    }

    public ArchiveNode(GameData config, Fi data, NodeType type){
        this.config = config;
        this.data = data;
        this.type = type;
        nodes.put(getKey(), this);
    }

    public ArchiveNode(GameData config, Fi data){
        this(config, data, NodeType.node);
    }

    public void addNode(ArchiveNode node){
        children.add(node.getKey());
        node.parents = this.getKey();
    }

    public void setDialog(ShowNodeDialog bd){
        this.dialog = bd;
    }

    public String getKey(){
        //TODO �����config��
        if (this.data != null) return this.data.path();

        switch (type){
            case node_new -> {
                return "new";
            }
            case core_node -> {
                return "root";
            }
        }
        throw new RuntimeException("�ڵ�մ浵��Ϣ������浵������(ϵͳ����)");
    }

    public void toCurrent(){
        this.current = !this.current;
    }

    public Seq<ArchiveNode> getChildren(){
        Seq<ArchiveNode> nodeChildren = new Seq<>();

        for (String c : children) {
            nodeChildren.add(nodes.get(c));
        }

        return nodeChildren;
    }

    public Seq<String> getNameChildren(){
        return children;
    }

    @Override
    public String toString() {
        return "ArchiveNode{" +
//                "icon=" + icon +
                ", name='" + name + '\'' +
//                ", description='" + description + '\'' +
                ", children=" + children +
                ", type=" + type +
                '}';
    }
}
