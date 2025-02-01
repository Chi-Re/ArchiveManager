package chire.archivemanager.ui;

import arc.Core;
import arc.flabel.FLabel;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.actions.Actions;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.ArrayMap;
import arc.util.Align;
import arc.util.Log;
import arc.util.Scaling;
import arc.util.Time;
import chire.archivemanager.archive.LoadedArchive;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.ItemImage;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.GameOverDialog;

import static arc.Core.settings;
import static mindustry.Vars.*;

public class ArchiveInfoDialog extends BaseDialog {
    private ItemStorageDialog itemDialog = new ItemStorageDialog();

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

        Log.info(archive.itemStorage());

        if (archive.itemStorage().size > 0) table.button("物品", ()->{
            itemDialog.show(archive.itemStorage());
        }).color(Pal.accent).fillX().padTop(10);

        ScrollPane pane = new ScrollPane(table);
        cont.add(pane);

        show();
    }

    public static class ItemStorageDialog extends BaseDialog{
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

    private static class StatLabel extends Table {
        private float progress = 0;

        public StatLabel(Element stat, int value, float delay){
            setTransform(true);
            setClip(true);
            setBackground(Tex.whiteui);
            setColor(Pal.accent);
            margin(2f);

            Label valueLabel = new Label("", Styles.outlineLabel);
            valueLabel.setAlignment(Align.right);

            add(stat).left().growX().padLeft(5);
            add(valueLabel).right().growX().padRight(5);

            actions(
                    Actions.scaleTo(0, 1),
                    Actions.delay(delay),
                    Actions.parallel(
                            Actions.scaleTo(1, 1, 0.3f, Interp.pow3Out),
                            Actions.color(Pal.darkestGray, 0.3f, Interp.pow3Out),
                            Actions.sequence(
                                    Actions.delay(0.3f),
                                    Actions.run(() -> {
                                        valueLabel.update(() -> {
                                            progress = Math.min(1, progress + (Time.delta / 60));
                                            valueLabel.setText("" + (int)Mathf.lerp(0, value, value < 10 ? progress : Interp.slowFast.apply(progress)));
                                        });
                                    })
                            )
                    )
            );
        }

        public StatLabel(Element stat, int value) {
            this(stat, value, 0f);
        }
    }
}
