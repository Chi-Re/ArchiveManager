package chire.archivemanager.ui.layout;

import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.actions.Actions;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Time;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

public class StatLabel extends Table {
    private float progress = 0;

    public StatLabel(Element stat, int value, float delay){
        setTransform(true);
        setClip(true);
        setBackground(Tex.whiteui);
        setColor(Pal.accent);
        margin(2f);

        Label valueLabel = new Label("", Styles.outlineLabel);
        valueLabel.setAlignment(Align.right);

        add(stat).left().growX().padLeft(5);
        add(valueLabel).right().growX().padRight(5);

        actions(
                Actions.scaleTo(0, 1),
                Actions.delay(delay),
                Actions.parallel(
                        Actions.scaleTo(1, 1, 0.3f, Interp.pow3Out),
                        Actions.color(Pal.darkestGray, 0.3f, Interp.pow3Out),
                        Actions.sequence(
                                Actions.delay(0.3f),
                                Actions.run(() -> {
                                    valueLabel.update(() -> {
                                        progress = Math.min(1, progress + (Time.delta / 60));
                                        valueLabel.setText("" + (int) Mathf.lerp(0, value, value < 10 ? progress : Interp.slowFast.apply(progress)));
                                    });
                                })
                        )
                )
        );
    }

    public StatLabel(Element stat, int value) {
        this(stat, value, 0f);
    }
}
