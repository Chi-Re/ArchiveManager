package chire.archivemanager.archive.sector;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.struct.ArrayMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Time;
import chire.archivemanager.game.SectorSlot;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.game.Saves;
import mindustry.game.Schematics;
import mindustry.game.Teams;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.io.SaveIO;
import mindustry.maps.SectorDamage;
import mindustry.net.WorldReloader;
import mindustry.type.Sector;
import mindustry.world.Tile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import static chire.archivemanager.ArchiveManager.archive;
import static chire.archivemanager.ArchiveManager.sectorDirectory;
import static mindustry.Vars.*;

public class Sectors {
    private final ArrayMap<String, Seq<SectorSlot>> archiveSectorMaps = new ArrayMap<>();

    public void init(){
        if (!sectorDirectory.exists()) return;

        for (var f : sectorDirectory.list()) {
            if (f.exists() && f.isDirectory())
                archiveSectorMaps.put(f.nameWithoutExtension(), getBackups(f.nameWithoutExtension()));
        }

        archive.addBackupFile(sectorDirectory.list());
    }

    private Seq<SectorSlot> getBackups(String sectorPath) {
        Fi sectorFile = sectorDirectory.child(sectorPath);
        Seq<SectorSlot> sectors = new Seq<>();

        for (var sf : sectorFile.list()) {
            sectors.add(new SectorSlot(SaveIO.getMeta(SaveIO.getStream(sf)), sf, false));
        }

        return sectors;
    }

    public @Nullable Seq<SectorSlot> getLoadeds(Sector sector){
        return archiveSectorMaps.get(sector.planet.name + "-" + sector.id);
    }

    private void savingSector(Sector sector){
        if (sector.save != null && sector.save.file != null){
            try{
                sector.save.save();
            }catch(Throwable e){
                //TODO 错误处理
            }
        }
    }

    public void load(Sector sector, SectorSlot slot){
        try{
            control.saves.getCurrent().save();
        }catch(Throwable e){
            e.printStackTrace();
            ui.showException("[accent]" + Core.bundle.get("savefail"), e);
        }
        logic.reset();
        ui.paused.hide();

        slot.file.copyTo(sector.save.file);

        this.playSector(sector, slot);
        //state.won;

        ui.planet.hide();

//        ui.paused.hide();
//
//        logic.reset();
//
//        slot.file.copyTo(sector.save.file);
//        control.playSector(sector);
//        logic.play();
//        state.wave = 2;
//        state.wavetime = slot.meta.rules.waveSpacing;
//        control.saves.resetSave();
    }

    public void playSector(Sector sector, SectorSlot backup) {
        this.playSector(sector, sector, backup);
    }

    public void playSector(@Nullable Sector origin, Sector sector, SectorSlot backup) {
        this.playSector(origin, sector, new WorldReloader(), backup);
    }

