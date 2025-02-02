package marumasa.panorama_window.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.stream.IntStream;

import static marumasa.panorama_window.PanoramaWindow.LOGGER;
import static net.minecraft.client.MinecraftClient.IS_SYSTEM_MAC;
import static org.lwjgl.opengl.GL31.*;

public class PanoramaManager {
    private static boolean taking = false;

    public static boolean isTaking() {
        return taking;
    }

    private static void take(ByteBuffer[] buffers, MinecraftClient client, int width, int height) {
        taking = true;

        Window window = client.getWindow();
        Entity camera_default = client.cameraEntity;
        if (client.player == null || camera_default == null || client.gameRenderer == null || client.worldRenderer == null || client.world == null)
            return;

        int i = window.getFramebufferWidth();
        int j = window.getFramebufferHeight();
        Framebuffer framebuffer = new SimpleFramebuffer(width, height, true, IS_SYSTEM_MAC);
        float yaw = client.player.getYaw();
        client.gameRenderer.setBlockOutlineEnabled(false);

        Entity camera = new AreaEffectCloudEntity(EntityType.AREA_EFFECT_CLOUD, client.world);
        camera.copyPositionAndRotation(client.player);
        client.setCameraEntity(camera);

        try {
            client.gameRenderer.setRenderingPanorama(true);
            client.worldRenderer.reloadTransparencyPostProcessor();
            window.setFramebufferWidth(width);
            window.setFramebufferHeight(height);

            for (int l = 0; l < 6; l++) {
                switch (l) {
                    case 0:
                        camera.setYaw(yaw);
                        camera.setPitch(0.0F);
                        break;
                    case 1:
                        camera.setYaw((yaw + 90.0F) % 360.0F);
                        camera.setPitch(0.0F);
                        break;
                    case 2:
                        camera.setYaw((yaw + 180.0F) % 360.0F);
                        camera.setPitch(0.0F);
                        break;
                    case 3:
                        camera.setYaw((yaw - 90.0F) % 360.0F);
                        camera.setPitch(0.0F);
                        break;
                    case 4:
                        camera.setYaw(yaw);
                        camera.setPitch(-90.0F);
                        break;
                    case 5:
                    default:
                        camera.setYaw(yaw);
                        camera.setPitch(90.0F);
                }

                camera.prevYaw = camera.getYaw();
                camera.prevPitch = camera.getPitch();
                framebuffer.beginWrite(true);
                client.gameRenderer.renderWorld(RenderTickCounter.ZERO);

                // テクスチャのバインド
                glBindTexture(GL_TEXTURE_2D, framebuffer.getColorAttachment());
                glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffers[l]);
            }
        } catch (Exception exception) {
            LOGGER.error("Couldn't take images", exception);
        } finally {
            client.setCameraEntity(camera_default);
            camera.remove(Entity.RemovalReason.KILLED);

            client.gameRenderer.setBlockOutlineEnabled(true);
            window.setFramebufferWidth(i);
            window.setFramebufferHeight(j);
            framebuffer.delete();
            client.gameRenderer.setRenderingPanorama(false);
            client.worldRenderer.reloadTransparencyPostProcessor();
            client.getFramebuffer().beginWrite(true);
            client.gameRenderer.renderWorld(RenderTickCounter.ZERO);
        }
        taking = false;
    }

    private final int camera_size;
    private final MinecraftClient client;
    private final ByteBuffer[] buffers;

    public PanoramaManager(int camera_size, MinecraftClient client) {
        this.client = client;
        this.camera_size = camera_size;
        // 6個の ByteBuffer 型の配列を生成する
        this.buffers = IntStream.range(0, 6).mapToObj(i ->
                // 配列の内容
                BufferUtils.createByteBuffer(this.camera_size * this.camera_size * 4)
        ).toArray(ByteBuffer[]::new);
    }

    public void take() {
        take(buffers, client, this.camera_size, this.camera_size);
    }

    public ByteBuffer[] getBuffers() {
        return buffers;
    }
}
