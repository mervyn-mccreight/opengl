package de.fhwedel.opengl;

public class Column {

    public double height;
    public double velocity;
    public float[] normal;

    public Column(double height, double velocity, float[] normal) {
        this.height = height;
        this.velocity = velocity;
        this.normal = normal;
    }
}