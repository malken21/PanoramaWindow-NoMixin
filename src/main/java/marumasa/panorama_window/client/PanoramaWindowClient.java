package marumasa.panorama_window.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class PanoramaWindowClient implements ClientModInitializer {

    static final int camera_size = 1024;
    static final int width = 3840;
    static final int height = 2160;

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
            windows = new WindowManager(width, height, "Panorama", camera_size, client);
            panorama = new PanoramaManager(camera_size, client);
        });
    }
}
