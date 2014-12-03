package de.fhwedel.opengl;

import com.google.common.collect.Lists;
import com.hackoeur.jglm.Vec3;
import com.jogamp.opengl.math.VectorUtil;

import java.util.List;

public class HeightField {
    public static final int DIMENSION = 100;
    public static final int COLUMN_HEIGHT = DIMENSION / 10;
    public static final double COLUMN_WIDTH = 0.5;
    public static final int COLUMN_VELOCITY = 0;
    private static final double SPEED = 3;

    private static final Vec3 INITIAL_POSITION = new Vec3((float) (-COLUMN_WIDTH * DIMENSION) / 2, -COLUMN_HEIGHT, (float) (-COLUMN_WIDTH * DIMENSION) / 2);

    private Column[][] mColumns;
    private Column[][] mNewColumns;

    public HeightField() {
        initColumns();
    }

    private void initColumns() {
        mColumns = new Column[DIMENSION][DIMENSION];
        mNewColumns = new Column[DIMENSION][DIMENSION];

        for (int j = 0; j < DIMENSION; j++) {
            for (int i = 0; i < DIMENSION; i++) {
                float y = Math.max(COLUMN_HEIGHT - 0.01f * (i * i + j * j), 0);

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

    public void update(double deltaT) {
        for (int j = 0; j < DIMENSION; j++) {
            for (int i = 0; i < DIMENSION; i++) {
                Column center = getColumn(mColumns, i, j);
                Column left = getColumn(mColumns, i - 1, j);
                Column right = getColumn(mColumns, i + 1, j);
                Column top = getColumn(mColumns, i, j - 1);
                Column bottom = getColumn(mColumns, i, j + 1);

                double f = SPEED * SPEED * (left.height + right.height + top.height + bottom.height - 4 * center.height) / (COLUMN_WIDTH * COLUMN_WIDTH);

                center.velocity += f * deltaT;
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

        for (int j = 0; j < DIMENSION; j++) {
            for (int i = 0; i < DIMENSION; i++) {
                Column center = getColumn(mColumns, i, j);
                Column left = getColumn(mColumns, i - 1, j);
                Column right = getColumn(mColumns, i + 1, j);
                Column top = getColumn(mColumns, i, j - 1);
                Column bottom = getColumn(mColumns, i, j + 1);

                // (u[i+1,j]+u[i-1,j]+u[i,j+1]+u[i,j-1])/4 – u[i,j]
                // calculates average slope in all four directions
                double offset = (right.height + left.height + bottom.height + top.height) / 4 - center.height;

                // maxslope is in percent between 0 and 1.
                double maxOffset = 0.3 * COLUMN_WIDTH;

                if (offset > maxOffset) {
                    center.height += offset - maxOffset;
                } else if (offset < -maxOffset) {
                    center.height += offset + maxOffset;
                }
            }
        }

        for (int j = 0; j < DIMENSION; j++) {
            for (int i = 0; i < DIMENSION; i++) {
                getColumn(mColumns, i, j).normal = getNormalFor(i, j);
            }
        }

    }

    public float[] getVertexArray() {
        List<Integer> indices = indices();

        float[] result = new float[indices.size() * 3];

        int k = 0;

        for (Integer integer : indices) {
            int i = integer % DIMENSION;
            int j = integer / DIMENSION;

            result[k++] = (float) (j * COLUMN_WIDTH);
            result[k++] = (float) mColumns[i][j].height;
            result[k++] = (float) (i * COLUMN_WIDTH);
        }

        return result;
    }

    public float[] getNormals() {
        List<Integer> indices = indices();

        float[] result = new float[indices.size() * 3];

        int k = 0;

        for (Integer integer : indices) {
            int i = integer % DIMENSION;
            int j = integer / DIMENSION;

            result[k++] = mColumns[i][j].normal[0];
            result[k++] = mColumns[i][j].normal[1];
            result[k++] = mColumns[i][j].normal[2];
        }

        return result;
    }


    public float[] getNormalFor(int i, int j) {
        float[] result = new float[3];

        Vec3 normal = new Vec3((float) ((getColumn(mColumns, i - 1, j).height - getColumn(mColumns, i + 1, j).height) / COLUMN_WIDTH),
                (float) ((getColumn(mColumns, i, j + 1).height - getColumn(mColumns, i, j - 1).height) / COLUMN_WIDTH),
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

//    for (int j = 0; j < DIMENSION - 1; j++) {
//        gl.glBegin(GL2.GL_TRIANGLE_STRIP);
//
//        for (int i = 0; i < DIMENSION; i++) {
//            gl.glVertex3d(i * COLUMN_WIDTH,  mColumns[i][j].height, j * COLUMN_WIDTH);
//            gl.glVertex3d(i * COLUMN_WIDTH,  mColumns[i][j+1].height, (j+1) * COLUMN_WIDTH);
//        }
//
//        gl.glEnd();
//    }

    public Vec3 getPosition() {
        return INITIAL_POSITION;
    }

}
