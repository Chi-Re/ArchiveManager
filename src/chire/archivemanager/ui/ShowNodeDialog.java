package chire.archivemanager.ui;

import chire.archivemanager.ui.tree.ArchiveNode;
import mindustry.ui.dialogs.BaseDialog;

/**ֻ��Ϊ����չ��(����б�Ҫ��)*/
public abstract class ShowNodeDialog extends BaseDialog {
    public ShowNodeDialog(String title) {
        super(title);
    }

    /**Ϊ����չ��*/
    public void show(ArchiveNode config){
        show();
    }
}
