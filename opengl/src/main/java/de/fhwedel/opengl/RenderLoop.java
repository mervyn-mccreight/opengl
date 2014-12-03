package de.fhwedel.opengl;

import com.hackoeur.jglm.Mat4;
import com.hackoeur.jglm.Matrices;
import com.hackoeur.jglm.Vec3;
import com.hackoeur.jglm.Vec4;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import javax.media.opengl.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;


public class RenderLoop implements GLEventListener {

    private final HeightField heightField;
    // An array of 3 vectors which represents 3 vertices
    private float vertexBufferData[] = {
            -1.0f,-1.0f,-1.0f, // triangle 1 : begin
            -1.0f,-1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f, // triangle 1 : end
            1.0f, 1.0f,-1.0f, // triangle 2 : begin
            -1.0f,-1.0f,-1.0f,
            -1.0f, 1.0f,-1.0f, // triangle 2 : end
            1.0f,-1.0f, 1.0f,
            -1.0f,-1.0f,-1.0f,
            1.0f,-1.0f,-1.0f,
            1.0f, 1.0f,-1.0f,
            1.0f,-1.0f,-1.0f,
            -1.0f,-1.0f,-1.0f,
            -1.0f,-1.0f,-1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f,-1.0f,
            1.0f,-1.0f, 1.0f,
            -1.0f,-1.0f, 1.0f,
            -1.0f,-1.0f,-1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f,-1.0f, 1.0f,
            1.0f,-1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f,-1.0f,-1.0f,
            1.0f, 1.0f,-1.0f,
            1.0f,-1.0f,-1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f,-1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f,-1.0f,
            -1.0f, 1.0f,-1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f,-1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f,-1.0f, 1.0f
    };

    private float textureUVData[] = {
            0.000059f, 0.000004f,
            0.000103f, 0.336048f,
            0.335973f, 0.335903f,
            1.000000f, 0.000013f,
            0.667979f, 0.335851f,
            0.999958f, 0.336064f,
            0.667979f, 0.335851f,
            0.336024f, 0.671877f,
            0.667969f, 0.671889f,
            1.000000f, 0.000013f,
            0.668104f, 0.000013f,
            0.667979f, 0.335851f,
            0.000059f, 0.000004f,
            0.335973f, 0.335903f,
            0.336098f, 0.000071f,
            0.667979f, 0.335851f,
            0.335973f, 0.335903f,
            0.336024f, 0.671877f,
            1.000000f, 0.671847f,
            0.999958f, 0.336064f,
            0.667979f, 0.335851f,
            0.668104f, 0.000013f,
            0.335973f, 0.335903f,
            0.667979f, 0.335851f,
            0.335973f, 0.335903f,
            0.668104f, 0.000013f,
            0.336098f, 0.000071f,
            0.000103f, 0.336048f,
            0.000004f, 0.671870f,
            0.336024f, 0.671877f,
            0.000103f, 0.336048f,
            0.336024f, 0.671877f,
            0.335973f, 0.335903f,
            0.667969f, 0.671889f,
            1.000000f, 0.671847f,
            0.667979f, 0.335851f
    };

    private long lastTime = System.currentTimeMillis();
    private int vertexBufferId;
    private int programId;
    private int textureUVBufferId;
    private int normalBufferId;

    public RenderLoop() {
        heightField = new HeightField();
    }

    public static Texture loadTexture(String file) throws GLException, IOException {
        TextureData textureData = TextureIO.newTextureData(GLProfile.getDefault(), new File(file), true, TextureIO.DDS);
        return TextureIO.newTexture(textureData);
    }

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
        gl.glDepthFunc(GL.GL_LESS);


