package marumasa.panorama_window.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.TitleScreen;

import static marumasa.panorama_window.PanoramaWindow.CONFIG;

public class PanoramaWindowClient implements ClientModInitializer {

    static WindowManager windows;
    static PanoramaManager panorama;

    @Override
    public void onInitializeClient() {

        WorldRenderEvents.END.register(context -> {
            if (windows == null || panorama == null || PanoramaManager.isTaking()) return;
            panorama.take();
            windows.renderShader(panorama.getBuffers());
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (windows == null || panorama == null) {
                windows = new WindowManager(CONFIG.getWidth(), CONFIG.getHeight(), "Panorama", CONFIG.getCameraSize(), client);
                panorama = new PanoramaManager(CONFIG.getCameraSize(), client);
            } else if (screen instanceof TitleScreen) {
                // タイトル画面が表示された場合
                // ウィンドウ全体を黒色を描画する
                windows.renderBlack();
            }
        });
    }
}
