/*
  **********************************************************************************************************************************
  * Source : https://github.com/Tau-ri-Dev/JSGMod-1.18.2/blob/main/Core/src/main/java/dev/tauri/jsgcore/loader/model/OBJModel.java *
  **********************************************************************************************************************************
*/
package fr.azures.mod.sgcraft_reborn.utils.loader.models;

import static org.lwjgl.opengl.GL11.GL_FLAT;
import static org.lwjgl.opengl.GL11.GL_SMOOTH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL32;

import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3f;

public class OBJModel {
    private final List<Vector3f> vertices;
    private final List<Vector2f> textureCoords;
    private final List<Vector3f> normals;
    private final List<Face> faces;
    private boolean enableSmoothShading;

    public void render() {
        OBJLoader.render(this);
    }

    public OBJModel(List<Vector3f> vertices, List<Vector2f> textureCoords, List<Vector3f> normals, List<Face> faces, boolean enableSmoothShading) {
        this.vertices = vertices;
        this.textureCoords = textureCoords;
        this.normals = normals;
        this.faces = faces;
        this.enableSmoothShading = enableSmoothShading;
    }

    public OBJModel() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), true);
    }

    public void enableStates() {
        if (this.isSmoothShadingEnabled()) {
            GL32.glShadeModel(GL_SMOOTH);
        } else {
            GL32.glShadeModel(GL_FLAT);
        }
    }

    public boolean hasTextureCoordinates() {
        return this.getTextureCoordinates().size() > 0;
    }

    public boolean hasNormals() {
        return this.getNormals().size() > 0;
    }

    public List<Vector3f> getVertices() {
        return this.vertices;
    }

    public List<Vector2f> getTextureCoordinates() {
        return this.textureCoords;
    }

    public List<Vector3f> getNormals() {
        return this.normals;
    }

    public List<Face> getFaces() {
        return this.faces;
    }

    public boolean isSmoothShadingEnabled() {
        return this.enableSmoothShading;
    }

    public void setSmoothShadingEnabled(boolean isSmoothShadingEnabled) {
        this.enableSmoothShading = isSmoothShadingEnabled;
    }

    public static class Face {

        private final int[] vertexIndices;
        private final int[] normalIndices;
        private final int[] textureCoordinateIndices;

        public boolean hasNormals() {
            return this.normalIndices != null;
        }

        public boolean hasTextureCoords() {
            return this.textureCoordinateIndices != null;
        }

        public int[] getVertices() {
            return this.vertexIndices;
        }

        public int[] getTextureCoords() {
            return this.textureCoordinateIndices;
        }

        public int[] getNormals() {
            return this.normalIndices;
        }

        public Face(int[] vertexIndices, int[] textureCoordinateIndices, int[] normalIndices) {
            super();

            this.vertexIndices = vertexIndices;
            this.normalIndices = normalIndices;
            this.textureCoordinateIndices = textureCoordinateIndices;
        }

        @Override
        public String toString() {
            return String.format("Face[vertexIndices%s normalIndices%s textureCoordinateIndices%s]",
                    Arrays.toString(vertexIndices), Arrays.toString(normalIndices), Arrays.toString(textureCoordinateIndices));
        }
    }
}