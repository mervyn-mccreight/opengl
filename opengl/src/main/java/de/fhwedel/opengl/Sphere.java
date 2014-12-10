package de.fhwedel.opengl;

import com.hackoeur.jglm.Vec3;

public class Sphere {
    private float radius;
    private Vec3 position;

    public boolean contains(float x, float y, float z) {
        return (position.getX() + radius > x || position.getX() - radius < x)
                && (position.getY() + radius > y || position.getY() - radius < y)
                && (position.getZ() + radius > z || position.getZ() - radius < z);
    }

    public float getY(float x, float z) {
        return (float) Math.sqrt(Math.pow(x-position.getX(), 2) + Math.pow(z-position.getZ(), 2) - Math.pow(radius, 2)) - position.getY();
    }

}
