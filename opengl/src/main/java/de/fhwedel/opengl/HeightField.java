package de.fhwedel.opengl;

import com.google.common.collect.Lists;
import com.hackoeur.jglm.Mat4;
import com.hackoeur.jglm.Vec3;
import com.hackoeur.jglm.Vec4;
import com.jogamp.opengl.math.VectorUtil;

import java.util.List;

import static java.lang.Math.pow;

public class HeightField {
    public static final int DIMENSION = 100;
    public static final float COLUMN_HEIGHT = DIMENSION / 100;
    public static final float COLUMN_WIDTH = 1f;
    private static final Vec3 INITIAL_POSITION = new Vec3(-COLUMN_WIDTH * DIMENSION / 2, -COLUMN_HEIGHT, -COLUMN_WIDTH * DIMENSION / 2);
    private static final float SPEED = COLUMN_WIDTH * 30;
    public static final int COLUMN_VELOCITY = 0;
    public static final float MAX_SLOPE = 0.3f;
    private static final float SCALING_FACTOR = 0.98f;
    private static final float INITIAL_SCALE = 0.3f;
    private static final float WATER_DENSITY = 0.2f;
    private final List<Integer> indices;

    private Column[][] mColumns;
    private Column[][] mNewColumns;
    private float[] vertexArray;
    private float[] normalArray;

    private List<Sphere> spheres = Lists.newArrayList();

    public HeightField() {
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
//                float y = Math.max(COLUMN_HEIGHT - 0.01f * (i*i + j*j), 0);
                float y = (float) Math.random() * 0.00f;

                mColumns[i][j] = new Column(COLUMN_HEIGHT + y, COLUMN_VELOCITY, new float[3]);
                mNewColumns[i][j] = new Column(COLUMN_HEIGHT + y, COLUMN_VELOCITY, new float[3]);
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

    private Column getColumnFromXZ(Column[][] columnArray, int x, int z) {
        x -= getPosition().getX();
        z -= getPosition().getZ();

        return getColumn(columnArray, (int) (x / COLUMN_WIDTH), (int) (z / COLUMN_WIDTH));
    }

    public Column getColumnFromXZ(int x, int z) {
        return getColumnFromXZ(mColumns, x, z);
    }

    public void update(float deltaT) {
        applyLogic(deltaT);
        calculateNormals();
        applySphereInteraction();
    }

    private void applySphereInteraction() {
        for (Sphere sphere : spheres) {
            Vec3 spherePos = sphere.getPosition();
            float radius = sphere.getRadius();

            float minX = spherePos.getX() - radius;
            float maxX = spherePos.getX() + radius;
            float minZ = spherePos.getZ() - radius;
            float maxZ = spherePos.getZ() + radius;

            int minI = (int) ((minZ - getPosition().getZ()) / COLUMN_WIDTH);
            int maxI = (int) ((maxZ - getPosition().getZ()) / COLUMN_WIDTH);
            int minJ = (int) ((minX - getPosition().getX()) / COLUMN_WIDTH);
            int maxJ = (int) ((maxX - getPosition().getX()) / COLUMN_WIDTH);

            float volume = 0;
            for (int i = minI; i < maxI; i++) {
                for (int j = minJ; j < maxJ; j++) {
                    Column column = getColumn(mColumns, i, j);
                    float x = (i * COLUMN_WIDTH) + getPosition().getX();
                    float y = column.height + getPosition().getY();
                    float z = (j * COLUMN_WIDTH) + getPosition().getZ();

                    if (sphere.isBelow(x, y, z)) {
                        float difference = column.height - sphere.getY(x, z);
                        volume += difference;
                        column.velocity = -difference;
                        column.height = sphere.getY(x, z);
                    }
                }
            }

            int actualCount = 0;
            float volume2 = 0f;
            int count = 2 * (maxI - minI + 2) + 2 * (maxJ - minJ + 2); // dark magic
            for (int i = minI - 1; i <= maxI + 1; i++) {
                for (int j = minJ - 1; j <= maxJ + 1; j++) {
                    if (i == minI - 1 || i == maxI + 1 || j == minJ - 1 || j == maxJ + 1) {
                        ++actualCount;
                        Column column = getColumn(mColumns, i, j);
                        volume2 += volume / count;
                        column.height += volume / count;
                    }
                }
            }

            Vec3 force = new Vec3(0, -volume * COLUMN_WIDTH * COLUMN_WIDTH * WATER_DENSITY * RenderLoop.GRAVITY.getY(), 0);
            sphere.applyForce(force);
        }
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

    public Mat4 getScaleMatrix() {
        return new Mat4(new Vec4(INITIAL_SCALE, 0, 0, 0),
                new Vec4(0, INITIAL_SCALE, 0, 0),
                new Vec4(0, 0, INITIAL_SCALE, 0),
                new Vec4(0, 0, 0, 1));
    }

    public void sprinkle() {
        float increaser = 5;

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

    public void addSphere(Sphere sphere) {
        spheres.add(sphere);
    }
}