        GL2 gl2 = gl.getGL2();
        //gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
        gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);


        IntBuffer textureUVBuffer = IntBuffer.allocate(1);
        gl2.glGenBuffers(1, textureUVBuffer);
        textureUVBufferId = textureUVBuffer.get(0);
        gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, textureUVBufferId);
        gl2.glBufferData(GL2.GL_ARRAY_BUFFER, textureUVData.length * Buffers.SIZEOF_FLOAT, FloatBuffer.wrap(textureUVData), GL2.GL_STATIC_DRAW);




        programId = loadShaders(gl2);

        Texture texture = null;
        try {
            texture = loadTexture("opengl/textures/cube.dds");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        texture.bind(gl2);
    }

    private int loadShaders(GL2 gl2) {
        int vertexShaderId = gl2.glCreateShader(GL2.GL_VERTEX_SHADER);
        int fragmentShaderId = gl2.glCreateShader(GL2.GL_FRAGMENT_SHADER);

        System.out.println("Compiling vertex shader");

        gl2.glShaderSource(vertexShaderId, 1, ShaderCodeLoader.readSourceFile("opengl/shader/vertex"), null);
        gl2.glCompileShader(vertexShaderId);

        // Check Vertex Shader
        IntBuffer vertexResult = IntBuffer.allocate(1);
        IntBuffer vertexInfoLogLength = IntBuffer.allocate(1);

        gl2.glGetShaderiv(vertexShaderId, GL2.GL_COMPILE_STATUS, vertexResult);
        gl2.glGetShaderiv(vertexShaderId, GL2.GL_INFO_LOG_LENGTH, vertexInfoLogLength);

        ByteBuffer vertexErrorMessage = ByteBuffer.allocate(vertexInfoLogLength.get(0));
        gl2.glGetShaderInfoLog(vertexShaderId, vertexInfoLogLength.get(0), null, vertexErrorMessage);
        System.out.println(new String(vertexErrorMessage.array()));

        // Compile Fragment Shader
        System.out.println("Compiling fragment shader");
        gl2.glShaderSource(fragmentShaderId, 1, ShaderCodeLoader.readSourceFile("opengl/shader/fragment"), null);
        gl2.glCompileShader(fragmentShaderId);

        // Check Fragment Shader
        IntBuffer fragmentResult = IntBuffer.allocate(1);
        IntBuffer fragmentInfoLogLength = IntBuffer.allocate(1);

        gl2.glGetShaderiv(fragmentShaderId, GL2.GL_COMPILE_STATUS, fragmentResult);
        gl2.glGetShaderiv(fragmentShaderId, GL2.GL_INFO_LOG_LENGTH, fragmentInfoLogLength);
        ByteBuffer fragmentErrorMessage = ByteBuffer.allocate(fragmentInfoLogLength.get(0));
        gl2.glGetShaderInfoLog(fragmentShaderId, fragmentInfoLogLength.get(0), null, fragmentErrorMessage);
        System.out.println(new String(fragmentErrorMessage.array()));

        // Link the program
        System.out.println("Linking program");
        int programId = gl2.glCreateProgram();
        gl2.glAttachShader(programId, vertexShaderId);
        gl2.glAttachShader(programId, fragmentShaderId);
        gl2.glLinkProgram(programId);

        // Check the program
        IntBuffer programResult = IntBuffer.allocate(1);
        IntBuffer programInfoLogLength = IntBuffer.allocate(1);

        gl2.glGetProgramiv(programId, GL2.GL_LINK_STATUS, programResult);
        gl2.glGetProgramiv(programId, GL2.GL_INFO_LOG_LENGTH, programInfoLogLength);
        ByteBuffer programErrorMessage = ByteBuffer.allocate(programInfoLogLength.get(0));
        gl2.glGetProgramInfoLog(programId, programInfoLogLength.get(0), null, programErrorMessage);
        System.out.println(new String(programErrorMessage.array()));

        gl2.glDeleteShader(vertexShaderId);
        gl2.glDeleteShader(fragmentShaderId);

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
        GL2 gl2 = drawable.getGL().getGL2();

        float[] vertexArray = heightField.getVertexArray();

        IntBuffer vertexBuffer = IntBuffer.allocate(1);
        gl2.glGenBuffers(1, vertexBuffer);
        vertexBufferId = vertexBuffer.get(0);
        gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexBufferId);
        gl2.glBufferData(GL2.GL_ARRAY_BUFFER, vertexArray.length * Buffers.SIZEOF_FLOAT, FloatBuffer.wrap(vertexArray), GL2.GL_DYNAMIC_DRAW);

        float[] normalArray = heightField.getNormals();
