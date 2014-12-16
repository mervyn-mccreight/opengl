package de.fhwedel.opengl;


import com.hackoeur.jglm.Vec3;

public class World {
    private static final Vec3 GRAVITY = new Vec3(0, -9.81f, 0);
    private static final Vec3 NO_GRAVITY = new Vec3(0, 0, 0);

    private Vec3 gravity;

    public World() {
        this.gravity = NO_GRAVITY;
    }

    public Vec3 getGravity() {
        return gravity;
    }

    public void toggleGravity() {
        gravity = gravity.equals(GRAVITY) ? NO_GRAVITY : GRAVITY;
    }
}
