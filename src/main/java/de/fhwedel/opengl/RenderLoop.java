package de.fhwedel.opengl;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
import java.nio.IntBuffer;


public class RenderLoop implements GLEventListener {

    private long lastTime = System.currentTimeMillis();

    private float vertices[] = {
            0.0f, 0.8f,
            -0.8f, -0.8f,
            0.8f, -0.8f
    };

    private int vertexShader;
    private int fragmentShader;
    private int program;

    @Override
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        gl.setSwapInterval(1);
        gl.glEnable(GL.GL_CULL_FACE);

        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);

        GL2 gl2 = gl.getGL2();

        vertexShader = gl2.glCreateShader(GL2.GL_VERTEX_SHADER);

        gl2.glShaderSource(vertexShader, 1, new String[]{"#version 120\n",
                        "attribute vec2 coord2d;                  ",
                        "void main(void) {                        ",
                        "  gl_Position = vec4(coord2d, 0.0, 1.0); ",
                        "}"},
                null);
        gl2.glCompileShader(vertexShader);

        fragmentShader = gl2.glCreateShader(GL2.GL_FRAGMENT_SHADER);

        gl2.glShaderSource(fragmentShader, 1,
                new String[]{"#version 120\n",
                        "void main(void) {",
                        "gl_FragColor[0] = 0.0;",
                        "gl_FragColor[1] = 0.0;",
                        "gl_FragColor[2] = 1.0;",
                        "}"},
                null);

        gl2.glCompileShader(fragmentShader);

        IntBuffer intBuffer = IntBuffer.allocate(1);

        // BRB
        gl2.glGetShaderiv(vertexShader, GL2.GL_COMPILE_STATUS, intBuffer);
        if (intBuffer.get(0) == GL2.GL_FALSE) {
            System.out.println("Error in vertex shader!");
            System.exit(1);
        }

        intBuffer.rewind();

        gl2.glGetShaderiv(fragmentShader, GL2.GL_COMPILE_STATUS, intBuffer);
        if (intBuffer.get(0) == GL2.GL_FALSE) {
            System.out.println("Error in fragment shader!");
            System.exit(1);
        }

        program = gl2.glCreateProgram();

        gl2.glAttachShader(program, vertexShader);
        gl2.glAttachShader(program, fragmentShader);

        gl2.glLinkProgram(program);

        intBuffer.rewind();
        gl2.glGetProgramiv(program, GL2.GL_LINK_STATUS, intBuffer);

        if (intBuffer.get(0) == GL2.GL_FALSE) {
            System.out.println("Error in program linking!");
            System.exit(1);
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {
        long thisTime = System.currentTimeMillis();
        long deltaTime = thisTime - lastTime;
        this.lastTime = thisTime;

        update(deltaTime / 1000.0d);
        render(drawable);
    }

    private void render(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0, 0, 0, 0);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        //render objects here.


    }

    private void update(double deltaT) {

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // Get the OpenGL graphics context
        GL2 gl = drawable.getGL().getGL2();

        height = (height == 0) ? 1 : height;  // Prevent divide by zero
        float aspect = (float) width / height; // Compute aspect ratio

        // Set view port to cover full screen
        gl.glViewport(0, 0, width, height);

        // Set up the projection matrix - choose perspective view
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();    // reset
        // Angle of view (fovy) is 45 degrees (in the up y-direction). Based on this
        // canvas's aspect ratio. Clipping z-near is 0.1f and z-near is 100.0f.

        GLU glu = new GLU();
        glu.gluPerspective(75f, aspect, 0.1f, 100.0f); // fovy, aspect, zNear, zFar
        glu.gluLookAt(0, 0, 100, // eye
                0, 0, 0, // look-at
                0, 1, 0); // up

        // Switch to the model-view transform
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity();    // reset
    }
}
