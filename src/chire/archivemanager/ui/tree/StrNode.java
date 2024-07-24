package chire.archivemanager.ui.tree;

import arc.struct.Seq;

public class StrNode {
    private final Seq<String> children = new Seq<>();

    private StrNode parents;

    public final String name;

    public StrNode(StrNode parents, String name){
        this.parents = parents;
        this.name = name;
    }

    public void addNode(StrNode node){

    }

    @Override
    public String toString() {
        return "StrNode{" +
                "children=" + children +
                ", parents=" + parents +
                ", name='" + name + '\'' +
                '}';
    }
}