    void playSector(@Nullable Sector origin, Sector sector, WorldReloader reloader, SectorSlot backup) {
        Vars.ui.loadAnd(() -> {
            if (control.saves.getCurrent() != null && Vars.state.isGame()) {
                Vars.control.saves.getCurrent().save();
                Vars.control.saves.resetSave();
            }

            if (sector.preset != null) {
                sector.preset.quietUnlock();
            }

            Vars.ui.planet.hide();
            Saves.SaveSlot slot = sector.save;
            sector.planet.setLastSector(sector);
            if (slot != null && !Vars.clearSectors && (!sector.planet.clearSectorOnLose || sector.info.hasCore)) {
                try {
                    boolean hadNoCore = !sector.info.hasCore;
                    reloader.begin();
                    slot.load();
                    slot.setAutosave(true);
                    Vars.state.rules.sector = sector;
                    Vars.state.rules.cloudColor = sector.planet.landCloudColor;
                    state.wave = slot.meta.wave;
                    state.rules.waves = slot.meta.wave < slot.meta.rules.winWave;
                    if (!Vars.state.rules.defaultTeam.cores().isEmpty() && !hadNoCore) {
                        Vars.state.set(GameState.State.playing);
                        reloader.end();
                    } else if (sector.planet.clearSectorOnLose) {
                        control.playNewSector(origin, sector, reloader);
                    } else {
                        if (sector.info.spawnPosition == 0) {
                            sector.save = null;
                            slot.delete();
                            this.playSector(origin, sector, reloader, backup);
                            return;
                        }

                        Tile spawn = Vars.world.tile(sector.info.spawnPosition);
                        spawn.setBlock(sector.planet.defaultCore, Vars.state.rules.defaultTeam);
                        SectorDamage.apply(1.0F);
                        Vars.state.wave = 1;
                        Vars.state.wavetime = Vars.state.rules.initialWaveSpacing <= 0.0F ? Vars.state.rules.waveSpacing * (sector.preset == null ? 2.0F : sector.preset.startWaveTimeMultiplier) : Vars.state.rules.initialWaveSpacing;
                        sector.info.wasCaptured = false;
                        if (Vars.state.rules.sector.planet.allowWaves) {
                            Vars.state.rules.waves = true;
                            Vars.state.rules.winWave = Vars.state.rules.attackMode ? -1 : (sector.preset != null && sector.preset.captureWave > 0 ? sector.preset.captureWave : (Vars.state.rules.winWave > Vars.state.wave ? Vars.state.rules.winWave : 30));
                        }

                        if (Vars.state.rules.attackMode) {

                            for (Teams.BlockPlan plan : Vars.state.rules.waveTeam.data().plans) {
                                Tile tile = Vars.world.tile(plan.x, plan.y);
                                if (tile != null) {
                                    tile.setBlock(Vars.content.block(plan.block), Vars.state.rules.waveTeam, plan.rotation);
                                    if (plan.config != null && tile.build != null) {
                                        tile.build.configureAny(plan.config);
                                    }
                                }
                            }

                            Vars.state.rules.waveTeam.data().plans.clear();
                        }

                        Groups.unit.clear();
                        Groups.fire.clear();
                        Groups.puddle.clear();
                        Vars.state.rules.defaultTeam.data().unitCap = 0;
                        Schematics.placeLaunchLoadout(spawn.x, spawn.y);
                        Vars.player.set((float)(spawn.x * 8), (float)(spawn.y * 8));
                        Core.camera.position.set(Vars.player);
                        Events.fire(new EventType.SectorLaunchEvent(sector));
                        Events.fire(EventType.Trigger.newGame);
                        Vars.state.set(GameState.State.playing);
                        reloader.end();
                    }
                } catch (SaveIO.SaveException var10) {
                    Log.err(var10);
                    sector.save = null;
                    Time.runTask(10.0F, () -> {
                        Vars.ui.showErrorMessage("@save.corrupted");
                    });
                    slot.delete();
                    this.playSector(origin, sector, backup);
                }

                Vars.ui.planet.hide();
            } else {
                control.playNewSector(origin, sector, reloader);
            }

        });
    }

    public void save(Sector sector){
        savingSector(sector);

        LocalDateTime time = time();
        String key = time.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        if (sector.planet != null) {
            sector.save.file.copyTo(sectorDirectory.child(sector.planet.name + "-" + sector.id).child(key));

            seqAdd(sector, new SectorSlot(sector.save.file, false));
        } else {
            //TODO 以后加存档地图的其他逻辑
        }
    }

    public void delete(Sector sector, SectorSlot slot){
        seqRemove(sector, slot);
        if (slot.file.isDirectory() && !slot.file.exists()) {
            //TODO 修改报错反馈
            Log.warn("区块备份文件[" + slot.file.path() + "]错误！删除失败");
            return;
        }
        slot.file.delete();
    }

    private void seqAdd(String key, SectorSlot slot){
        var sectorMaps = archiveSectorMaps.get(key);
        if (sectorMaps != null) {
            sectorMaps.add(slot);
            archiveSectorMaps.removeKey(key);
            archiveSectorMaps.put(key, sectorMaps);
        } else {
            sectorMaps = new Seq<>();
            sectorMaps.add(slot);
            archiveSectorMaps.put(key, sectorMaps);
        }
    }

    private void seqAdd(Sector sector, SectorSlot slot) {
        seqAdd(sector.planet.name + "-" + sector.id, slot);
    }

    private void seqRemove(String key, SectorSlot slot){
        var sectorMaps = archiveSectorMaps.get(key);
        sectorMaps.remove(slot);
        archiveSectorMaps.removeKey(key);
        archiveSectorMaps.put(key, sectorMaps);
    }

    private void seqRemove(Sector sector, SectorSlot slot) {
        seqRemove(sector.planet.name + "-" + sector.id, slot);
    }

    public LocalDateTime time(){
        return LocalDateTime.now();
    }
}
