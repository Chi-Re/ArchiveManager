package chire.archivemanager.ui;

import chire.archivemanager.ui.tree.ArchiveNode;
import mindustry.ui.dialogs.BaseDialog;

/**只是为了拓展性(真的有必要吗？)*/
public abstract class ShowNodeDialog extends BaseDialog {
    public ShowNodeDialog(String title) {
        super(title);
    }

    /**为了拓展性*/
    public void show(ArchiveNode config){
        show();
    }
}
