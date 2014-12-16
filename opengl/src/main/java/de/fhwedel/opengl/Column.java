package de.fhwedel.opengl;

public class Column {

    public float height;
    public float velocity;
    public float replaced;
    public float replacedDelta;
    public float[] normal;

    public Column(float height, float velocity, float replaced, float replacedDelta, float[] normal) {
        this.height = height;
        this.velocity = velocity;
        this.replaced = replaced;
        this.replacedDelta = replacedDelta;
        this.normal = normal;
    }
}