package chire.archivemanager.ui.dialogs.sector;

import arc.Core;
import arc.scene.style.Drawable;
import arc.scene.ui.Image;
import arc.scene.ui.TextButton;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import chire.archivemanager.game.SectorSlot;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.Sector;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static chire.archivemanager.ArchiveManager.*;
import static mindustry.Vars.ui;

public class ArchiveSectorDialog extends BaseDialog {
    private float scroll = 0f;

    public ArchiveSectorDialog() {
        super("@sectors.save");

        addCloseButton();
    }

    public void show(Sector sector){
        float h = 110f;
        float w = Math.min(Core.graphics.getWidth() / Scl.scl(1.05f), 520f);

        cont.clear();

        cont.table(buttons -> {
            buttons.left().defaults().growX().height(60f).uniformX();

            TextButton.TextButtonStyle style = Styles.flatBordert;
            float margin = 12f;

            buttons.button("@archive.create", Icon.save, style, () -> {
                archiveSectors.save(sector);
                hide();
                show(sector);
            }).margin(margin);
        }).width(w);

        cont.row();

        Seq<SectorSlot> as = archiveSectors.getLoadeds(sector);
        if (as != null && as.size != 0) {
            cont.pane(c -> {
                Table table = c.margin(10f).top();

                table.clear();

                for (SectorSlot item : as) {
                    table.row();

                    Drawable background = Styles.flatBordert.up;
                    table.table(background, t -> {
                        t.top().left();
                        t.margin(12f);
                        t.defaults().left().top();

                        t.table(title1 -> {
                            title1.top().left();
                            title1.defaults().left().top();

                            title1.add(new Image(Icon.editor));

                            title1.add("[accent]"+item.name()).row();
                        }).left().top();

                        t.table(right -> {
                            right.right();

                            right.table(right2 -> {
                                right2.button(Icon.play, Styles.clearNonei, ()->{
                                    archiveSectors.load(sector, item);
                                }).size(50f).disabled(false).get().addListener(new Tooltip(o -> {
                                    o.background(Tex.button).add(Core.bundle.get("archives.play.tooltip"));
                                }));

                                right2.button(Icon.trash, Styles.clearNonei, ()->{
                                    ui.showInfoOnHidden("你确定要删除吗?", ()->{
                                        archiveSectors.delete(sector, item);
                                        hide();
                                        show(sector);
                                    });
                                }).size(50f).get().addListener(new Tooltip(o -> {
                                    o.background(Tex.button).add(Core.bundle.get("archives.delete.tooltip"));
                                }));

                                right2.button(Icon.info, Styles.clearNonei, ()->{

                                }).size(50f).get().addListener(new Tooltip(o -> {
                                    o.background(Tex.button).add(Core.bundle.get("archives.info.tooltip"));
                                }));
                            }).padRight(-8f).padTop(-8f);
                        }).growX().right();
                    }).width(w).growX().pad(4f);
                }
            }).scrollX(false).update(s -> scroll = s.getScrollY()).get().setScrollYForce(scroll);
        }else{
            cont.table(Styles.black6, t -> t.add("@archives.none")).height(80f);
        }

        cont.row();

        show();
    }
}
