package marumasa.panorama_window.util;

import marumasa.panorama_window.PanoramaWindow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static marumasa.panorama_window.PanoramaWindow.MOD_ID;

public class ModResource {
    public static String readString(String path) {
        try (InputStream is = readStream(path)) {
            if (is == null) throw new RuntimeException("Resource not found " + path);
            return StreamToString(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource", e);
        }
    }

    public static InputStream readStream(String path) {
        return PanoramaWindow.class.getResourceAsStream(String.format("/assets/%s/%s", MOD_ID, path));
    }

    private static String StreamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator()); // 改行コードを追加
            }
        }
        return sb.toString();
    }
}
