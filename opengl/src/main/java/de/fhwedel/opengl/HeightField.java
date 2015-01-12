package de.fhwedel.opengl;

import com.google.common.collect.Lists;
import com.hackoeur.jglm.Mat4;
import com.hackoeur.jglm.Vec3;
import com.hackoeur.jglm.Vec4;
import com.jogamp.opengl.math.VectorUtil;

import java.util.Collection;
import java.util.List;

import static java.lang.Math.pow;

public class HeightField {
    public static final int DIMENSION = 100;
    public static final float COLUMN_HEIGHT = DIMENSION;
    public static final float COLUMN_WIDTH = 0.8f; // in meters.
    private static final Vec3 INITIAL_POSITION = new Vec3(-COLUMN_WIDTH * DIMENSION / 2, -COLUMN_HEIGHT, -COLUMN_WIDTH * DIMENSION / 2);
    private static final float SPEED = 5f; // <measure-unit of width> per second, since delta-time is given in seconds.
    private static final float SCALING_FACTOR = 0.999f;
    private static final float WATER_DENSITY = 999.97f; // in kg/m^3
    private final List<Integer> indices;
    private final World world;

    private Column[][] mColumns;
    private Column[][] mNewColumns;
    private float[] vertexArray;
    private float[] normalArray;

    private List<Sphere> spheres = Lists.newArrayList();

    public HeightField(World world) {
        this.world = world;
        initColumns();
        indices = indices();
        vertexArray = new float[indices.size() * 3];
        normalArray = new float[indices.size() * 3];

        getVertexArray();
        getNormals();
    }

    private void initColumns() {
        mColumns = new Column[DIMENSION][DIMENSION];
        mNewColumns = new Column[DIMENSION][DIMENSION];

        for (int j = 0; j < DIMENSION; j++) {
            for (int i = 0; i < DIMENSION; i++) {
                int initialVelocity = 0;
                float initialHeight = COLUMN_HEIGHT;

                mColumns[i][j] = new Column(initialHeight, initialVelocity, 0f, 0f, new float[3]);
                mNewColumns[i][j] = new Column(initialHeight, initialVelocity, 0f, 0f, new float[3]);
            }
        }
    }

    private Column getColumn(Column[][] columnArray, int i, int j) {
        i = Math.max(i, 0);
        i = Math.min(i, DIMENSION - 1);
        j = Math.max(j, 0);
        j = Math.min(j, DIMENSION - 1);

        return columnArray[i][j];
    }

    public void update(float deltaT) {
        applyLogic(deltaT);
        calculateNormals();
        applySphereInteraction();
        clearSphereInteractionForNonSphereColumns();
    }

    private void clearSphereInteractionForNonSphereColumns() {
        for (int i = 0; i < DIMENSION; i++) {
            for (int j = 0; j < DIMENSION; j++) {
                float x = j*COLUMN_WIDTH + getPosition().getX();
                float z = i*COLUMN_WIDTH + getPosition().getZ();

                boolean flag = false;

                for (Sphere sphere : spheres) {
                     flag = flag || (sphere.touches(x, getColumn(mColumns, i, j).height + getPosition().getY(), z));
                }

                if (!flag) {
                    getColumn(mColumns, i, j).replaced = 0;
                    getColumn(mColumns, i, j).replacedDelta = 0;
                }
            }
        }
    }

