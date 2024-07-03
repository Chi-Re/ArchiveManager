package chire.archivemanager.ui;

import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import chire.archivemanager.ui.tree.ArchiveNode;

import static mindustry.Vars.iconXLarge;

public class ArchiveInfoDialog extends ShowNodeDialog {
    public ArchiveInfoDialog() {
        super("@info.title");

        addCloseButton();
    }

    public void show(ArchiveNode config){
        cont.clear();

        Table table = new Table();
        table.margin(10);

        table.table(title1 -> {
            title1.image(config.icon).size(iconXLarge).scaling(Scaling.fit);
            title1.add("[accent]" + config.name).padLeft(5);
        });

        table.row();

        if(config.description != null){
            table.add("[lightgray]" + config.description).wrap().fillX().padLeft(10).width(500f).padTop(0).left();
            table.row();
        }



        ScrollPane pane = new ScrollPane(table);
        cont.add(pane);

        show();
    }
}
