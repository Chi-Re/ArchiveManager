package chire.archivemanager;

import arc.Core;
import arc.files.Fi;
import chire.archivemanager.archive.Archives;
import chire.archivemanager.io.DataFile;
import chire.archivemanager.ui.dialogs.archive.ArchiveDialog;
import chire.archivemanager.ui.dialogs.archive.ArchiveInfoDialog;
import chire.archivemanager.ui.modifier.PausedDialogModifier;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.mod.Mod;

import java.io.IOException;

public class ArchiveManager extends Mod {
    public static String modName;

    public static ArchiveDialog archiveDialog;

    public static ArchiveInfoDialog infoDialog;

    public static Archives archive;

    public static Fi archiveDirectory = Vars.dataDirectory.child("archives");

    public static DataFile data;

    public PausedDialogModifier paused;

    @Override
    public void loadContent(){
    }

    @Override
    public void init() {
        modName = Vars.mods.getMod(ArchiveManager.class).name;

        infoDialog = new ArchiveInfoDialog();

        archiveDialog = new ArchiveDialog();

        paused = new PausedDialogModifier();

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

        //====================loadContent=======================
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

    public static String getBundle(String str){
        return Core.bundle.format(str);
    }

    public static String name(String add){
        return modName + "-" + add;
    }
}
