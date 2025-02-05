package chire.archivemanager.ui.dialogs.sector;

import arc.Core;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Scl;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class ArchiveSectorDialog extends BaseDialog {
    private float scroll = 0f;

    public ArchiveSectorDialog() {
        super("@sectors.save");

        shown(this::setup);
        onResize(this::setup);

        addCloseListener();
    }

    private void setup(){
        float h = 110f;
        float w = Math.min(Core.graphics.getWidth() / Scl.scl(1.05f), 520f);

        cont.clear();


    }
}
