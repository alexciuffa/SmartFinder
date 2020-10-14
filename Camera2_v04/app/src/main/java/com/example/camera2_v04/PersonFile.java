package com.example.camera2_v04;

public class PersonFile {
    private String name;
    private float[] embedding;

    PersonFile(String n, float[] e) {
        name = n;
        embedding = e;
    }

    public String getName() {
        return name;
    }

    public float[] getEmbedding() {
        return embedding;
    }
}
