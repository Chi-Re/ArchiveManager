package chire.archivemanager.ui.dialogs.archive;

import arc.Core;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Scaling;
import arc.util.Time;
import chire.archivemanager.game.SectorSlot;
import mindustry.core.GameState;
import mindustry.game.Saves;
import mindustry.gen.Icon;
import mindustry.io.MapIO;
import mindustry.io.SaveIO;
import mindustry.ui.BorderImage;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import java.io.IOException;
import java.util.ArrayList;

import static mindustry.Vars.*;
import static mindustry.Vars.ui;

public class SectorsDialog extends BaseDialog {
    Table slots;
    ScrollPane pane;
    public SectorsDialog() {
        super("@archives.info.sectors");
        addCloseButton();
    }

    public void show(ArrayList<SectorSlot> maps){
        cont.clear();

        slots = new Table();
        pane = new ScrollPane(slots);

        slots.clear();
        slots.marginRight(24).marginLeft(20f);

        Time.runTask(2f, () -> Core.scene.setScrollFocus(pane));

        int maxwidth = Math.max((int)(Core.graphics.getWidth() / Scl.scl(470)), 1);
        int i = 0;
        boolean any = false;
        maps.sort((slot, other) -> -Long.compare(slot.getTimestamp(), other.getTimestamp()));

        for (var slot : maps) {
            any = true;

            TextButton button = new TextButton("", Styles.grayt);
            button.getLabel().remove();
            button.clearChildren();

            button.defaults().left();

            button.table(title -> {
                title.add("[accent]" + slot.name() + (slot.isBackup() ? "-backup" : "")).left().growX().width(230f).wrap();

                title.table(t -> {
                    t.right();
                    t.defaults().size(40f);
                    t.button(Icon.export, Styles.emptyi, () -> platform.export("save-" + slot.name(), saveExtension, slot::exportFile)).right();
                }).padRight(-10).growX();
            }).growX().colspan(2);
            button.row();

            String color = "[lightgray]";
            TextureRegion def = Core.atlas.find("nomap");

            button.left().add(new BorderImage(def, 4f)).update(im -> {
                TextureRegionDrawable draw = (TextureRegionDrawable)im.getDrawable();
                if(draw.getRegion().texture.isDisposed()){
                    draw.setRegion(def);
                }

                Texture text = slot.pixmap();
                if(draw.getRegion() == def && text != null){
                    draw.setRegion(new TextureRegion(text));
                }
                im.setScaling(Scaling.fit);
            }).left().size(160f).padRight(6);

            button.table(meta -> {
                meta.left().top();
                meta.defaults().padBottom(-2).left().width(290f);
                meta.row();
                meta.labelWrap(Core.bundle.format("save.map", color + slot.name()));
                meta.row();
                meta.labelWrap(slot.mode().toString() + " /" + color + " " + Core.bundle.format("save.wave", color + slot.getWave()));
                meta.row();
                meta.labelWrap(() -> Core.bundle.format("save.autosave", color + Core.bundle.get(slot.isAutosave() ? "on" : "off")));
                meta.row();
                meta.labelWrap(() -> Core.bundle.format("save.playtime", color + slot.getPlayTime()));
                meta.row();
                meta.labelWrap(color + slot.getDate());
                meta.row();
                if (slot.isBackup()) {
                    meta.labelWrap(() -> Core.bundle.get("save.backup"));
                    meta.row();
                }
            }).left().growX().width(250f);

            slots.add(button).uniformX().fillX().pad(4).padRight(8f).margin(10f);

            if(++i % maxwidth == 0){
                slots.row();
            }
        }

        if(!any){
            slots.add("@save.none");
        }


        pane.setFadeScrollBars(false);
        pane.setScrollingDisabled(true, false);

        cont.add(pane).growY();

        show();
    }
}
