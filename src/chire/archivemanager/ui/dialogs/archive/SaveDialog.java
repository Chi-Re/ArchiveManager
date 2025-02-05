package chire.archivemanager.ui.dialogs.archive;

import arc.scene.ui.TextField;
import arc.util.Log;
import chire.archivemanager.archive.SaveConfig;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;

import static chire.archivemanager.ArchiveManager.archive;
import static chire.archivemanager.ArchiveManager.archiveDialog;
import static mindustry.Vars.ui;

public class SaveDialog extends BaseDialog {
    /**方便存档后打开一些东西*/
    private Runnable hideRunnable;

    public SaveDialog(Runnable hideRunnable) {
        super("@archive.save");

        this.hideRunnable = hideRunnable;

        shown(this::setup);
        onResize(this::setup);
    }

    public SaveDialog(){
        this(()->{});
    }

    private void setup(){
        SaveConfig config = new SaveConfig();
        cont.clear();

        final TextField[] name = new TextField[1];

        cont.table(table -> {
            table.left();
            table.add("@archives.info.name");
            table.add("@optional");
            table.add(":");
            name[0] = table.field("", res -> {}).growX().get();
        }).size(330f, 50f).row();

        buttons.button("@cancel", this::hide).size(width, 64.0F);
        buttons.button("@archive.create", () -> {
            try {
                config.name = name[0].getText();
                archive.save(config);
                hide();
            } catch (Exception e) {
                Log.warn("存档保存错误！");
                Log.warn(e.toString());
                ui.showException("存档保存时出现错误！", e);
            }
        }).size(width, 64.0F);
    }

    @Override
    public void hide() {
        super.hide();

        hideRunnable.run();
    }
}
