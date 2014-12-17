package de.fhwedel.opengl;

import com.google.common.collect.Lists;
import com.hackoeur.jglm.Mat4;
import com.hackoeur.jglm.Vec3;
import com.hackoeur.jglm.Vec4;

import java.util.List;

public class Sphere {
    private static final int RINGS = 100;
    private static final float R = 1f/(RINGS-1);
    private static final int SECTORS = 100;
    private static final float S = 1f/(SECTORS-1);
    private static final float INITIAL_SCALE = 0.3f;
    private static final float DENSITY = 400f; // in kg/m^3

    private final float[] vertexArray;
    private final float[] normalArray;

    private float radius;
    private Vec3 position;
    private Vec3 velocity = Vec3.VEC3_ZERO;
    private List<Vec3> forces = Lists.newArrayList();
    private float mass;

    public Sphere(Vec3 position, float radius) {
        this.radius = radius;
        this.position = position;
        mass = getVolume() * DENSITY;

        vertexArray = new float[RINGS * SECTORS * 3];
        normalArray = new float[RINGS * SECTORS * 3];
    }

    public float getVolume() {
        float diameter = radius * 2;
        return (float) (Math.PI * (Math.pow(diameter, 3) / 6f)); // pi * d^3/6
    }

    public boolean contains(float x, float y, float z) {
        Vec3 vec = new Vec3(x-position.getX(), y-position.getY(), z-position.getZ());
        return vec.getLength() <= radius;
    }

    public boolean isBelow(float x, float y, float z) {
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
}
