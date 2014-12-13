package de.fhwedel.opengl;

import com.hackoeur.jglm.Mat4;
import com.hackoeur.jglm.Vec3;
import com.hackoeur.jglm.Vec4;

public class Sphere {
    private static final int RINGS = 100;
    private static final float R = 1f/(RINGS-1);
    private static final int SECTORS = 100;
    private static final float S = 1f/(SECTORS-1);
    private static final float INITIAL_SCALE = 0.3f;
    private final float[] vertexArray;
    private final float[] normalArray;

    private float radius;
    private Vec3 position;

    public Sphere(Vec3 position, float radius) {
        this.radius = radius;
        this.position = position;

        vertexArray = new float[RINGS * SECTORS * 3];
        normalArray = new float[RINGS * SECTORS * 3];
    }

    public boolean contains(float x, float y, float z) {
        return (position.getX() + radius > x || position.getX() - radius < x)
                && (position.getY() + radius > y || position.getY() - radius < y)
                && (position.getZ() + radius > z || position.getZ() - radius < z);
    }

    public float getY(float x, float z) {
        return (float) Math.sqrt(Math.pow(x-position.getX(), 2) + Math.pow(z-position.getZ(), 2) - Math.pow(radius, 2)) - position.getY();
    }

    public float getRadius() {
        return radius;
    }

    public Vec3 getPosition() {
        return position;
    }

    public float[] getVertexArray() {
        int k = 0;

        for (int r = 0; r < RINGS; r++) {
            for (int s = 0; s < SECTORS; s++) {
                float y = (float) Math.sin(-Math.PI/2 + Math.PI * r * R);
                float x = (float) (Math.cos(2*Math.PI * s * S) * Math.sin(Math.PI * r * R));
                float z = (float) (Math.sin(2 * Math.PI * s * S) * Math.sin(Math.PI * r * R));

                vertexArray[k++] = x * radius;
                vertexArray[k++] = y * radius;
                vertexArray[k++] = z * radius;
            }
        }

        return vertexArray;
    }

    public float[] getNormals() {
        int k = 0;

        for (int r = 0; r < RINGS; r++) {
            for (int s = 0; s < SECTORS; s++) {
                float y = (float) Math.sin(-Math.PI/2 + Math.PI * r * R);
                float x = (float) (Math.cos(2*Math.PI * s * S) * Math.sin(Math.PI * r * R));
                float z = (float) (Math.sin(2 * Math.PI * s * S) * Math.sin(Math.PI * r * R));

                normalArray[k++] = x;
                normalArray[k++] = y;
                normalArray[k++] = z;
            }
        }

        return normalArray;
    }

    public Mat4 getScaleMatrix() {
        return new Mat4(new Vec4(INITIAL_SCALE, 0, 0, 0),
                new Vec4(0, INITIAL_SCALE, 0, 0),
                new Vec4(0, 0, INITIAL_SCALE, 0),
                new Vec4(0, 0, 0, 1));
    }
}
