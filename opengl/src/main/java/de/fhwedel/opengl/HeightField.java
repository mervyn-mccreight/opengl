package de.fhwedel.opengl;

public class HeightField {
    public static final int DIMENSION = 200;
    public static final int COLUMN_HEIGHT = DIMENSION / 4;
    public static final double COLUMN_WIDTH = 1.0;
    public static final int COLUMN_VELOCITY = 0;
    private static final double SPEED = 7;

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
                float y = Math.max(COLUMN_HEIGHT - 0.01f * (i*i + j*j), 0);

                mColumns[i][j] = new Column(COLUMN_HEIGHT + y, COLUMN_VELOCITY);
                mNewColumns[i][j] = new Column(COLUMN_HEIGHT + y, COLUMN_VELOCITY);
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

    public void update(long deltaT) {
        for (int j = 0; j < DIMENSION; j++) {
            for (int i = 0; i < DIMENSION; i++) {
                Column center = getColumn(mColumns, i, j);
                Column left = getColumn(mColumns, i-1, j);
                Column right = getColumn(mColumns, i+1, j);
                Column top = getColumn(mColumns, i, j-1);
                Column bottom = getColumn(mColumns, i, j+1);

                double f = SPEED * SPEED * (left.height + right.height + top.height + bottom.height - 4*center.height) / (COLUMN_WIDTH * COLUMN_WIDTH);


                center.velocity += f * deltaT / 1000f;
                Column newColumn = getColumn(mNewColumns, i, j);
                newColumn.height = center.height + center.velocity * deltaT / 1000f;

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
        //@todo: to be implemented.
        return new float[9];
    }

    public float[] getNormalArray() {
        return TrianglePack.fromVertexArray(getVertexArray()).normalArray();
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


}
