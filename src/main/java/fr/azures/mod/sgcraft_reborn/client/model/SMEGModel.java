package fr.azures.mod.sgcraft_reborn.client.model;

import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;

public class SMEGModel {
    private double[] bounds;
    private Face[] faces;
    private Box[] boxes;

    public static class Face {
    	public int texture;
    	public Vertex[] vertices;
        public int[][] triangles;

        public static class Vertex {
        	public double x, y, z;
            public double nx;
            public double ny;
            public double nz;
            public double u, v;
        }
    }

    public static class Box {
        private double[] bounds;

        public Box(double[] bounds) {
            this.bounds = bounds;
        }

        public double[] getBounds() {
            return bounds;
        }
    }

    public static SMEGModel loadFromFile(ResourceLocation resourceLocation) throws IOException {
        Gson gson = new Gson();
        IResource resource = Minecraft.getInstance().getResourceManager().getResource(resourceLocation);
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream())) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);

            SMEGModel model = new SMEGModel();
            model.bounds = gson.fromJson(json.getAsJsonArray("bounds"), double[].class);

            JsonArray facesArray = json.getAsJsonArray("faces");
            model.faces = new SMEGModel.Face[facesArray.size()];
            for (int i = 0; i < facesArray.size(); i++) {
                JsonObject faceJson = facesArray.get(i).getAsJsonObject();
                SMEGModel.Face face = new SMEGModel.Face();
                face.texture = faceJson.get("texture").getAsInt();

                JsonArray verticesArray = faceJson.getAsJsonArray("vertices");
                face.vertices = new SMEGModel.Face.Vertex[verticesArray.size()];
                for (int j = 0; j < verticesArray.size(); j++) {
                    JsonArray vertexArray = verticesArray.get(j).getAsJsonArray();
                    SMEGModel.Face.Vertex vertex = new SMEGModel.Face.Vertex();
                    vertex.x = vertexArray.get(0).getAsDouble();
                    vertex.y = vertexArray.get(1).getAsDouble();
                    vertex.z = vertexArray.get(2).getAsDouble();
                    vertex.nx = vertexArray.get(3).getAsDouble();
                    vertex.ny = vertexArray.get(4).getAsDouble();
                    vertex.nz = vertexArray.get(5).getAsDouble();
                    vertex.u = vertexArray.get(6).getAsDouble();
                    vertex.v = vertexArray.get(7).getAsDouble();
                    face.vertices[j] = vertex;
                }

                JsonArray trianglesArray = faceJson.getAsJsonArray("triangles");
                face.triangles = new int[trianglesArray.size()][3];
                for (int j = 0; j < trianglesArray.size(); j++) {
                    JsonArray triangleArray = trianglesArray.get(j).getAsJsonArray();
                    for (int k = 0; k < 3; k++) {
                        face.triangles[j][k] = triangleArray.get(k).getAsInt();
                    }
                }
                model.faces[i] = face;
            }

            JsonArray boxesArray = json.getAsJsonArray("boxes");
            model.boxes = new SMEGModel.Box[boxesArray.size()];
            for (int i = 0; i < boxesArray.size(); i++) {
                model.boxes[i] = new SMEGModel.Box(gson.fromJson(boxesArray.get(i).getAsJsonArray(), double[].class));
            }

            return model;
        }
    }

    public double[] getBounds() {
        return bounds;
    }

    public Face[] getFaces() {
        return faces;
    }

    public Box[] getBoxes() {
        return boxes;
    }
}