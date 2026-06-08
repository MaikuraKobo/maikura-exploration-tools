package jp.maikura.explorationtools.client.config;

import jp.maikura.explorationtools.ExplorationToolsConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class ExplorationToolsConfigScreen extends Screen {
    private final Screen parent;

    public ExplorationToolsConfigScreen(Screen parent) {
        super(Text.literal("Maikura Exploration Tools 設定"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int y = this.height / 6 + 4;

        this.addDrawableChild(ButtonWidget.builder(radiusText(), button -> {
            ExplorationToolsConfig.cycleRadius();
            button.setMessage(radiusText());
        }).dimensions(center - 110, y, 220, 20).build());

        y += 26;
        this.addDrawableChild(ButtonWidget.builder(modeText(), button -> {
            ExplorationToolsConfig.cycleScanMode();
            button.setMessage(modeText());
        }).dimensions(center - 110, y, 220, 20).build());

        y += 26;
        this.addDrawableChild(ButtonWidget.builder(toggleText("コンテナ系（オレンジ）", ExplorationToolsConfig.showContainers), button -> {
            ExplorationToolsConfig.showContainers = !ExplorationToolsConfig.showContainers;
            ExplorationToolsConfig.save();
            button.setMessage(toggleText("コンテナ系（オレンジ）", ExplorationToolsConfig.showContainers));
        }).dimensions(center - 110, y, 220, 20).build());

        y += 24;
        this.addDrawableChild(ButtonWidget.builder(toggleText("スポナー（赤）", ExplorationToolsConfig.showSpawners), button -> {
            ExplorationToolsConfig.showSpawners = !ExplorationToolsConfig.showSpawners;
            ExplorationToolsConfig.save();
            button.setMessage(toggleText("スポナー（赤）", ExplorationToolsConfig.showSpawners));
        }).dimensions(center - 110, y, 220, 20).build());

        y += 24;
        this.addDrawableChild(ButtonWidget.builder(toggleText("発掘対象（青緑）", ExplorationToolsConfig.showArchaeology), button -> {
            ExplorationToolsConfig.showArchaeology = !ExplorationToolsConfig.showArchaeology;
            ExplorationToolsConfig.save();
            button.setMessage(toggleText("発掘対象（青緑）", ExplorationToolsConfig.showArchaeology));
        }).dimensions(center - 110, y, 220, 20).build());

        y += 24;
        this.addDrawableChild(ButtonWidget.builder(toggleText("エンティティ系（水色）", ExplorationToolsConfig.showEntities), button -> {
            ExplorationToolsConfig.showEntities = !ExplorationToolsConfig.showEntities;
            ExplorationToolsConfig.save();
            button.setMessage(toggleText("エンティティ系（水色）", ExplorationToolsConfig.showEntities));
        }).dimensions(center - 110, y, 220, 20).build());

        y += 24;
        this.addDrawableChild(ButtonWidget.builder(toggleText("その他（青）", ExplorationToolsConfig.showOthers), button -> {
            ExplorationToolsConfig.showOthers = !ExplorationToolsConfig.showOthers;
            ExplorationToolsConfig.save();
            button.setMessage(toggleText("その他（青）", ExplorationToolsConfig.showOthers));
        }).dimensions(center - 110, y, 220, 20).build());

        y += 32;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("完了"), button -> close()).dimensions(center - 100, y, 200, 20).build());
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    private static Text radiusText() {
        return Text.literal("探査距離: " + ExplorationToolsConfig.scanRadius + " ブロック（クリックで 16/32/48/64）");
    }

    private static Text modeText() {
        return Text.literal("探査モード: " + ExplorationToolsConfig.scanMode.displayName() + "（右クリックでも切替）");
    }

    private static Text toggleText(String label, boolean value) {
        return Text.literal(label + ": " + (value ? "ON" : "OFF"));
    }
}
