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

import static mindustry.Vars.content;

public class GameData implements Serializable {
    /**�浵����Դ����*/
    public ArrayMap<Item, Integer> itemStorage = new ArrayMap<>();
    /**�浵��ռ���ͼ*/
    public Seq<Sector> sectors = new Seq<>();
    /**�浵��ռ���ͼ*/
    public Seq<Mods.LoadedMod> mods = new Seq<>();

    public GameData(){
        this.init();
    }

    public void init(){
        //��ʼ��itemStorage
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

        //��ʼ��sectors
        sectors = Vars.ui.planet.state.planet.sectors.select(Sector::hasBase);

        //��ʼ��mods
        for (var item : Vars.mods.list()) {
            //item.isSupported() && !item.hasUnmetDependencies() && !item.hasContentErrors() && item.enabled()
            mods.add(item);
        }
    }

    public static void save(Fi file, GameData gd){
        DataFile dat = new DataFile(file);

        //dat.putClass("itemStorage", ArrayMap.class, itemStorage);
        //dat.putClass("sectors", Seq.class, sectors);
        //dat.putClass("mods", Seq.class, mods);
        dat.putClass("data", gd.getClass(), gd);

        dat.saveValues();
    }

    public static GameData read(Fi file){
        if (!file.exists()) throw new RuntimeException("���ڶ�ȡ�������ļ������ڣ�");
        DataFile dat = new DataFile(file);
        try {
            dat.loadValues();
        } catch (IOException e){
            Vars.ui.showException(e);
        }
        return dat.getDataClass("data", GameData.class);
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
