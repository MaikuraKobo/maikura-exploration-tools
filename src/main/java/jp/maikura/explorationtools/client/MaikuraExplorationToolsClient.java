package jp.maikura.explorationtools.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

public final class MaikuraExplorationToolsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WorldRenderEvents.END_MAIN.register(context -> RuinScannerGizmoRenderer.render());
    }
}
