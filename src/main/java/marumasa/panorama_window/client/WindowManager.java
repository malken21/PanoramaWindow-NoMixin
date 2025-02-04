package marumasa.panorama_window.client;

import net.minecraft.client.MinecraftClient;

import java.nio.ByteBuffer;

import static marumasa.panorama_window.PanoramaWindow.CONFIG;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWErrorCallback.createPrint;
import static org.lwjgl.opengl.GL31.*;

public class WindowManager {

    public final long window;

    private final int window_width;
    private final int window_height;
    private final int camera_size;

    private WindowPos windowPos;
    private CursorPos cursorPos;
    private boolean isDragging = false;

    private static final class WindowPos {
        public final int x;
        public final int y;

        public WindowPos(long window) {
            int[] x = new int[1];
            int[] y = new int[1];
            glfwGetWindowPos(window, x, y);
            this.x = x[0];
            this.y = y[0];
        }
    }

    private static final class CursorPos {
        public final double x;
        public final double y;

        public CursorPos(long window) {
            double[] x = new double[1];
            double[] y = new double[1];
            glfwGetCursorPos(window, x, y);
            this.x = x[0];
            this.y = y[0];

        }
    }

    public WindowManager(int width, int height, String title, int camera_size, MinecraftClient client) {
        window_width = width;
        window_height = height;
        this.camera_size = camera_size;
        // GLFWのエラコールバックを設定
        createPrint(System.err).set();
        // GLFWを初期化
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        // ウィンドウを作成時にウィンドウを表示する
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        // ウィンドウのサイズを変更不可にする
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        // ボーダーレスにするかどうかを設定する
        glfwWindowHint(GLFW_DECORATED, CONFIG.isBorderless() ? GLFW_FALSE : GLFW_TRUE);
        // 垂直同期をオフにする
        glfwSwapInterval(0);
        // ウィンドウを作成
        window = glfwCreateWindow(window_width, window_height, title, 0, 0);
        // OpenGLコンテキストの初期化
        glfwMakeContextCurrent(window);
        // ウィンドウ全体を黒色を描画する
        runBlack();

        // マウスボタンコールバックの設定
        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                if (action == GLFW_PRESS) {
                    isDragging = true;
                    cursorPos = new CursorPos(window);
                } else if (action == GLFW_RELEASE) {
                    isDragging = false;
                }
            }
        });

        // マウスカーソル位置コールバックの設定
        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            if (isDragging) {
                windowPos = new WindowPos(window);
                CursorPos currentPos = new CursorPos(window);
                double dx = currentPos.x - cursorPos.x;
                double dy = currentPos.y - cursorPos.y;
                glfwSetWindowPos(window, windowPos.x + (int) dx, windowPos.y + (int) dy);
            }
        });


        shaderManager = new ShaderManager();

        // VAOとVBOの作成
        int vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        float[] vertices = {
                -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, // 左下
                1.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f, // 右下
                1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, // 右上
                -1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f  // 左上
        };

        VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        // 頂点属性の指定
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 24, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 24, 12);
        glEnableVertexAttribArray(1);


        for (int i = 0; i < textures.length; i++) {
            textures[i] = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textures[i]);
            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA8,
                    this.camera_size,
                    this.camera_size,
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    (ByteBuffer) null
            );
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
        }
        // OpenGLコンテキストの初期化
        glfwMakeContextCurrent(client.getWindow().getHandle());
    }

    int[] textures = new int[6];

    private final int VBO;
    private final ShaderManager shaderManager;

    public void renderShader(ByteBuffer[] buffers) {
        // OpenGLコンテキストの初期化
        glfwMakeContextCurrent(window);

        glViewport(0, 0, window_width, window_height);
        loadUniforms(buffers, camera_size, camera_size);
        runShader();

        // OpenGLコンテキストの初期化 ()
        glfwMakeContextCurrent(MinecraftClient.getInstance().getWindow().getHandle());
    }

    public void renderBlack() {
        // OpenGLコンテキストの初期化
        glfwMakeContextCurrent(window);

        glViewport(0, 0, window_width, window_height);
        runBlack();

        // OpenGLコンテキストの初期化 ()
        glfwMakeContextCurrent(MinecraftClient.getInstance().getWindow().getHandle());
    }

    private void runBlack() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // 画面をクリア
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // 画面をクリアする色
        glfwSwapBuffers(window);
    }

    // テクスチャ読み込み
    private int loadTexture(ByteBuffer byteBuffer, int width, int height) {
        // テクスチャの作成
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        // テクスチャパラメータの設定
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        // テクスチャデータの転送
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, byteBuffer);
        return textureID;
    }


    private void loadUniforms(ByteBuffer[] buffers, int width, int height) {

        // texFront
        glUniform1i(2, 0);

        // texRight
        glUniform1i(4, 1);

        // texBack
        glUniform1i(0, 2);

        // texLeft
        glUniform1i(3, 3);

        // texTop
        glUniform1i(5, 4);

        // texBottom
        glUniform1i(1, 5);

        for (int i = 0; i < 6; i++) {
            switch (i) {
                case 0 -> glActiveTexture(GL_TEXTURE0);
                case 1 -> glActiveTexture(GL_TEXTURE1);
                case 2 -> glActiveTexture(GL_TEXTURE2);
                case 3 -> glActiveTexture(GL_TEXTURE3);
                case 4 -> glActiveTexture(GL_TEXTURE4);
                default -> glActiveTexture(GL_TEXTURE5);
            }
            glBindTexture(GL_TEXTURE_2D, textures[i]);
            // テクスチャデータの転送
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffers[i]);
        }
    }

    private void removeUniforms(int[] textures) {
        for (int texture : textures) {
            glDeleteTextures(texture);
        }
    }

    private void runShader() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // 画面をクリア
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // 画面をクリアする色

        glUseProgram(shaderManager.getShaderProgram());
        glBindVertexArray(VBO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

        glfwSwapBuffers(window);
    }
}
