package de.fhwedel.opengl;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.jogamp.opengl.math.VectorUtil;

import java.util.ArrayList;
import java.util.List;

public class TrianglePack extends ArrayList<Triangle> {

    private float[] vertexArray;

    private TrianglePack(float[] vertexArray) {
        this.vertexArray = vertexArray;
    }

    public static TrianglePack fromVertexArray(float[] vertexArray) {
        if (vertexArray.length % 9 != 0) {
            throw new IllegalArgumentException("count of vertices on array incorrect.");
        }

        TrianglePack pack = new TrianglePack(vertexArray);

        for (int i = 0; i < vertexArray.length; i += 9) {
            float[] v0 = {vertexArray[i], vertexArray[i+1], vertexArray[i+2]};
            float[] v1 = {vertexArray[i+3], vertexArray[i+4], vertexArray[i+5]};
            float[] v2 = {vertexArray[i+6], vertexArray[i+7], vertexArray[i+8]};

            pack.add(new Triangle(v0, v1, v2));
        }

        return pack;
    }

    private List<Triangle> containingVertex(final float[] v0) {
       return FluentIterable.from(this).filter(new Predicate<Triangle>() {
            @Override
            public boolean apply(Triangle triangle) {
                return triangle.hasVertex(v0);
            }
        }).toList();
    }

    public float[] normalArray() {
        float[] result = new float[vertexArray.length];

        for (int i = 0; i < vertexArray.length; i +=3) {
            float[] v = {vertexArray[i], vertexArray[i + 1], vertexArray[i + 2]};

            List<Triangle> triangles = containingVertex(v);

            float[] normal = {0, 0, 0};

            for (Triangle triangle : triangles) {
                VectorUtil.addVec3(normal, normal, triangle.normal());
            }

            normal = VectorUtil.normalizeVec3(normal);

            result[i] = normal[0];
            result[i+1] = normal[1];
            result[i+2] = normal[2];
        }

        return result;
    }


}
