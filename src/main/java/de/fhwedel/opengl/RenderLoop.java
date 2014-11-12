package de.fhwedel.opengl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.PMVMatrix;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;


public class RenderLoop implements GLEventListener {

    private long lastTime = System.currentTimeMillis();

    // An array of 3 vectors which represents 3 vertices
    float vertexBufferData[] = {
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
    };
    private int vertexBufferId;
    private int programId;


    @Override
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        System.out.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
        System.out.println("INIT GL IS: " + gl.getClass().getName());
        System.out.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
        System.out.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
        System.out.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));

        gl.setSwapInterval(1); // V-SYNC
        gl.glEnable(GL.GL_CULL_FACE);

        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);


        GL3 gl3 = gl.getGL3();

        IntBuffer vertexArrayId = IntBuffer.allocate(1);
        gl3.glGenVertexArrays(1, vertexArrayId);
        gl3.glBindVertexArray(vertexArrayId.get(0));

        IntBuffer vertexBuffer = IntBuffer.allocate(1);
        gl3.glGenBuffers(1, vertexBuffer);
        vertexBufferId = vertexBuffer.get(0);
        gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, vertexBufferId);
        gl3.glBufferData(GL3.GL_ARRAY_BUFFER, vertexBufferData.length * Buffers.SIZEOF_FLOAT, FloatBuffer.wrap(vertexBufferData), GL3.GL_STATIC_DRAW);

        programId = loadShaders(gl3);
    }

    private int loadShaders(GL3 gl3) {
        int vertexShaderId = gl3.glCreateShader(GL3.GL_VERTEX_SHADER);
        int fragmentShaderId = gl3.glCreateShader(GL3.GL_FRAGMENT_SHADER);

        System.out.println("Compiling vertex shader");
        String vertexShaderSource[] = {"#version 330 core\n" +
                "in vec3 vertexPosition_modelspace;\n" +
                "uniform mat4 MVP;\n" +
                "void main(){\n" +
                "   vec4 v = vec4(vertexPosition_modelspace,1);\n" +
                "   gl_Position = MVP * v;\n" +
                "}"};

        gl3.glShaderSource(vertexShaderId, 1, vertexShaderSource, null);
        gl3.glCompileShader(vertexShaderId);

        // Check Vertex Shader
        IntBuffer vertexResult = IntBuffer.allocate(1);
        IntBuffer vertexInfoLogLength = IntBuffer.allocate(1);

        gl3.glGetShaderiv(vertexShaderId, GL3.GL_COMPILE_STATUS, vertexResult);
        gl3.glGetShaderiv(vertexShaderId, GL3.GL_INFO_LOG_LENGTH, vertexInfoLogLength);

        ByteBuffer vertexErrorMessage = ByteBuffer.allocate(vertexInfoLogLength.get(0));
        gl3.glGetShaderInfoLog(vertexShaderId, vertexInfoLogLength.get(0), null, vertexErrorMessage);
        System.out.println(new String(vertexErrorMessage.array()));

        // Compile Fragment Shader
        System.out.println("Compiling fragment shader");
        String fragmentShaderSource[] = {"#version 330 core\n" +
                "out vec3 color;\n" +
                "void main(){\n" +
                "    color = vec3(1,0,0);\n" +
                "}"};
        gl3.glShaderSource(fragmentShaderId, 1, fragmentShaderSource, null);
        gl3.glCompileShader(fragmentShaderId);

        // Check Fragment Shader
        IntBuffer fragmentResult = IntBuffer.allocate(1);
        IntBuffer fragmentInfoLogLength = IntBuffer.allocate(1);

        gl3.glGetShaderiv(fragmentShaderId, GL3.GL_COMPILE_STATUS, fragmentResult);
        gl3.glGetShaderiv(fragmentShaderId, GL3.GL_INFO_LOG_LENGTH, fragmentInfoLogLength);
        ByteBuffer fragmentErrorMessage = ByteBuffer.allocate(fragmentInfoLogLength.get(0));
        gl3.glGetShaderInfoLog(fragmentShaderId, fragmentInfoLogLength.get(0), null, fragmentErrorMessage);
        System.out.println(new String(fragmentErrorMessage.array()));

        // Link the program
        System.out.println("Linking program");
        int programId = gl3.glCreateProgram();
        gl3.glAttachShader(programId, vertexShaderId);
        gl3.glAttachShader(programId, fragmentShaderId);
        gl3.glLinkProgram(programId);

        // Check the program
        IntBuffer programResult = IntBuffer.allocate(1);
        IntBuffer programInfoLogLength = IntBuffer.allocate(1);

        gl3.glGetProgramiv(programId, GL3.GL_LINK_STATUS, programResult);
        gl3.glGetProgramiv(programId, GL3.GL_INFO_LOG_LENGTH, programInfoLogLength);
        ByteBuffer programErrorMessage = ByteBuffer.allocate(programInfoLogLength.get(0));
        gl3.glGetProgramInfoLog(programId, programInfoLogLength.get(0), null, programErrorMessage);
        System.out.println(new String(programErrorMessage.array()));

        gl3.glDeleteShader(vertexShaderId);
        gl3.glDeleteShader(fragmentShaderId);

        return programId;
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {
        long thisTime = System.currentTimeMillis();
        long deltaTime = thisTime - lastTime;
        this.lastTime = thisTime;

        update(drawable, deltaTime / 1000.0d);
        render(drawable);
    }

    private void render(GLAutoDrawable drawable) {
        GL3 gl3 = drawable.getGL().getGL3();

        gl3.glClearColor(0, 0, 0.4f, 0);
        gl3.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
        gl3.glUseProgram(programId);

        //render objects here.
        gl3.glEnableVertexAttribArray(0);
        gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, vertexBufferId);
        gl3.glVertexAttribPointer(0,    // no reason
                3,    // number of vertices
                GL3.GL_FLOAT,  // type
                false, // normalized?
                0,    // stride (Schrittweite)
                0);

        gl3.glDrawArrays(GL3.GL_TRIANGLES, 0, 3); // starting from 0, 3 vertices total
        gl3.glDisableVertexAttribArray(0);
    }

    private void update(GLAutoDrawable drawable, double deltaT) {
        PMVMatrix pmvMatrix = new PMVMatrix();

        // init.
        pmvMatrix.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        pmvMatrix.glLoadIdentity();
        pmvMatrix.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        pmvMatrix.glLoadIdentity();

        pmvMatrix.glMatrixMode(GLMatrixFunc.GL_MODELVIEW); //set to mv-matrix.
        pmvMatrix.glLoadIdentity();
        pmvMatrix.gluLookAt(4,3,3, // eye
                            0,0,0, // look-at
                            0,1,0); // up.

        pmvMatrix.gluPerspective(45,                                            //fov-y (angle)
                                (float)Window.WIDTH / (float)Window.HEIGHT,     // aspect-ratio
                                0.1f,                                           // z-near
                                100f);                                          // z-far

        pmvMatrix.update();

        GL3 gl3 = drawable.getGL().getGL3();

        int matrixId = gl3.glGetUniformLocation(programId, "MVP");

        System.out.println(pmvMatrix.matrixToString(new StringBuilder(), "%10.5f", pmvMatrix.glGetMatrixf()).toString());

        gl3.glUniformMatrix4fv(matrixId, 1, false, pmvMatrix.glGetMatrixf());
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
//        // Get the OpenGL graphics context
//        GL2 gl = drawable.getGL().getGL2();
//
//        height = (height == 0) ? 1 : height;  // Prevent divide by zero
//        float aspect = (float) width / height; // Compute aspect ratio
//
//        // Set view port to cover full screen
//        gl.glViewport(0, 0, width, height);
//
//        // Set up the projection matrix - choose perspective view
//        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
//        gl.glLoadIdentity();    // reset
//        // Angle of view (fovy) is 45 degrees (in the up y-direction). Based on this
//        // canvas's aspect ratio. Clipping z-near is 0.1f and z-near is 100.0f.
//
//        GLU glu = new GLU();
//        glu.gluPerspective(75f, aspect, 0.1f, 100.0f); // fovy, aspect, zNear, zFar
//        glu.gluLookAt(0, 0, 100, // eye
//                0, 0, 0, // look-at
//                0, 1, 0); // up
//
//        // Switch to the model-view transform
//        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
//        gl.glLoadIdentity();    // reset
    }
}
