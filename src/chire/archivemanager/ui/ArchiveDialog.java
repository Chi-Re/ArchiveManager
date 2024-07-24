package chire.archivemanager.ui;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.actions.RelativeTemporalAction;
import arc.scene.event.ElementGestureListener;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.TextButton.TextButtonStyle;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.ArrayMap;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Scaling;
import chire.archivemanager.archive.SaveArchive;
import chire.archivemanager.ui.tree.ArchiveNode;
import chire.archivemanager.ui.tree.NodeType;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.input.Binding;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.layout.BranchTreeLayout;
import mindustry.ui.layout.TreeLayout.TreeNode;

import java.util.Arrays;

import static mindustry.Vars.mobile;
import static mindustry.gen.Tex.buttonDown;
import static mindustry.gen.Tex.buttonOver;

public class ArchiveDialog extends BaseDialog {
    public final float nodeSize = Scl.scl(60f);
    public static ArrayMap<String, TechTreeNode> nodes = new ArrayMap<>();
    //public TechTreeNode root = new TechTreeNode(ArchiveTree.nodeRoot("空", "这是简介", null, ()->{}), null);
    //public ArchiveNode lastNode = root.node;
    public TechTreeNode root = null;
    public Rect bounds = new Rect();
    public View view;

    public ArchiveDialog(){
        super("");

        margin(0f).marginBottom(8);
        cont.stack(view = new View()).grow();

        shouldPause = true;

        SaveArchive.loadTree();

        shown(() -> {
            switchTree(SaveArchive.archiveTree);

            //checkNodes(root);
            treeLayout();

            view.hoverNode = null;
            view.infoTable.remove();
            view.infoTable.clear();
        });

        addCloseButton();

        keyDown(key -> {
            if(key == Core.keybinds.get(Binding.research).key){
                Core.app.post(this::hide);
            }
        });

        //scaling/drag input
        addListener(new InputListener(){
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                view.setScale(Mathf.clamp(view.scaleX - amountY / 10f * view.scaleX, 0.25f, 1f));
                view.setOrigin(Align.center);
                view.setTransform(true);
                return true;
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y){
                view.requestScroll();
                return super.mouseMoved(event, x, y);
            }
        });

        touchable = Touchable.enabled;