//        float[] normalArray = new float[vertexArray.length];

        IntBuffer normalBuffer = IntBuffer.allocate(1);
        gl2.glGenBuffers(1, normalBuffer);
        normalBufferId = normalBuffer.get();
        gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, normalBufferId);
        gl2.glBufferData(GL2.GL_ARRAY_BUFFER, normalArray.length * Buffers.SIZEOF_FLOAT, FloatBuffer.wrap(normalArray), GL2.GL_DYNAMIC_DRAW);

        gl2.glClearColor(0.5f, 0.5f, 0.5f, 0);
        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl2.glUseProgram(programId);

        //render objects here.
        gl2.glEnableVertexAttribArray(0);
        gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexBufferId);
        gl2.glVertexAttribPointer(0,    // index of attribute (vertex, color...)
                3,    // number of vertices
                GL2.GL_FLOAT,  // type
                false, // normalized?
                0,    // stride (Schrittweite)
                0);   // offset

        gl2.glEnableVertexAttribArray(1);
        gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, textureUVBufferId);
        gl2.glVertexAttribPointer(1,
                2,
                GL2.GL_FLOAT,
                false,
                0,
                0);

        gl2.glEnableVertexAttribArray(2);
        gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, normalBufferId);
        gl2.glVertexAttribPointer(2,
                3,
                GL2.GL_FLOAT,
                false,
                0,
                0);

        gl2.glDrawArrays(GL2.GL_TRIANGLES, 0, vertexArray.length / 3); // starting from 0, 12*3 vertices total
        gl2.glDisableVertexAttribArray(0);
        gl2.glDisableVertexAttribArray(1);
        gl2.glDisableVertexAttribArray(2);
    }

    private void update(GLAutoDrawable drawable, double deltaT) {

        heightField.update(deltaT);

        Mat4 model = Mat4.MAT4_IDENTITY;
        model = model.translate(heightField.getPosition());

        Mat4 view = Matrices.lookAt(new Vec3(0, 30, 30), // eye
                new Vec3(0, 0, 0), // lookat
                new Vec3(0, 1, 0) // up.
        );

        Mat4 projection = Matrices.perspective(45,
                (float) Window.WIDTH / (float) Window.HEIGHT,
                0.1f,
                100f
        );

        GL2 gl2 = drawable.getGL().getGL2();

        int modelId = gl2.glGetUniformLocation(programId, "M");
        int viewId = gl2.glGetUniformLocation(programId, "V");
        int projectionId = gl2.glGetUniformLocation(programId, "P");

        gl2.glUniformMatrix4fv(modelId, 1, false, model.getBuffer());
        gl2.glUniformMatrix4fv(viewId, 1, false, view.getBuffer());
        gl2.glUniformMatrix4fv(projectionId, 1, false, projection.getBuffer());

        int lightPositionId = gl2.glGetUniformLocation(programId, "LightPosition_worldspace");
        gl2.glUniform3fv(lightPositionId, 1, new Vec3(0, 30, 0).getBuffer());

        int lightColorId = gl2.glGetUniformLocation(programId, "LightColor");
        gl2.glUniform3fv(lightColorId, 1, new Vec3(1, 1, 1f).getBuffer());

        int lightPowerId = gl2.glGetUniformLocation(programId, "LightPower");
        gl2.glUniform1f(lightPowerId, 300f);

        int materialAmbientColorId = gl2.glGetUniformLocation(programId, "MaterialAmbientComponent");
        gl2.glUniform3fv(materialAmbientColorId, 1, new Vec3(0.1f, 0.1f, 0.1f).getBuffer());

        Vec4 specularComponent = new Vec4(0.3f, 0.3f, 0.3f, 5f);
        int specularId = gl2.glGetUniformLocation(programId, "MaterialSpecularComponent");
        gl2.glUniform4f(specularId, specularComponent.getX(), specularComponent.getY(), specularComponent.getZ(), specularComponent.getW());
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
