package atom.grabvillager.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Paths;

public class GrabVillagerConfig {
    public static float throwMultiplier = 1.0f; // Max 3.0
    public static boolean allowTools = false;
    public static int barColorIndex = 0;
    public static float barSize = 1.0f; // Max 1.0
    public static int barOffsetX = 0;
    public static int barOffsetY = 0;
    public static boolean barVertical = false;

    private static volatile boolean hasServerSync = false;
    private static volatile float serverThrowMultiplier = 3.0f;
    private static volatile boolean serverAllowTools = false;

    private static final File FILE = Paths.get("config", "grabvillager.json").toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void applyServerSync(float maxThrowMultiplier, boolean allowToolsOnServer) {
        serverThrowMultiplier = Math.max(0.1f, Math.min(3.0f, maxThrowMultiplier));
        serverAllowTools = allowToolsOnServer;
        hasServerSync = true;
    }

    public static void clearServerSync() {
        hasServerSync = false;
    }

    public static float getEffectiveThrowMultiplier() {
        float local = Math.max(0.1f, Math.min(3.0f, throwMultiplier));
        return hasServerSync ? Math.min(local, serverThrowMultiplier) : local;
    }

    public static boolean getEffectiveAllowTools() {
        return hasServerSync ? serverAllowTools : allowTools;
    }

    public static void load() {
        try {
            if (FILE.exists()) {
                try (FileReader reader = new FileReader(FILE)) {
                    JsonObject json = GSON.fromJson(reader, JsonObject.class);
                    if (json != null) {
                        if (json.has("throwMultiplier")) throwMultiplier = json.get("throwMultiplier").getAsFloat();
                        if (json.has("allowTools")) allowTools = json.get("allowTools").getAsBoolean();
                        if (json.has("barColorIndex")) barColorIndex = json.get("barColorIndex").getAsInt();
                        if (json.has("barSize")) barSize = json.get("barSize").getAsFloat();
                        if (json.has("barOffsetX")) barOffsetX = json.get("barOffsetX").getAsInt();
                        if (json.has("barOffsetY")) barOffsetY = json.get("barOffsetY").getAsInt();
                        if (json.has("barVertical")) barVertical = json.get("barVertical").getAsBoolean();
                    }
                }

                // Math bounds & rounding
                throwMultiplier = Math.round(Math.max(0.1f, Math.min(3.0f, throwMultiplier)) * 10.0f) / 10.0f;
                barSize = Math.round(Math.max(0.1f, Math.min(1.0f, barSize)) * 10.0f) / 10.0f;
                barColorIndex = Math.floorMod(barColorIndex, 4);
            }
        } catch (Exception ignored) {}
    }

    public static void save() {
        try {
            // Math bounds & rounding before save
            throwMultiplier = Math.round(Math.max(0.1f, Math.min(3.0f, throwMultiplier)) * 10.0f) / 10.0f;
            barSize = Math.round(Math.max(0.1f, Math.min(1.0f, barSize)) * 10.0f) / 10.0f;
            barColorIndex = Math.floorMod(barColorIndex, 4);

            FILE.getParentFile().mkdirs();
            JsonObject json = new JsonObject();
            json.addProperty("throwMultiplier", throwMultiplier);
            json.addProperty("allowTools", allowTools);
            json.addProperty("barColorIndex", barColorIndex);
            json.addProperty("barSize", barSize);
            json.addProperty("barOffsetX", barOffsetX);
            json.addProperty("barOffsetY", barOffsetY);
            json.addProperty("barVertical", barVertical);
            try (FileWriter writer = new FileWriter(FILE)) {
                GSON.toJson(json, writer);
            }
        } catch (Exception ignored) {}
    }
}
