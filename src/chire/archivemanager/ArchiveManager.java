package chire.archivemanager;

import arc.Core;
import arc.util.Log;
import chire.archivemanager.ui.ArchiveDialog;
import chire.archivemanager.ui.tree.ArchiveNode;
import chire.archivemanager.ui.tree.NodeType;
import mindustry.Vars;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.SettingsMenuDialog;

public class ArchiveManager extends Mod {
    public static String modName;

    public static ArchiveDialog archiveDialog;

    @Override
    public void loadContent(){

    }

    @Override
    public void init() {
        modName = Vars.mods.getMod(ArchiveManager.class).name;

        Log.info(modName);

        archiveDialog = new ArchiveDialog();

        if(Vars.ui != null && Vars.ui.settings != null) {
            Vars.ui.settings.addCategory(getBundle("setting-title"), settingsTable -> {
                settingsTable.pref(new SettingsMenuDialog.SettingsTable.Setting(getBundle("archive-dialog-title")) {
                    @Override
                    public void add(SettingsMenuDialog.SettingsTable table) {
                        table.button(name, archiveDialog::show).margin(14).width(200f).pad(6);
                        table.row();
                    }
                });
            });
        }


//        ArchiveNode node = new ArchiveNode("001", NodeType.core_node);
//        node.addNode(new ArchiveNode("001-001"));
//
//        Log.info(node.getChildren());
    }

    public static String getBundle(String str){
        return Core.bundle.format(str);
    }

    public static String name(String add){
        return modName + "-" + add;
    }
}
