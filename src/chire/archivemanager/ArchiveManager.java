package chire.archivemanager;

import arc.Core;
import chire.archivemanager.archive.Archives;
import chire.archivemanager.ui.ArchiveDialog;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.SettingsMenuDialog;

public class ArchiveManager extends Mod {
    public static String modName;

    public static ArchiveDialog archiveDialog;

    public static Archives archive;

    @Override
    public void loadContent(){
        archive = new Archives();
    }

    @Override
    public void init() {
        modName = Vars.mods.getMod(ArchiveManager.class).name;

        archiveDialog = new ArchiveDialog();

        if(Vars.ui != null && Vars.ui.settings != null) {
            Vars.ui.menufrag.addButton("@archive.button.name", Icon.save, archiveDialog::show);

//            Vars.ui.settings.addCategory(getBundle("setting-title"), settingsTable -> {
//                settingsTable.pref(new SettingsMenuDialog.SettingsTable.Setting(getBundle("archive.dialog.title")) {
//                    @Override
//                    public void add(SettingsMenuDialog.SettingsTable table) {
//                        table.button(name, archiveDialog::show).margin(14).width(200f).pad(6);
//                        table.row();
//                    }
//                });
//            });
        }
    }

    public static String getBundle(String str){
        return Core.bundle.format(str);
    }

    public static String name(String add){
        return modName + "-" + add;
    }
}
