package chire.archivemanager.ui;

import arc.Core;
import arc.files.Fi;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.Drawable;
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
import mindustry.game.MapObjectives;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.mod.Mods;
import mindustry.ui.BorderImage;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import java.io.IOException;

import static chire.archivemanager.ArchiveManager.*;
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
                SaveDialog saveDialog = new SaveDialog(this::show);
                saveDialog.show();
                hide();
            }).margin(margin);

            buttons.button("@archive.import", Icon.add, style, () -> {

            }).margin(margin);
        }).width(w);

        cont.row();

        if(!archive.list().isEmpty()){
            cont.pane(c -> {
                Table table = c.margin(10f).top();

                table.clear();

                table.image().growX().height(4f).pad(6f).color(Pal.gray).row();
                for (LoadedArchive item : archive.list()) {
                    table.row();
//                    table.image().growX().height(4f).pad(6f).color(Pal.gray).row();
                    Drawable background = Styles.flatBordert.up;
                    if (item.last()) {
                        background = Styles.flatBordert.over;
                    }
                    table.table(background, t -> {
                        t.top().left();
                        t.margin(12f);
                        t.defaults().left().top();

                        t.table(title1 -> {
                            title1.top().left();
                            title1.defaults().left().top();
                            title1.add("[accent]"+item.name()).row();
                            title1.add(getBundle("archives.info.time")+":[lightgray]"+item.parseTime()).row();
                            title1.add(getBundle("archives.info.arc")+":"+
                                    (item.arc() ? "[lightgray]学术端" : "[lightgray]原版") + " | " + item.gameVersion()
                            ).row();
                            if (item.last()) title1.add("[green]已加载");
                        }).left().top();

                        t.table(right -> {
                            right.right();

                            right.image().growY().width(4f).color(Pal.gray);

                            right.table(right2 -> {
                                //加载存档
                                right2.button(Icon.play, Styles.clearNonei, ()->{
                                    try {
                                        archive.load(item);
                                        setup();
                                        reload();
                                    } catch (Exception ex) {
                                        Log.warn("存档加载错误！");
                                        Log.warn(ex.toString());
                                        ui.showException("存档加载时出现错误！", ex);
                                    }
                                }).size(50f).disabled(false).get().addListener(new Tooltip(o -> {
                                    o.background(Tex.button).add(Core.bundle.get("archives.play.tooltip"));
                                }));

                                //删除存档
                                right2.button(Icon.trash, Styles.clearNonei, ()->{
                                    ui.showInfoOnHidden("你确定要删除吗?", ()->{
                                        archive.delete(item);
                                        setup();
                                    });
                                }).size(50f).get().addListener(new Tooltip(o -> {
                                    o.background(Tex.button).add(Core.bundle.get("archives.delete.tooltip"));
                                }));

                                //分界线
                                right2.row();

                                //导出存档
                                right2.button(Icon.upload, Styles.clearNonei, ()->{
                                    if(ios){
                                        Fi file = Core.files.local("mindustry-data-export.zip");
                                        try{
                                            archive.export(item, file);
                                        }catch(Exception e){
                                            ui.showException(e);
                                        }
                                        platform.shareFile(file);
                                    }else{
                                        platform.showFileChooser(false, "zip", file -> {
                                            try{
                                                archive.export(item, file);
                                                ui.showInfo("@data.exported");
                                            }catch(Exception e){
                                                e.printStackTrace();
                                                ui.showException(e);
                                            }
                                        });
                                    }
                                }).size(50f).get().addListener(new Tooltip(o -> {
                                    o.background(Tex.button).add(Core.bundle.get("archives.export.tooltip"));
                                }));

                                //存档详情
                                right2.button(Icon.info, Styles.clearNonei, ()->{
                                    infoDialog.show(item);
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

    private void reload(){
        ui.showInfoOnHidden("@archives.reloadexit", () -> {
            Log.info("Exiting to reload archives.");
            Core.app.exit();
        });
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