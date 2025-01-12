package chire.archivemanager.archive;

import arc.files.Fi;
import arc.struct.ArrayMap;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import chire.archivemanager.io.DataFile;
import mindustry.Vars;
import mindustry.mod.Mods;
import mindustry.type.Item;
import mindustry.type.ItemSeq;
import mindustry.type.Planet;
import mindustry.type.Sector;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import static mindustry.Vars.content;

/**关于玩家游戏的资源,地图,模组等进度数据*/
public class GameData implements Serializable {
    /**存档的资源数量*/
    public ArrayMap<Item, Integer> itemStorage = new ArrayMap<>();
    /**存档的占领地图*/
    public Seq<Sector> sectors = new Seq<>();
    /**存档的模组数量*/
    public Seq<Mods.LoadedMod> mods = new Seq<>();

    public GameData(){
        this.init();
    }

    public void init(){
        //初始化itemStorage
        var items = new ItemSeq(){
            //store sector item amounts for modifications
            ObjectMap<Sector, ItemSeq> cache = new ObjectMap<>();

            {
                for (Planet p : content.planets()) {
                    for (Sector sector : p.sectors) {
                        if (sector.hasBase()) {
                            ItemSeq cached = sector.items();
                            cache.put(sector, cached);
                            cached.each((item, amount) -> {
                                values[item.id] += Math.max(amount, 0);
                                total += Math.max(amount, 0);
                            });
                        }
                    }
                }
            }

            //this is the only method that actually modifies the sequence itself.
            @Override
            public void add(Item item, int amount){
                //only have custom removal logic for when the sequence gets items taken out of it (e.g. research)
                if(amount < 0){
                    //remove items from each sector's storage, one by one

                    //negate amount since it's being *removed* - this makes it positive
                    amount = -amount;

                    //% that gets removed from each sector
                    double percentage = (double)amount / get(item);
                    int[] counter = {amount};
                    cache.each((sector, seq) -> {
                        if(counter[0] == 0) return;

                        //amount that will be removed
                        int toRemove = Math.min((int)Math.ceil(percentage * seq.get(item)), counter[0]);

                        //actually remove it from the sector
                        sector.removeItem(item, toRemove);
                        seq.remove(item, toRemove);

                        counter[0] -= toRemove;
                    });

                    //negate again to display correct number
                    amount = -amount;
                }

                super.add(item, amount);
            }
        };
        for(Item item : content.items()) {
            if (!items.has(item)) continue;

            itemStorage.put(item, items.get(item));
        }

        //初始化sectors
        sectors = Vars.ui.planet.state.planet.sectors.select(Sector::hasBase);

        //初始化mods
        for (var item : Vars.mods.list()) {
            //item.isSupported() && !item.hasUnmetDependencies() && !item.hasContentErrors() && item.enabled()
            mods.add(item);
        }
    }

    public void save(Fi file){
        DataFile dat = new DataFile(file);

//        dat.putClass("itemStorage", ArrayMap.class, itemStorage);
//        dat.putClass("sectors", Seq.class, sectors);
//        dat.putClass("mods", Seq.class, mods);
        dat.putClass("data", this.getClass(), this);

        dat.saveValues();
    }

    public static HashMap<String, Object> read(Fi file){
        if (!file.exists()) throw new RuntimeException("正在读取的数据文件不存在！");
        DataFile dat = new DataFile(file);
        try {
            dat.loadValues();
        } catch (IOException e){
            Vars.ui.showException(e);
        }
        return dat.getValues();
    }

    @Override
    public String toString() {
        return "GameData{" +
                "itemStorage:" + itemStorage +
                ", sectors=" + sectors +
                ", mods=" + mods +
                '}';
    }
}