        addCaptureListener(new ElementGestureListener(){
            @Override
            public void zoom(InputEvent event, float initialDistance, float distance){
                if(view.lastZoom < 0){
                    view.lastZoom = view.scaleX;
                }

                view.setScale(Mathf.clamp(distance / initialDistance * view.lastZoom, 0.25f, 1f));
                view.setOrigin(Align.center);
                view.setTransform(true);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                view.lastZoom = view.scaleX;
            }

            @Override
            public void pan(InputEvent event, float x, float y, float deltaX, float deltaY){
                view.panX += deltaX / view.scaleX;
                view.panY += deltaY / view.scaleY;
                view.moved = true;
                view.clamp();
            }
        });
    }

    public void switchTree(ArchiveNode node){
        //if(lastNode == node || node == null) return;
        nodes.clear();
        root = new TechTreeNode(node);
        view.rebuildAll();
    }

    void treeLayout(){
        float spacing = 20f;
        LayoutNode node = new LayoutNode(root, null);
        LayoutNode[] children = node.children;
        LayoutNode[] leftHalf = Arrays.copyOfRange(node.children, 0, Mathf.ceil(node.children.length/2f));
        LayoutNode[] rightHalf = Arrays.copyOfRange(node.children, Mathf.ceil(node.children.length/2f), node.children.length);

        node.children = leftHalf;
        new BranchTreeLayout(){{
            gapBetweenLevels = gapBetweenNodes = spacing;
            rootLocation = TreeLocation.top;
        }}.layout(node);

        float lastY = node.y;

        if(rightHalf.length > 0){

            node.children = rightHalf;
            new BranchTreeLayout(){{
                gapBetweenLevels = gapBetweenNodes = spacing;
                rootLocation = TreeLocation.bottom;
            }}.layout(node);

            shift(leftHalf, node.y - lastY);
        }

        node.children = children;

        float minx = 0f, miny = 0f, maxx = 0f, maxy = 0f;
        copyInfo(node);

        for(TechTreeNode n : nodes.values()){
            minx = Math.min(n.x - n.width/2f, minx);
            maxx = Math.max(n.x + n.width/2f, maxx);
            miny = Math.min(n.y - n.height/2f, miny);
            maxy = Math.max(n.y + n.height/2f, maxy);
        }
        bounds = new Rect(minx, miny, maxx - minx, maxy - miny);
        bounds.y += nodeSize*1.5f;
    }

    void shift(LayoutNode[] children, float amount){
        for(LayoutNode node : children){
            node.y += amount;
            if(node.children != null && node.children.length > 0) shift(node.children, amount);
        }
    }

    void copyInfo(LayoutNode node){
        node.node.x = node.x;
        node.node.y = node.y;
        if(node.children != null){
            for(LayoutNode child : node.children){
                copyInfo(child);
            }
        }
    }

    boolean selectable(ArchiveNode node){
        //return node.content.unlocked() || !node.objectives.contains(i -> !i.complete());
        return true;
    }

    boolean current(ArchiveNode node){
        //return node.content.locked();
        return node.current;
    }

    class LayoutNode extends TreeNode<LayoutNode>{
        final TechTreeNode node;

        LayoutNode(TechTreeNode node, LayoutNode parent){
            this.node = node;
            this.parent = parent;
            this.width = this.height = nodeSize;
            if(node.getChildren().size != 0){
                children = Seq.with(node.getChildren()).map(t -> new LayoutNode(t, this)).toArray(LayoutNode.class);
            }
        }
    }

    //extends TreeNode<TechTreeNode>
    public class TechTreeNode {
        public final ArchiveNode node;

        public String[] children;

        public float width, height, x, y;

        public final String name;

        public TechTreeNode(ArchiveNode node){
            Log.info(node);
            this.node = node;
            this.name = node.name;
            this.width = this.height = nodeSize;
            nodes.put(name, this);
            children = new String[node.getChildren().size];
            for(int i = 0; i < children.length; i++){
                var n = node.getNameChildren().get(i);
                children[i] = n;
                nodes.put(n, new TechTreeNode(ArchiveNode.nodes.get(n)));
            }
        }

        public Seq<TechTreeNode> getChildren(){
            Seq<TechTreeNode> nodeChildren = new Seq<>();

            for (String c : children) {
                Log.info("name子:"+c);
                nodeChildren.add(nodes.get(c));
            }

            Log.info("子:"+nodeChildren);

            return nodeChildren;
        }
    }

    public class View extends Group{
        public float panX = 0, panY = -200, lastZoom = -1;
        public boolean moved = false;
        public ImageButton hoverNode;
        public Table infoTable = new Table();

        {
            rebuildAll();
        }

        public void rebuildAll(){
            clear();
            hoverNode = null;
            infoTable.clear();
            infoTable.touchable = Touchable.enabled;

            for(TechTreeNode node : nodes.values()){
                ImageButton button = new ImageButton(node.node.icon, Styles.nodei);
                //button.visible(() -> node.visible);
                button.clicked(() -> {
                    if(moved) return;

                    if (node.node.type == NodeType.node_new) {
                        node.node.dialog.show(node.node);
                        rebuild();
                        return;
                    }

                    if(mobile){
                        hoverNode = button;
                        rebuild();
                        float right = infoTable.getRight();
                        if(right > Core.graphics.getWidth()){
                            float moveBy = right - Core.graphics.getWidth();
                            addAction(new RelativeTemporalAction(){
                                {
                                    setDuration(0.1f);
                                    setInterpolation(Interp.fade);
                                }

                                @Override
                                protected void updateRelative(float percentDelta){
                                    panX -= moveBy * percentDelta;
                                }
                            });
                        }
                    }
                    //else if(locked(node.node)){

                    //}
                });
                button.hovered(() -> {
                    if(!mobile && hoverNode != button && node.node.type != NodeType.node_new){
                        hoverNode = button;
                        rebuild();
                    }
                });
                button.exited(() -> {
                    if(!mobile && hoverNode == button && !infoTable.hasMouse() && !hoverNode.hasMouse()){
                        hoverNode = null;
                        rebuild();
                    }
                });
                //button.touchable(() -> !node.visible ? Touchable.disabled : Touchable.enabled);
                button.userObject = node.node;
                button.setSize(nodeSize);
                button.update(() -> {
                    float offset = (Core.graphics.getHeight() % 2) / 2f;
                    button.setPosition(node.x + panX + width / 2f, node.y + panY + height / 2f + offset, Align.center);
                    button.getStyle().up = current(node.node) ? Tex.buttonOver : !selectable(node.node) ? Tex.buttonRed : Tex.button;

                    button.getStyle().imageUp = node.node.icon;
                    button.getImage().setColor(Color.white);
                    button.getImage().setScaling(Scaling.bounded);
                });
                addChild(button);
            }

            if(mobile){
                tapped(() -> {
                    Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                    if(e == this){
                        hoverNode = null;
                        rebuild();
                    }
                });
            }

            setOrigin(Align.center);
            setTransform(true);
            released(() -> moved = false);
        }

        void clamp(){
            float pad = nodeSize;

            float ox = width/2f, oy = height/2f;
            float rx = bounds.x + panX + ox, ry = panY + oy + bounds.y;
            float rw = bounds.width, rh = bounds.height;
            rx = Mathf.clamp(rx, -rw + pad, Core.graphics.getWidth() - pad);
            ry = Mathf.clamp(ry, -rh + pad, Core.graphics.getHeight() - pad);
            panX = rx - bounds.x - ox;
            panY = ry - bounds.y - oy;
        }

        void rebuild(){
            rebuild(null);
        }

        //pass an array of stack indexes that should shine here
        void rebuild(@Nullable boolean[] shine){
            ImageButton button = hoverNode;

            infoTable.remove();
            infoTable.clear();
            infoTable.update(null);

            if(button == null) return;

            ArchiveNode node = (ArchiveNode)button.userObject;

            infoTable.exited(() -> {
                if(hoverNode == button && !infoTable.hasMouse() && !hoverNode.hasMouse()){
                    hoverNode = null;
                    rebuild();
                }
            });

            infoTable.update(() -> infoTable.setPosition(button.x + button.getWidth(), button.y + button.getHeight(), Align.topLeft));

            infoTable.left();
            infoTable.background(Tex.button).margin(8f);

            boolean selectable = selectable(node);

            infoTable.table(b -> {
                b.margin(0).left().defaults().left();

                if(selectable){
                    b.button(Icon.info, Styles.flati, () -> {
                        node.dialog.show(node);
                    }).growY().width(50f);
                }
                b.add().grow();
                b.table(desc -> {
                    desc.left().defaults().left();
                    desc.add(selectable ? node.name : "[accent]???");
                    desc.row();

                }).pad(9);

                //if(locked(node)){
                b.row();

                if (node.current) {
                    b.button("@mods.viewcontent", Icon.zoom, new TextButtonStyle(){{
                        disabled = Tex.button;
                        font = Fonts.def;
                        fontColor = Color.white;
                        disabledFontColor = Color.gray;
                        up = buttonOver;
                        over = buttonDown;
                    }}, () -> {

                    }).disabled(i -> false).growX().height(44f).colspan(3);
                } else {
                    b.button("@save.import", Icon.download, new TextButtonStyle(){{
                        disabled = Tex.button;
                        font = Fonts.def;
                        fontColor = Color.white;
                        disabledFontColor = Color.gray;
                        up = buttonOver;
                        over = buttonDown;
                    }}, () -> {
                        //node.current = true;
                        //在这里写加载的逻辑
                    }).disabled(i -> false).growX().height(44f).colspan(3);
                }
            });

            infoTable.row();
            if(node.description != null && selectable){
                infoTable.table(t -> t.margin(3f).left().labelWrap(node.description).color(Color.lightGray).growX()).fillX();
            }

            addChild(infoTable);

            infoTable.pack();
            infoTable.act(Core.graphics.getDeltaTime());
        }

        @Override
        public void drawChildren(){
            clamp();
            float offsetX = panX + width / 2f, offsetY = panY + height / 2f;
            Draw.sort(true);

            for(TechTreeNode node : nodes.values()){
                //if(!node.visible) continue;
                for(TechTreeNode child : node.getChildren()){
                    //if(!child.visible) continue;
                    boolean current = current(node.node) || current(child.node);
                    Draw.z(current ? 2f : 1f);

                    Lines.stroke(Scl.scl(4f), current ? Pal.accent : Pal.gray);
                    Draw.alpha(parentAlpha);
                    if(Mathf.equal(Math.abs(node.y - child.y), Math.abs(node.x - child.x), 1f) && Mathf.dstm(node.x, node.y, child.x, child.y) <= node.width*3){
                        Lines.line(node.x + offsetX, node.y + offsetY, child.x + offsetX, child.y + offsetY);
                    }else{
                        Lines.line(node.x + offsetX, node.y + offsetY, child.x + offsetX, node.y + offsetY);
                        Lines.line(child.x + offsetX, node.y + offsetY, child.x + offsetX, child.y + offsetY);
                    }
                }
            }

            Draw.sort(false);
            Draw.reset();
            super.drawChildren();
        }
    }
}