    private void applySphereInteraction() {
        for (Sphere sphere : spheres) {
            Vec3 spherePos = sphere.getPosition();
            float radius = sphere.getRadius();

            float minX = spherePos.getX() - radius;
            float maxX = spherePos.getX() + radius;
            float minZ = spherePos.getZ() - radius;
            float maxZ = spherePos.getZ() + radius;

            int minJ = (int) ((minX - getPosition().getX()) / COLUMN_WIDTH);
            int maxJ = (int) ((maxX - getPosition().getX()) / COLUMN_WIDTH);
            int minI = (int) ((minZ - getPosition().getZ()) / COLUMN_WIDTH);
            int maxI = (int) ((maxZ - getPosition().getZ()) / COLUMN_WIDTH);


            // try hybrid implementation.
            // if sphere(x,z) needs water above itself, this implementation is correct.
            // otherwise old logic, where sphere "pushes" the water down is applied.
            float displacedVolume = 0f;

            for (int i = minI; i < maxI; i++) {
                for (int j = minJ; j < maxJ; j++) {
                    Column column = getColumn(mColumns, i, j);
                    float x = (j * COLUMN_WIDTH) + getPosition().getX();
                    float y = column.height + getPosition().getY();
                    float z = (i * COLUMN_WIDTH) + getPosition().getZ();

                    if (sphere.touches(x, y, z)) {
                        if (!sphere.isMovingUp()) {
                            sphere.scaleVelocity(0.9999f);
                        }

                        // this part of sphere is completely covered with water.
                        if (sphere.getTopHalfY(x, z) <= y) {
                            float replacedHeight = sphere.getTopHalfY(x, z) - sphere.getBottomHalfY(x, z);
                            displacedVolume += replacedHeight * COLUMN_WIDTH * COLUMN_WIDTH; // *width*height to get volume
                            column.replacedDelta = replacedHeight - column.replaced;
                            column.replaced = replacedHeight;
                        } else { // this part of sphere is only dipped in water.
                            float replacedHeight = y - sphere.getBottomHalfY(x, z);
                            displacedVolume += replacedHeight * COLUMN_WIDTH * COLUMN_WIDTH; // *width*height to get volume
                            column.replacedDelta = replacedHeight - column.replaced;
                            column.replaced = replacedHeight;
                        }
                    }
                }
            }

            // archimedes principle: forceUp = displacedVolume * density * gravity
            // problem: how to determine displacedVolume?
            // currently: the weakener the resolution of the water grid is, the
            // less accurate is the displaced volume calculation.

            // another problem: the force when breaking the water surface is missing.
            // this is why the object is bouncing that much.
            Vec3 force = new Vec3(0, displacedVolume * WATER_DENSITY * world.getGravity().scale(-1).getY(), 0);
            sphere.applyForce(force);
        }
    }

    private float calcVolume() {
        float volume = 0f;
        for (int i = 0; i < DIMENSION; i++) {
            for (int j = 0; j < DIMENSION; j++) {
                volume += getColumn(mColumns, i, j).height * COLUMN_WIDTH * COLUMN_WIDTH;
            }
        }

        return volume;
    }

    private void calculateNormals() {
        for (int j = 0; j < DIMENSION; j++) {
            for (int i = 0; i < DIMENSION; i++) {
                getColumn(mColumns, i, j).normal = getNormalFor(i, j);
            }
        }
    }

    private void applyLogic(float deltaT) {
        for (int j = 0; j < DIMENSION; j++) {
            for (int i = 0; i < DIMENSION; i++) {
                Column column = getColumn(mColumns, i, j);
                Column left = getColumn(mColumns, i - 1, j);
                Column right = getColumn(mColumns, i + 1, j);
                Column top = getColumn(mColumns, i, j - 1);
                Column bottom = getColumn(mColumns, i, j + 1);

                left.height += column.replacedDelta / 6;
                right.height += column.replacedDelta / 6;
                top.height += column.replacedDelta / 6;
                bottom.height += column.replacedDelta / 6;
            }
        }

        for (int j = 0; j < DIMENSION; j++) {
            for (int i = 0; i < DIMENSION; i++) {
                Column center = getColumn(mColumns, i, j);
                Column left = getColumn(mColumns, i - 1, j);
                Column right = getColumn(mColumns, i + 1, j);
                Column top = getColumn(mColumns, i, j - 1);
                Column bottom = getColumn(mColumns, i, j + 1);

                double f = pow(SPEED, 2) * (left.height + right.height + top.height + bottom.height - 4 * center.height) / (pow(COLUMN_WIDTH, 2));

                center.velocity += f * deltaT;
                center.velocity *= SCALING_FACTOR;

                Column newColumn = getColumn(mNewColumns, i, j);
                newColumn.height = center.height + center.velocity * deltaT;
            }
        }

        for (int j = 0; j < DIMENSION; j++) {
            for (int i = 0; i < DIMENSION; i++) {
                Column column = getColumn(mColumns, i, j);
                Column newColumn = getColumn(mNewColumns, i, j);

                column.height = newColumn.height;
            }
        }


    }

