package de.fhwedel.opengl;

import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.VectorUtil;

public class Triangle {
    private final float[] v0;
    private final float[] v1;
    private final float[] v2;
    private final float[] normal;

    public Triangle(float[] v0, float[] v1, float[] v2) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.normal = VectorUtil.getNormalVec3(new float[3], v0, v1, v2, new float[3], new float[3]) ;
    }

    public boolean hasVertex(float [] toCheck) {
        return VectorUtil.isVec3Equal(v0, 0, toCheck, 0, FloatUtil.EPSILON)
                || VectorUtil.isVec3Equal(v1, 0, toCheck, 0, FloatUtil.EPSILON)
                || VectorUtil.isVec3Equal(v2, 0, toCheck, 0, FloatUtil.EPSILON);
    }

    public float[] normal() {
        return normal;
    }
}
