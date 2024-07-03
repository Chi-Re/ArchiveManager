package chire.archivemanager;

import arc.Core;
import arc.struct.ArrayMap;
import arc.util.Log;
import chire.archivemanager.archive.GameData;
import chire.archivemanager.io.CRJson;
import chire.archivemanager.io.DataFile;
import chire.archivemanager.ui.ArchiveDialog;
import mindustry.Vars;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.SettingsMenuDialog;

import java.io.IOException;

public class ArchiveManager extends Mod {
    public static String modName;

    public ArchiveDialog archiveDialog;

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

//        var cj = new CRJson(Vars.modDirectory.child("test.json"));
//        cj.writeJson("test1", "dsad", "test2", 12, "test3", false, "test4", 1.2);
//
//        SaveClass.write(cj.toClassVar(ClassData.class), Vars.modDirectory.child("test.dat"));
//
//        Log.info(SaveClass.read(ClassData.class, Vars.modDirectory.child("test.dat")));

//        var df = new DataFile(Vars.modDirectory.child("test.bin"));
//        df.putClass("test", GameData.class, new GameData());
//        df.saveValues();
//        try {
//            df.loadValues();
//            Log.info(df.getDataClass("test", GameData.class));
//        } catch (IOException e) {
//            Log.err(e);
//        }
    }

    public static String getBundle(String str){
        return Core.bundle.format(str);
    }

    public static String name(String add){
        return modName + "-" + add;
    }
}