    public float[] getVertexArray() {
        int k = 0;

        for (Integer integer : indices) {
            int i = integer % DIMENSION;
            int j = integer / DIMENSION;

            vertexArray[k++] = j * COLUMN_WIDTH;
            vertexArray[k++] = mColumns[i][j].height;
            vertexArray[k++] = i * COLUMN_WIDTH;
        }

        return vertexArray;
    }

    public float[] getNormals() {
        int k = 0;

        for (Integer integer : indices) {
            int i = integer % DIMENSION;
            int j = integer / DIMENSION;

            normalArray[k++] = mColumns[i][j].normal[0];
            normalArray[k++] = mColumns[i][j].normal[1];
            normalArray[k++] = mColumns[i][j].normal[2];
        }

        return normalArray;
    }


    public float[] getNormalFor(int i, int j) {
        float[] result = new float[3];

        Vec3 normal = new Vec3((getColumn(mColumns, i - 1, j).height - getColumn(mColumns, i + 1, j).height) / COLUMN_WIDTH,
                (getColumn(mColumns, i, j + 1).height - getColumn(mColumns, i, j - 1).height) / COLUMN_WIDTH,
                2);

        float[] normalized = VectorUtil.normalizeVec3(new float[3], normal.getArray());

        result[0] = normalized[1];
        result[1] = normalized[2];
        result[2] = normalized[0];

        return result;
    }

    private List<Integer> indices() {
        List<Integer> result = Lists.newArrayList();

        int vertexCount = DIMENSION * DIMENSION;
        int col = 0;

        int offset = DIMENSION - 1;
        int length = DIMENSION;

        for (int i = 0; i < vertexCount && (col < length - 1); i++) {
            if ((i + 1) % length == 0) {
                col = col + 1;
            } else {
                result.add(i);
                result.add(i + offset + 2);
                result.add(i + offset + 1);

                result.add(i);
                result.add(i + 1);
                result.add(i + offset + 2);
            }
        }

        return result;
    }

    public Vec3 getPosition() {
        return INITIAL_POSITION;
    }

    public Mat4 getScaleMatrix(float scaleFactor) {
        return new Mat4(new Vec4(scaleFactor, 0, 0, 0),
                new Vec4(0, scaleFactor, 0, 0),
                new Vec4(0, 0, scaleFactor, 0),
                new Vec4(0, 0, 0, 1));
    }

    public void sprinkle() {
        float increaser = 0.7f;
        int v = (int) (Math.random() * DIMENSION);
        int u = (int) (Math.random() * DIMENSION);

        getColumn(mColumns, u, v).height += increaser;
        getColumn(mColumns, u, v).velocity = increaser;

        getColumn(mColumns, u - 1, v - 1).height += increaser / 2;
        getColumn(mColumns, u - 1, v - 1).velocity = increaser / 2;
        getColumn(mColumns, u, v - 1).height += increaser / 2;
        getColumn(mColumns, u, v - 1).velocity = increaser / 2;
        getColumn(mColumns, u + 1, v - 1).height += increaser / 2;
        getColumn(mColumns, u + 1, v - 1).velocity = increaser / 2;

        getColumn(mColumns, u - 1, v).height += increaser / 2;
        getColumn(mColumns, u - 1, v).velocity = increaser / 2;
        getColumn(mColumns, u + 1, v).height += increaser / 2;
        getColumn(mColumns, u + 1, v).velocity = increaser / 2;

        getColumn(mColumns, u - 1, v + 1).height += increaser / 2;
        getColumn(mColumns, u - 1, v + 1).velocity = increaser / 2;
        getColumn(mColumns, u, v + 1).height += increaser / 2;
        getColumn(mColumns, u, v + 1).velocity = increaser / 2;
        getColumn(mColumns, u + 1, v + 1).height += increaser / 2;
        getColumn(mColumns, u + 1, v + 1).velocity = increaser / 2;
    }

    public void addSphere(Collection<Sphere> toAdd) {
        spheres.addAll(toAdd);
    }
}
