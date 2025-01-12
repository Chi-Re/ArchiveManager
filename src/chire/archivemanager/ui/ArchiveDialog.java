package chire.archivemanager.ui;

import arc.Core;
import arc.files.Fi;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.TextButton;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.ArrayMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Strings;
import chire.archivemanager.archive.Archives;
import chire.archivemanager.archive.LoadedArchive;
import chire.archivemanager.io.DataFile;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.mod.Mods;
import mindustry.ui.BorderImage;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import java.io.IOException;

import static chire.archivemanager.ArchiveManager.archive;
import static mindustry.Vars.*;
import static mindustry.Vars.mobile;

public class ArchiveDialog extends BaseDialog {
    private float scroll = 0f;

    public ArchiveDialog() {
        super("@archive.dialog.title");
        addCloseButton();

        shown(this::setup);
        onResize(this::setup);
    }

    void setup(){
        float h = 110f;
        float w = Math.min(Core.graphics.getWidth() / Scl.scl(1.05f), 520f);

        cont.clear();

        cont.table(buttons -> {
            buttons.left().defaults().growX().height(60f).uniformX();

            TextButton.TextButtonStyle style = Styles.flatBordert;
            float margin = 12f;

            buttons.button("@archive.create", Icon.save, style, () -> {
                archive.save();
                setup();
                ui.showInfo("@data.exported");//backup
            }).margin(margin);

            buttons.button("@archive.import", Icon.add, style, () -> {

            }).margin(margin);

            buttons.button("??????", ()->{
                platform.showFileChooser(true, "dat", file -> {
                    try {
                        var a = new DataFile(file);
                        a.loadValues();
                        ui.showErrorMessage(a.getObject("name") + "," + a.getObject("time"));
                    } catch (IOException e) {
                        e.printStackTrace();
                        ui.showException("@save.import.fail", e);
                    }
                });

//                var a = new DataFile(SaveArchive.archiveDirectory.child("202409151846").child("current.dat"));
//                try {
//                    a.loadValues();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                Log.info(a.getDataClass("same", ArrayMap.class));
            }).margin(margin);
        }).width(w);

        cont.row();

        if(!archive.list().isEmpty()){
            cont.pane(e -> {
                Table table = e.margin(10f).top();

                table.clear();

                table.image().growX().height(4f).pad(6f).color(Pal.gray).row();
                for (LoadedArchive item : archive.list()) {
                    table.row();
//                    table.image().growX().height(4f).pad(6f).color(Pal.gray).row();
                    table.table(Styles.flatBordert.up, t -> {
                        t.top().left();
                        t.margin(12f);
                        t.defaults().left().top();

                        t.table(title1 -> {
                            title1.top().left();
                            title1.defaults().left().top();
                            title1.add("[accent]"+item.name()).row();
                            title1.add("[lightgray]"+item.time());
                        }).left().top();

                        t.table(right -> {
                            right.right();

                            right.image().growY().width(4f).color(Pal.gray);

                            right.table(right2 -> {
                                //加载存档
                                right2.button(Icon.play, Styles.clearNonei, ()->{

                                }).size(50f).disabled(false).get().addListener(new Tooltip(o -> {
                                    o.background(Tex.button).add(Core.bundle.get("archives.play.tooltip"));
                                }));

                                //删除存档
                                right2.button(Icon.trash, Styles.clearNonei, ()->{

                                }).size(50f).get().addListener(new Tooltip(o -> {
                                    o.background(Tex.button).add(Core.bundle.get("archives.trash.tooltip"));
                                }));

                                //分界线
                                right2.row();

                                //上传存档(网络)
                                right2.button(Icon.up, Styles.clearNonei, ()->{

                                }).size(50f).get().addListener(new Tooltip(o -> {
                                    o.background(Tex.button).add(Core.bundle.get("archives.up.tooltip"));
                                }));

                                //存档详情
                                right2.button(Icon.info, Styles.clearNonei, ()->{
                                    showArchive(item);
                                }).size(50f).get().addListener(new Tooltip(o -> {
                                    o.background(Tex.button).add(Core.bundle.get("archives.info.tooltip"));
                                }));
                            }).padRight(-8f).padTop(-8f);
                        }).growX().right();
                    }).size(w, h).growX().pad(4f);//showArchive(item) Styles.flatBordert
                }
            }).scrollX(false).update(s -> scroll = s.getScrollY()).get().setScrollYForce(scroll);
        }else{
            cont.table(Styles.black6, t -> t.add("@archives.none")).height(80f);
        }

        cont.row();
    }

    private String getStateDetails(LoadedArchive item){
        return "null";
    }

    public void showArchive(LoadedArchive archive){
        BaseDialog dialog = new BaseDialog(archive.name());

        dialog.addCloseButton();

        dialog.cont.pane(desc -> {
            desc.center();
            desc.defaults().padTop(10).left();

            desc.add("@editor.name").padRight(10).color(Color.gray).padTop(0);
            desc.row();
            desc.add(archive.name()).growX().wrap().padTop(2);
            desc.row();
        }).width(400f);

        dialog.show();
    }
}