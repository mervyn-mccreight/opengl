package de.fhwedel.opengl;

import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;

import javax.media.nativewindow.WindowClosingProtocol;
import javax.media.opengl.FPSCounter;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;

public class Window {

    public static final int WIDTH = 1024;
    public static final int HEIGHT = 768;
    private static final String TITLE = "Test Fenster";

    public static void main(String[] args) {
        GLProfile glp = GLProfile.getDefault();

        GLCapabilities caps = new GLCapabilities(glp);
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        GLWindow window = GLWindow.create(caps);

        final FPSAnimator animator = new FPSAnimator(window, 60);
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

        RenderLoop loop = new RenderLoop();
        window.addGLEventListener(loop);
        window.addKeyListener(loop);

        window.setSize(WIDTH, HEIGHT);
        window.setTitle(TITLE);
        window.setVisible(true);
        window.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);

        animator.start();
    }
}
