package chire.archivemanager;

import arc.Core;
import arc.Settings;
import arc.files.Fi;
import chire.archivemanager.archive.Archives;
import chire.archivemanager.io.DataFile;
import chire.archivemanager.ui.ArchiveDialog;
import chire.archivemanager.ui.ArchiveInfoDialog;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.SettingsMenuDialog;

import java.io.IOException;

public class ArchiveManager extends Mod {
    public static String modName;

    public static ArchiveDialog archiveDialog;

    public static ArchiveInfoDialog infoDialog;

    public static Archives archive;

    public static Fi archiveDirectory = Vars.dataDirectory.child("archives");

    public static DataFile data;

    @Override
    public void loadContent(){
        archive = new Archives();
        data = new DataFile(archiveDirectory.child("setting.dat"));
        if (data.getFile().exists()){
            try {
                data.loadValues();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            archive.init();
        }
    }

    @Override
    public void init() {
        modName = Vars.mods.getMod(ArchiveManager.class).name;

        infoDialog = new ArchiveInfoDialog();

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
