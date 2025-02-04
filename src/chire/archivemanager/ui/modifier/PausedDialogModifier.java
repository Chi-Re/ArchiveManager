package chire.archivemanager.ui.modifier;

import arc.scene.ui.layout.Table;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.Icon;

import static mindustry.Vars.*;

public class PausedDialogModifier {
    Table cont;

    public PausedDialogModifier(){
        cont = Vars.ui.paused.cont;
        Vars.ui.paused.shown(this::rebuild);
    }

    void rebuild(){
        cont.row();

        float dw = 220f;

        if(!mobile){
            if (state.rules.sector != null) cont.button("@sectors.save", Icon.book, ()->{

            }).colspan(2).width(dw + 10f);
        } else {
            if (state.rules.sector != null) cont.buttonRow("@sectors.save", Icon.book, ()->{

            });
        }
    }
}
