package fr.azures.mod.sgcraft_reborn.client.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SMEGModel {

    private static final Logger LOGGER = LogManager.getLogger();

    private double[] bounds;
    private List<Face> faces = new ArrayList<>();
    private List<Box> boxes = new ArrayList<>();

    public static class Face {
        public int texture;
        public List<Vertex> vertices = new ArrayList<>();
        public List<int[]> triangles = new ArrayList<>(); // Use int[] instead of int[][]

        public static class Vertex {
            public double x, y, z;
            public double nx, ny, nz;
            public double u, v;

            @Override
            public String toString() {
                return String.format("Vertex{x=%.2f, y=%.2f, z=%.2f, u=%.2f, v=%.2f}", x, y, z, u, v);
            }
        }

        @Override
        public String toString() {
            return String.format("Face{texture=%d, vertices=%d, triangles=%d}", texture, vertices.size(), triangles.size());
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
        IResource resource;

        try {
            resource = Minecraft.getInstance().getResourceManager().getResource(resourceLocation);
        } catch (IOException e) {
            LOGGER.error("Could not load resource: {}", resourceLocation, e);
            throw e;
        }

        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream())) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            SMEGModel model = new SMEGModel();

            // Load bounds
            JsonArray boundsArray = json.getAsJsonArray("bounds");
            if (boundsArray != null && boundsArray.size() == 6) {
                model.bounds = gson.fromJson(boundsArray, double[].class);
                LOGGER.debug("Loaded bounds: {}", (Object) model.bounds); // Cast to Object to avoid varargs
            } else {
                LOGGER.warn("Bounds are missing or invalid in {}", resourceLocation);
            }

            // Load faces
            JsonArray facesArray = json.getAsJsonArray("faces");
            if (facesArray != null) {
                for (int i = 0; i < facesArray.size(); i++) {
                    JsonObject faceJson = facesArray.get(i).getAsJsonObject();
                    Face face = new Face();
                    face.texture = faceJson.get("texture").getAsInt();

                    // Load vertices
                    JsonArray verticesArray = faceJson.getAsJsonArray("vertices");
                    if (verticesArray != null) {
                        for (int j = 0; j < verticesArray.size(); j++) {
                            JsonArray vertexArray = verticesArray.get(j).getAsJsonArray();
                            if (vertexArray != null && vertexArray.size() == 8) {
                                Face.Vertex vertex = new Face.Vertex();
                                vertex.x = vertexArray.get(0).getAsDouble();
                                vertex.y = vertexArray.get(1).getAsDouble();
                                vertex.z = vertexArray.get(2).getAsDouble();
                                vertex.nx = vertexArray.get(3).getAsDouble();
                                vertex.ny = vertexArray.get(4).getAsDouble();
                                vertex.nz = vertexArray.get(5).getAsDouble();
                                vertex.u = vertexArray.get(6).getAsDouble();
                                vertex.v = vertexArray.get(7).getAsDouble();
                                face.vertices.add(vertex);
                            } else {
                                LOGGER.warn("Invalid vertex format in face {} of {}", i, resourceLocation);
                            }
                        }
                    } else {
                        LOGGER.warn("Vertices are missing in face {} of {}", i, resourceLocation);
                    }

                    // Load triangles
                    JsonArray trianglesArray = faceJson.getAsJsonArray("triangles");
                    if (trianglesArray != null) {
                        for (int j = 0; j < trianglesArray.size(); j++) {
                            JsonArray triangleArray = trianglesArray.get(j).getAsJsonArray();
                            if (triangleArray != null && triangleArray.size() == 3) {
                                int[] triangle = new int[3];
                                for (int k = 0; k < 3; k++) {
                                    triangle[k] = triangleArray.get(k).getAsInt();
                                }
                                face.triangles.add(triangle);
                            } else {
                                LOGGER.warn("Invalid triangle format in face {} of {}", i, resourceLocation);
                            }
                        }
                    } else {
                        LOGGER.warn("Triangles are missing in face {} of {}", i, resourceLocation);
                    }

                    model.faces.add(face);
                }
            } else {
                LOGGER.warn("Faces are missing in {}", resourceLocation);
            }

            // Load boxes
            JsonArray boxesArray = json.getAsJsonArray("boxes");
            if (boxesArray != null) {
                for (int i = 0; i < boxesArray.size(); i++) {
                    JsonArray boxArray = boxesArray.get(i).getAsJsonArray();
                    if (boxArray != null && boxArray.size() == 6) {
                        double[] boxBounds = gson.fromJson(boxArray, double[].class);
                        model.boxes.add(new Box(boxBounds));
                    } else {
                        LOGGER.warn("Invalid box format in {}", resourceLocation);
                    }
                }
            } else {
                LOGGER.warn("Boxes are missing in {}", resourceLocation);
            }

            LOGGER.info("Loaded model from {}: {} faces, {} boxes", resourceLocation, model.faces.size(), model.boxes.size());
            return model;

        } catch (Exception e) {
            LOGGER.error("Error loading model from {}", resourceLocation, e);
            return null; // Or throw an exception if you prefer
        }
    }

    public double[] getBounds() {
        return bounds;
    }

    public List<Face> getFaces() {
        return faces;
    }

    public List<Box> getBoxes() {
        return boxes;
    }
}