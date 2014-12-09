package de.fhwedel.opengl;

import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.Animator;

import javax.media.nativewindow.WindowClosingProtocol;
import javax.media.opengl.FPSCounter;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;

public class Window {

    public static final int WIDTH = 640;
    public static final int HEIGHT = 480;
    private static final String TITLE = "Test Fenster";

    public static void main(String[] args) {
        GLProfile glp = GLProfile.getDefault();

        GLCapabilities caps = new GLCapabilities(glp);
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        GLWindow window = GLWindow.create(caps);

        final Animator animator = new Animator(window);
        animator.setUpdateFPSFrames(FPSCounter.DEFAULT_FRAMES_PER_INTERVAL, System.out);

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDestroyNotify(WindowEvent event) {
                // Use a dedicate thread to run the stop() to ensure that the
                // animator stops before program exits.
                new Thread() {
                    @Override
                    public void run() {
                        while (animator.isStarted()) {
                            animator.stop();
                        }

                        System.exit(0);
                    }
                }.start();
            }
        });

        RenderLoop renderLoop = new RenderLoop();
        window.addMouseListener(renderLoop);
        window.addGLEventListener(renderLoop);

        window.setSize(WIDTH, HEIGHT);
        window.setTitle(TITLE);
        window.setVisible(true);
        window.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);

        animator.start();
    }
}
