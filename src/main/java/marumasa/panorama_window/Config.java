package marumasa.panorama_window;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static marumasa.panorama_window.PanoramaWindow.MOD_ID;

public class Config {

    private static final Path path = FabricLoader.getInstance().getConfigDir().normalize().resolve(MOD_ID + ".json");

    private int CameraSize = 2048;
    private int Width = 3840;
    private int Height = 2160;
    private boolean Borderless = false;

    public int getCameraSize() {
        return CameraSize;
    }

    public void setCameraSize(int cameraSize) {
        CameraSize = cameraSize;
        serialize();
    }

    public int getWidth() {
        return Width;
    }

    public void setWidth(int width) {
        Width = width;
        serialize();
    }

    public int getHeight() {
        return Height;
    }

    public void setHeight(int height) {
        Height = height;
        serialize();
    }

    public boolean isBorderless() {
        return Borderless;
    }

    public void setBorderless(boolean borderless) {
        Borderless = borderless;
        serialize();
    }

    private record JsonModel(
            int CameraSize,
            int Width,
            int Height,
            boolean Borderless
    ) {
    }

    private static final Gson gson = new Gson();

    private static JsonModel loadJSON() {
        try (final BufferedReader reader = Files.newBufferedReader(Config.path)) {
            return gson.fromJson(reader, JsonModel.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveJSON(JsonModel model) {
        try (final BufferedWriter writer = Files.newBufferedWriter(Config.path)) {
            gson.toJson(model, model.getClass(), writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deserialize() {
        final JsonModel model = loadJSON();
        CameraSize = model.CameraSize();
        Width = model.Width();
        Height = model.Height();
        Borderless = model.Borderless();
    }

    private void serialize() {
        saveJSON(new JsonModel(
                getCameraSize(),
                getWidth(),
                getHeight(),
                isBorderless()
        ));
    }

    public Config() {
        final File configFile = path.toFile();
        if (!configFile.exists()) {
            serialize();
        } else {
            deserialize();
        }
    }
}