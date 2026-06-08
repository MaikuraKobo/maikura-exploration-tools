package jp.maikura.explorationtools.client;

import jp.maikura.explorationtools.MaikuraExplorationToolsMod;
import jp.maikura.explorationtools.ExplorationToolsConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.debug.gizmo.GizmoDrawing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class RuinScannerGizmoRenderer {
    private static final int SCAN_INTERVAL_TICKS = 12;
    private static final int MAX_OUTLINES = 160;

    private static final float OUTLINE_WIDTH = 0.60F;
    private static final float OUTER_OUTLINE_WIDTH = 0.90F;

    private static long lastScanTime = Long.MIN_VALUE;
    private static final List<Target> targets = new ArrayList<>();
    private static final Map<UUID, Entity> glowingEntities = new java.util.HashMap<>();

    private RuinScannerGizmoRenderer() {
    }

    public static void render() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) {
            targets.clear();
            glowingEntities.clear();
            return;
        }

        ItemStack mainHand = client.player.getMainHandStack();
        ItemStack offHand = client.player.getOffHandStack();
        if (!mainHand.isOf(MaikuraExplorationToolsMod.RUIN_SCANNER)
                && !offHand.isOf(MaikuraExplorationToolsMod.RUIN_SCANNER)) {
            targets.clear();
            clearGlowing();
            lastScanTime = Long.MIN_VALUE;
            return;
        }

        World world = client.world;
        long time = world.getTime();
        if (lastScanTime == Long.MIN_VALUE || time - lastScanTime >= SCAN_INTERVAL_TICKS) {
            lastScanTime = time;
            rescan(client, world, client.player.getBlockPos());
        }

        for (Target target : targets) {
            DrawStyle outerStyle = DrawStyle.stroked(0xFF000000, OUTER_OUTLINE_WIDTH);
            DrawStyle innerStyle = DrawStyle.stroked(target.outlineColor(), OUTLINE_WIDTH);

            GizmoDrawing.box(target.pos(), target.outerPadding(), outerStyle)
                    .ignoreOcclusion()
                    .withLifespan(14);
            GizmoDrawing.box(target.pos(), target.innerPadding(), innerStyle)
                    .ignoreOcclusion()
                    .withLifespan(14);
        }
    }

    private static void rescan(MinecraftClient client, World world, BlockPos origin) {
        targets.clear();

        List<Target> found = new ArrayList<>();
        scanBlocks(world, origin, found);
        scanEntities(client, world, origin, found);

        found.sort(Comparator.comparingDouble(target -> target.pos().getSquaredDistance(origin)));

        int count = Math.min(MAX_OUTLINES, found.size());
        for (int i = 0; i < count; i++) {
            targets.add(found.get(i));
        }
    }

    private static void scanBlocks(World world, BlockPos origin, List<Target> found) {
        for (BlockPos pos : BlockPos.iterate(
                origin.add(-ExplorationToolsConfig.scanRadius, -ExplorationToolsConfig.scanRadius, -ExplorationToolsConfig.scanRadius),
                origin.add(ExplorationToolsConfig.scanRadius, ExplorationToolsConfig.scanRadius, ExplorationToolsConfig.scanRadius))) {
            BlockState state = world.getBlockState(pos);
            if (MaikuraExplorationToolsMod.isRuinSpawnerBlock(state)) {
                if (ExplorationToolsConfig.shouldShowSpawners()) {
                    found.add(new Target(pos.toImmutable(), 0xFFFF3333, 0.090F, 0.045F));
                }
                continue;
            }

            if (MaikuraExplorationToolsMod.isRuinContainerBlock(state)) {
                if (ExplorationToolsConfig.shouldShowContainers()) {
                    // チェスト/樽/シュルカー/MOD追加チェスト系はオレンジ。
                    found.add(new Target(pos.toImmutable(), 0xFFFF9A00, 0.090F, 0.045F));
                }
                continue;
            }

            if (MaikuraExplorationToolsMod.isRuinArchaeologyBlock(state)) {
                if (ExplorationToolsConfig.shouldShowArchaeology()) {
                    // 怪しい砂/怪しい砂利は、発掘対象として青緑。
                    found.add(new Target(pos.toImmutable(), 0xFF3DD9B8, 0.090F, 0.045F));
                }
                continue;
            }

            if (world.getBlockEntity(pos) instanceof Inventory) {
                if (ExplorationToolsConfig.shouldShowOthers()) {
                    // 醸造台など、収納チェストではないInventory系は青。
                    found.add(new Target(pos.toImmutable(), 0xFF2F8CFF, 0.090F, 0.045F));
                }
            }
        }
    }

    private static void scanEntities(MinecraftClient client, World world, BlockPos origin, List<Target> found) {
        Set<UUID> current = new HashSet<>();
        if (!ExplorationToolsConfig.shouldShowEntities()) {
            clearGlowing();
            return;
        }

        Box box = new Box(origin).expand(ExplorationToolsConfig.scanRadius);
        List<Entity> entities = world.getOtherEntities(client.player, box, RuinScannerGizmoRenderer::isContainerVehicleEntity);

        for (Entity entity : entities) {
            current.add(entity.getUuid());
            entity.setGlowing(true);

            // Glowingが環境によって見えない場合の保険として、エンティティ位置にも水色アウトラインを出す。
            BlockPos pos = entity.getBlockPos();
            found.add(new Target(pos.toImmutable(), 0xFF00C8FF, 0.160F, 0.080F));
        }

        for (UUID uuid : new HashSet<>(glowingEntities.keySet())) {
            if (!current.contains(uuid)) {
                Entity entity = glowingEntities.remove(uuid);
                if (entity != null) {
                    entity.setGlowing(false);
                }
            }
        }
        for (Entity entity : entities) {
            glowingEntities.put(entity.getUuid(), entity);
        }
    }

    private static boolean isContainerVehicleEntity(Entity entity) {
        Identifier id = Registries.ENTITY_TYPE.getId(entity.getType());
        String path = id.getPath();

        // 普通のボート/トロッコは対象外。
        // バニラ名だけでなく、MOD追加の「inventory付き乗り物」もなるべく拾う。
        boolean namedContainerVehicle = path.equals("chest_minecart")
                || path.equals("hopper_minecart")
                || path.contains("chest_boat")
                || path.contains("chest_raft")
                || path.contains("container_boat")
                || path.contains("storage_boat");

        boolean inventoryVehicle = entity instanceof Inventory
                && (path.contains("minecart") || path.contains("boat") || path.contains("raft"));

        return namedContainerVehicle || inventoryVehicle;
    }

    private static void clearGlowing() {
        for (Entity entity : glowingEntities.values()) {
            if (entity != null) {
                entity.setGlowing(false);
            }
        }
        glowingEntities.clear();
    }

    private record Target(BlockPos pos, int outlineColor, float outerPadding, float innerPadding) {
    }
}
