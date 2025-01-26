package chire.archivemanager.ui;

import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import chire.archivemanager.archive.LoadedArchive;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.dialogs.BaseDialog;

import static arc.Core.settings;
import static mindustry.Vars.iconXLarge;

public class ArchiveInfoDialog extends BaseDialog {

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

        table.add("@editor.name").color(Pal.accent).fillX().padTop(10);
        table.row();

        table.add("[lightgray]" + archive.name()).wrap().fillX().padLeft(10).width(500f).padTop(0).left();
        table.row();

        if (archive.time() != null){
            table.add("@editor.time").color(Pal.accent).fillX().padTop(10);
            table.row();

            table.add("[lightgray]" + archive.parseTime()).wrap().fillX().padLeft(10).width(500f).padTop(0).left();
            table.row();
        }

        ScrollPane pane = new ScrollPane(table);
        cont.add(pane);

        show();
    }
}
