package de.fhwedel.opengl;

public class Column {

    public float height;
    public float velocity;
    public float[] normal;

    public Column(float height, float velocity, float[] normal) {
        this.height = height;
        this.velocity = velocity;
        this.normal = normal;
    }
}