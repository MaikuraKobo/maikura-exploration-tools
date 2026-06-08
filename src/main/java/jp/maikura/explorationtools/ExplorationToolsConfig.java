package jp.maikura.explorationtools;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ExplorationToolsConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("maikura_exploration_tools.json");

    public static int scanRadius = 32;
    public static boolean showContainers = true;
    public static boolean showSpawners = true;
    public static boolean showArchaeology = true;
    public static boolean showEntities = true;
    public static boolean showOthers = true;
    public static ScanMode scanMode = ScanMode.ALL;

    public enum ScanMode {
        ALL("全探査"),
        TREASURE("宝箱"),
        ARCHAEOLOGY("発掘"),
        RUINS("遺跡");

        private final String displayName;

        ScanMode(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }

        public ScanMode next() {
            ScanMode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }

        public static ScanMode fromName(String name) {
            for (ScanMode mode : values()) {
                if (mode.name().equalsIgnoreCase(name)) {
                    return mode;
                }
            }
            return ALL;
        }
    }

    private ExplorationToolsConfig() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try {
            String json = Files.readString(CONFIG_PATH, StandardCharsets.UTF_8);
            scanRadius = clampRadius(readInt(json, "scanRadius", scanRadius));
            showContainers = readBoolean(json, "showContainers", showContainers);
            showSpawners = readBoolean(json, "showSpawners", showSpawners);
            showArchaeology = readBoolean(json, "showArchaeology", showArchaeology);
            showEntities = readBoolean(json, "showEntities", showEntities);
            showOthers = readBoolean(json, "showOthers", showOthers);
            scanMode = ScanMode.fromName(readString(json, "scanMode", scanMode.name()));
        } catch (Exception ignored) {
            scanRadius = 32;
            showContainers = true;
            showSpawners = true;
            showArchaeology = true;
            showEntities = true;
            showOthers = true;
            scanMode = ScanMode.ALL;
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = "{\n"
                    + "  \"scanRadius\": " + clampRadius(scanRadius) + ",\n"
                    + "  \"showContainers\": " + showContainers + ",\n"
                    + "  \"showSpawners\": " + showSpawners + ",\n"
                    + "  \"showArchaeology\": " + showArchaeology + ",\n"
                    + "  \"showEntities\": " + showEntities + ",\n"
                    + "  \"showOthers\": " + showOthers + ",\n"
                    + "  \"scanMode\": \"" + scanMode.name() + "\"\n"
                    + "}\n";
            Files.writeString(CONFIG_PATH, json, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    public static int clampRadius(int value) {
        if (value <= 16) return 16;
        if (value <= 32) return 32;
        if (value <= 48) return 48;
        return 64;
    }

    public static void cycleRadius() {
        scanRadius = switch (clampRadius(scanRadius)) {
            case 16 -> 32;
            case 32 -> 48;
            case 48 -> 64;
            default -> 16;
        };
        save();
    }

    public static ScanMode cycleScanMode() {
        scanMode = scanMode.next();
        save();
        return scanMode;
    }

    public static boolean shouldShowContainers() {
        return showContainers && (scanMode == ScanMode.ALL || scanMode == ScanMode.TREASURE);
    }

    public static boolean shouldShowSpawners() {
        return showSpawners && (scanMode == ScanMode.ALL || scanMode == ScanMode.RUINS);
    }

    public static boolean shouldShowArchaeology() {
        return showArchaeology && (scanMode == ScanMode.ALL || scanMode == ScanMode.ARCHAEOLOGY);
    }

    public static boolean shouldShowEntities() {
        return showEntities && (scanMode == ScanMode.ALL || scanMode == ScanMode.TREASURE);
    }

    public static boolean shouldShowOthers() {
        return showOthers && (scanMode == ScanMode.ALL || scanMode == ScanMode.RUINS);
    }

    private static int readInt(String json, String key, int fallback) {
        String needle = "\"" + key + "\"";
        int i = json.indexOf(needle);
        if (i < 0) return fallback;
        int colon = json.indexOf(':', i + needle.length());
        if (colon < 0) return fallback;
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        if (end <= start) return fallback;
        return Integer.parseInt(json.substring(start, end));
    }

    private static String readString(String json, String key, String fallback) {
        String needle = "\"" + key + "\"";
        int i = json.indexOf(needle);
        if (i < 0) return fallback;
        int colon = json.indexOf(':', i + needle.length());
        if (colon < 0) return fallback;
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        if (start >= json.length() || json.charAt(start) != '\"') return fallback;
        start++;
        int end = json.indexOf('\"', start);
        if (end < 0) return fallback;
        return json.substring(start, end);
    }

    private static boolean readBoolean(String json, String key, boolean fallback) {
        String needle = "\"" + key + "\"";
        int i = json.indexOf(needle);
        if (i < 0) return fallback;
        int colon = json.indexOf(':', i + needle.length());
        if (colon < 0) return fallback;
        String tail = json.substring(colon + 1).trim();
        if (tail.startsWith("true")) return true;
        if (tail.startsWith("false")) return false;
        return fallback;
    }
}
