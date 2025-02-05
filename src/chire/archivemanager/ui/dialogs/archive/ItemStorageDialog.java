package chire.archivemanager.ui.dialogs.archive;

import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.ArrayMap;
import arc.util.Align;
import chire.archivemanager.ui.layout.StatLabel;
import mindustry.ui.ItemImage;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static mindustry.Vars.content;

public class ItemStorageDialog extends BaseDialog {
    private boolean lack = true;

    public ItemStorageDialog() {
        super("");
    }

    public void show(ArrayMap<String, Integer> itemStorage){
        buttons.clear();
        cont.clear();

        buttons.margin(10);

        addCloseButton();

        cont.add("@archives.lack").visible(lackMod()).center().get().setAlignment(Align.center);
        cont.row();

        cont.table(t -> {
            t.pane(p -> {
                p.margin(13f);
                p.left().defaults().left();
                p.setBackground(Styles.black3);

                p.table(stats -> {
                    for (var item : itemStorage) {
                        if (content.item(item.key) != null) {
                            addStat(stats, content.item(item.key).uiIcon, item.value);
                        } else {
                            lack = false;
                            addStat(stats, item.key, item.value);
                        }
                    }
                }).top().grow().row();
            }).grow().pad(12).top();
        }).center().minWidth(370).maxSize(600, 550).grow();

        show();
    }

    public boolean lackMod(){
        return lack;
    }

    public void addStat(Table parent, TextureRegion icon, Integer storage) {
        parent.add(new StatLabel(new ItemImage(icon, 0), storage)).top().pad(5).growX().height(50).row();
    }

    public void addStat(Table parent, String stat, Integer storage) {
        Label statLabel = new Label(stat);
        statLabel.setStyle(Styles.outlineLabel);
        statLabel.setWrap(true);
        parent.add(new StatLabel(statLabel, storage)).top().pad(5).growX().height(50).row();
    }
}
