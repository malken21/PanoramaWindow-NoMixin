package marumasa.panorama_window;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

public class PanoramaWindow implements ModInitializer {
    // Logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "panorama_window";
    public static final Config CONFIG = new Config();

    @Override
    public void onInitialize() {
        LOGGER.info("Start: " + MOD_ID);
    }
}
