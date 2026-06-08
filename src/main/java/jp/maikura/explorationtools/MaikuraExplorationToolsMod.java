package jp.maikura.explorationtools;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class MaikuraExplorationToolsMod implements ModInitializer {
    public static final String MOD_ID = "maikura_exploration_tools";

    public static final Identifier RUIN_SCANNER_ID = Identifier.of(MOD_ID, "ruin_scanner");
    public static final RegistryKey<Item> RUIN_SCANNER_KEY = RegistryKey.of(RegistryKeys.ITEM, RUIN_SCANNER_ID);

    public static final Item RUIN_SCANNER = Registry.register(
            Registries.ITEM,
            RUIN_SCANNER_KEY,
            new RuinScannerItem(new Item.Settings()
                    .registryKey(RUIN_SCANNER_KEY)
                    .maxCount(1))
    );

    @Override
    public void onInitialize() {
        ExplorationToolsConfig.load();
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(RUIN_SCANNER));
    }

    public static boolean isRuinContainerBlock(BlockState state) {
        Identifier id = Registries.BLOCK.getId(state.getBlock());
        String path = id.getPath();

        return state.isOf(Blocks.CHEST)
                || state.isOf(Blocks.TRAPPED_CHEST)
                || state.isOf(Blocks.BARREL)
                || state.getBlock() instanceof ShulkerBoxBlock
                // 宝物庫（Trial Vault / Vault）は、遺跡の宝箱相当としてコンテナカテゴリ（オレンジ）へ寄せる。
                || path.equals("vault")
                || path.equals("trial_vault")
                || path.equals("ominous_vault")
                // MOD追加コンテナの補助判定。最終判定はclient側のBlockEntity instanceof Inventoryも併用。
                || path.contains("chest")
                || path.contains("barrel")
                || path.contains("shulker_box")
                || path.contains("vault");
    }

    public static boolean isRuinArchaeologyBlock(BlockState state) {
        return state.isOf(Blocks.SUSPICIOUS_SAND)
                || state.isOf(Blocks.SUSPICIOUS_GRAVEL);
    }

    public static boolean isRuinSpawnerBlock(BlockState state) {
        Identifier id = Registries.BLOCK.getId(state.getBlock());
        String path = id.getPath();

        // 通常スポナーに加えて、試練のスポナー（trial_spawner）もスポナーカテゴリ（赤枠）として扱う。
        // MOD追加スポナーも path に spawner を含むものは同カテゴリへ寄せる。
        return state.isOf(Blocks.SPAWNER)
                || path.equals("trial_spawner")
                || path.equals("ominous_trial_spawner")
                || path.contains("spawner");
    }

    public static boolean isRuinTargetBlock(BlockState state) {
        return isRuinContainerBlock(state) || isRuinSpawnerBlock(state) || isRuinArchaeologyBlock(state);
    }

    public static class RuinScannerItem extends Item {
        public RuinScannerItem(Settings settings) {
            super(settings);
        }

        @Override
        public ActionResult use(World world, PlayerEntity user, Hand hand) {
            // アウトライン表示はクライアント側処理なので、モードもクライアント側設定として切り替える。
            // これによりマルチ環境でも各プレイヤーごとに見たい対象を選べる。
            if (world.isClient()) {
                ExplorationToolsConfig.ScanMode mode = ExplorationToolsConfig.cycleScanMode();
                user.sendMessage(Text.literal("遺跡探査器 モード: " + mode.displayName()), true);
                user.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.45F, 1.15F);
            }
            return ActionResult.SUCCESS;
        }
    }
}
