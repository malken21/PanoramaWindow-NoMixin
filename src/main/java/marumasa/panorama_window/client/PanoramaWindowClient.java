package marumasa.panorama_window.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import static marumasa.panorama_window.PanoramaWindow.CONFIG;

public class PanoramaWindowClient implements ClientModInitializer {

    static WindowManager windows;
    static PanoramaManager panorama;

    @Override
    public void onInitializeClient() {

        WorldRenderEvents.END.register(context -> {
            if (windows == null || panorama == null || PanoramaManager.isTaking()) return;
            panorama.take();
            windows.render(panorama.getBuffers());
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            windows = new WindowManager(CONFIG.getWidth(), CONFIG.getHeight(), "Panorama", CONFIG.getCameraSize(), client);
            panorama = new PanoramaManager( CONFIG.getCameraSize(), client);
        });
    }
}
