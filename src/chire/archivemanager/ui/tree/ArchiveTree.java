package chire.archivemanager.ui.tree;

import arc.files.Fi;
import arc.struct.Seq;
import chire.archivemanager.ui.NodeNewDialog;
import mindustry.gen.Icon;

public class ArchiveTree {
    //TODO 这是个危险的东西
//    private static ArchiveNode context = null;
//
//    public static Seq<ArchiveNode> roots = new Seq<>();
//
//    public static ArchiveNode nodeRoot(String name, String description, Fi archiveDirectory, Runnable children){
//        var root = node(archiveDirectory, children);
//        root.name = name;
//        root.description = description;
//        return root;
//    }
//
//    public static ArchiveNode node(Fi archiveDirectory, Runnable children){
//        var node = new ArchiveNode(context, archiveDirectory);
//
//        ArchiveNode prev = context;
//        context = node;
//        children.run();
//        context = prev;
//
//        return node;
//    }


    public static ArchiveNode nodeRoot(String name, String description, String fileName, ArchiveNode... children){
        var root = node(fileName, children);
        root.name = name;
        root.description = description;
        root.icon = Icon.tree;
        root.setType(NodeType.core_node);
        return root;
    }

    public static ArchiveNode node(String fileName, ArchiveNode... children){
        return new ArchiveNode(fileName, children);
    }

    public static ArchiveNode nodeNew(){
        var an = new ArchiveNode(null);
        an.icon = Icon.add;
        an.name = "创建存档";
        an.setDialog(new NodeNewDialog());
        an.setType(NodeType.node_new);

        return an;
    }

//    public static ArchiveNode nodeRoot(String name, String description, Fi archiveDirectory, Runnable children){
//        var root = node(content, content.researchRequirements(), children);
//        root.name = name;
//        root.requiresUnlock = requireUnlock;
//        roots.add(root);
//        return root;
//    }

//    public static ArchiveNode node(UnlockableContent content, Runnable children){
//        return node(content, content.researchRequirements(), children);
//    }
//
//    public static ArchiveNode node(UnlockableContent content, ItemStack[] requirements, Runnable children){
//        return node(content, requirements, null, children);
//    }

//    public static ArchiveNode node(UnlockableContent content, ItemStack[] requirements, Seq<Objectives.Objective> objectives, Runnable children){
//        ArchiveNode node = new ArchiveNode(context, content, requirements);
//        if(objectives != null){
//            node.objectives.addAll(objectives);
//        }
//
//        ArchiveNode prev = context;
//        context = node;
//        children.run();
//        context = prev;
//
//        return node;
//    }

//    public static ArchiveNode node(UnlockableContent content, Seq<Objectives.Objective> objectives, Runnable children){
//        return node(content, content.researchRequirements(), objectives, children);
//    }
//
//    public static ArchiveNode node(UnlockableContent block){
//        return node(block, () -> {});
//    }
//
//    public static TechTree.TechNode nodeProduce(UnlockableContent content, Seq<Objectives.Objective> objectives, Runnable children){
//        return node(content, content.researchRequirements(), objectives.add(new Objectives.Produce(content)), children);
//    }
//
//    public static ArchiveNode nodeProduce(UnlockableContent content, Runnable children){
//        return nodeProduce(content, new Seq<>(), children);
//    }
}
