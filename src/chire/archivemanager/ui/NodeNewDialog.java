package chire.archivemanager.ui;

import chire.archivemanager.archive.SaveArchive;

public class NodeNewDialog extends ShowNodeDialog{
    public NodeNewDialog() {
        super("@nodeNew.title");

        cont.button("´´½¨", SaveArchive::save);

        addCloseButton();
    }
}
