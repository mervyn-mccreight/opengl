package de.fhwedel.opengl;

import com.google.common.collect.Lists;
import com.hackoeur.jglm.Mat4;
import com.hackoeur.jglm.Vec3;
import com.hackoeur.jglm.Vec4;

import java.util.List;

public class Sphere {
    private static final int RINGS = 20;
    private static final float R = 1f/(RINGS-1);
    private static final int SECTORS = 20;
    private static final float S = 1f/(SECTORS-1);
    private static final float DENSITY = 400f; // in kg/m^3

    private float radius;
    private Vec3 position;
    private Vec3 velocity = Vec3.VEC3_ZERO;
    private List<Vec3> forces = Lists.newArrayList();
    private float mass;
    private float[] vertices;
    private float[] normalArray;
    private int[] indices;
    private float[] vertexArray;
    private float[] normals;

    public Sphere(Vec3 position, float radius) {
        this.radius = radius;
        this.position = position;
        mass = getVolume() * DENSITY;

        vertices = new float[RINGS * SECTORS * 3];
        normals = new float[RINGS * SECTORS * 3];
        indices = new int[RINGS * SECTORS * 4];
        vertexArray = new float[indices.length * 3];
        normalArray = new float[indices.length * 3];

        calcArrays();
    }

    public float getVolume() {
        float diameter = radius * 2;
        return (float) (Math.PI * (Math.pow(diameter, 3) / 6f)); // pi * d^3/6
    }

    public boolean contains(float x, float y, float z) {
        Vec3 vec = new Vec3(x-position.getX(), y-position.getY(), z-position.getZ());
        return vec.getLength() <= radius;
    }

    public boolean touches(float x, float y, float z) {
        Float sphereY = getBottomHalfY(x, z);
        if (sphereY.equals(Float.NaN)) {
            return false;
        }

        boolean b = sphereY <= y;
        return b;
    }

    public float getTopHalfY(float x, float z) {
        float v = (float) Math.sqrt(Math.pow(radius, 2) - Math.pow(x - position.getX(), 2) - Math.pow(z - position.getZ(), 2)) + position.getY();
        return v;
    }

    public float getBottomHalfY(float x, float z) {
        float v = (float) -Math.sqrt(Math.pow(radius, 2) - Math.pow(x - position.getX(), 2) - Math.pow(z - position.getZ(), 2)) + position.getY();
        return v;
    }

    public float getRadius() {
        return radius;
    }

    public Vec3 getPosition() {
        return position;
    }

    public void moveBy(Vec3 vec) {
        position = position.add(vec);
    }

    private void calcArrays() {
        calcVerticesAndNormals();
        calcIndexArray();
        calcVertexArray();
        calcNormalArray();
    }

    private void calcVerticesAndNormals() {
        int k = 0;
        int n = 0;

        for (int r = 0; r < RINGS; r++) {
            for (int s = 0; s < SECTORS; s++) {
                float y = (float) Math.sin(-Math.PI/2 + Math.PI * r * R);
                float x = (float) (Math.cos(2*Math.PI * s * S) * Math.sin(Math.PI * r * R));
                float z = (float) (Math.sin(2 * Math.PI * s * S) * Math.sin(Math.PI * r * R));

                vertices[k++] = x * radius;
                vertices[k++] = y * radius;
                vertices[k++] = z * radius;

                normals[n++] = x;
                normals[n++] = y;
                normals[n++] = z;
            }
        }
    }

    private void calcIndexArray() {
        int k = 0;

        for (int r = 0; r < RINGS-1; r++) {
            for (int s = 0; s < SECTORS-1; s++) {
                indices[k++] = r * SECTORS + s;
                indices[k++] = (r+1) * SECTORS + s;
                indices[k++] = (r+1) * SECTORS + (s+1);
                indices[k++] = r * SECTORS + (s+1);
            }
        }
    }

    private void calcVertexArray() {
        int k = 0;

        for (int index : indices) {
            vertexArray[k++] = vertices[3 * index + 0];
            vertexArray[k++] = vertices[3 * index + 1];
            vertexArray[k++] = vertices[3 * index + 2];
        }
    }

    private void calcNormalArray() {
        int k = 0;

        for (int index : indices) {
            normalArray[k++] = normals[3 * index + 0];
            normalArray[k++] = normals[3 * index + 1];
            normalArray[k++] = normals[3 * index + 2];
        }
    }

    public float[] getVertexArray() {
        return vertexArray;
    }

    public float[] getNormalsArray() {
        return normalArray;
    }

    public Mat4 getScaleMatrix(float scaleFactor) {
        return new Mat4(new Vec4(scaleFactor, 0, 0, 0),
                new Vec4(0, scaleFactor, 0, 0),
                new Vec4(0, 0, scaleFactor, 0),
                new Vec4(0, 0, 0, 1));
    }

    public void update(float deltaT) {
        Vec3 force = Vec3.VEC3_ZERO;

        for (Vec3 vec3 : forces) {
            force = force.add(vec3);
        }
        //System.out.println("Sphere force: " + force);

        Vec3 acceleration = force.scale(1f/ mass);

        position = position.add(velocity.scale(deltaT));
        velocity = velocity.add(acceleration.scale(deltaT));

        forces.clear();
    }

    public void applyForce(Vec3 force) {
        forces.add(force);
    }

    public float getMass() {
        return mass;
    }

    public boolean isMovingUp() {
        return velocity.getY() > 0;
    }

    public void scaleVelocity(float factor) {
        velocity = velocity.scale(factor);
    }
}
