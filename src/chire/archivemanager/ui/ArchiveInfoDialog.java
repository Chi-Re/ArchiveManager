package chire.archivemanager.ui;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.ArrayMap;
import arc.util.Align;
import arc.util.Scaling;
import chire.archivemanager.archive.LoadedArchive;
import chire.archivemanager.game.SectorSlot;
import chire.archivemanager.ui.layout.StatLabel;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.ItemImage;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import java.util.ArrayList;

import static mindustry.Vars.content;
import static mindustry.Vars.iconXLarge;

public class ArchiveInfoDialog extends BaseDialog {
    private final ItemStorageDialog itemDialog = new ItemStorageDialog();

    private final SectorsDialog sectorsDialog = new SectorsDialog();

    public ArchiveInfoDialog() {
        super("@archives.info");

        addCloseButton();
    }

    public void show(LoadedArchive archive){
        cont.clear();

        Table table = new Table();
        table.margin(10);

        table.table(title1 -> {
            title1.image(Icon.book).size(iconXLarge).scaling(Scaling.fit);
        });

        table.row();

        table.add("@archives.info.name").color(Pal.accent).fillX().padTop(10);
        table.row();

        table.add("[lightgray]" + archive.name()).wrap().fillX().padLeft(10).width(500f).padTop(0).left();
        table.row();

        if (archive.time() != null){
            table.add("@archives.info.time").color(Pal.accent).fillX().padTop(10);
            table.row();

            table.add("[lightgray]" + archive.parseTime()).wrap().fillX().padLeft(10).width(500f).padTop(0).left();
            table.row();
        }

        if (archive.itemStorage().size > 0) table.button("@archives.info.items", ()->{
            itemDialog.show(archive.itemStorage());
        }).color(Pal.accent).fillX().padTop(10);

        table.row();

        if (archive.sectors().size() > 0) table.button("@archives.info.sectors", ()->{
            sectorsDialog.show(archive.sectors());
        }).color(Pal.accent).fillX().padTop(10);

        table.row();

        table.button("测试", ()->{

        }).color(Pal.accent).fillX().padTop(10);

        ScrollPane pane = new ScrollPane(table);
        cont.add(pane);

        show();
    }
}
