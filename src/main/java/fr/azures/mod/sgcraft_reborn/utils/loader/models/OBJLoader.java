/*
  ***********************************************************************************************************************************
  * Source : https://github.com/Tau-ri-Dev/JSGMod-1.18.2/blob/main/Core/src/main/java/dev/tauri/jsgcore/loader/model/OBJLoader.java *
  ***********************************************************************************************************************************
*/
package fr.azures.mod.sgcraft_reborn.utils.loader.models;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

import java.util.Objects;
import java.util.Scanner;

import org.lwjgl.opengl.GL32;

import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3f;

public class OBJLoader {
    /**
     * Renders a given <code>Obj</code> file.
     *
     * @param model the <code>Obj</code> file to be rendered
     */
    public static void render(OBJModel model) {
        //GL32.glMaterialf(GL_FRONT, GL_SHININESS, 120);
        GL32.glBegin(GL_TRIANGLES);
        {
            for (OBJModel.Face face : model.getFaces()) {
                Vector3f[] normals = {
                        model.getNormals().get(face.getNormals()[0] - 1),
                        model.getNormals().get(face.getNormals()[1] - 1),
                        model.getNormals().get(face.getNormals()[2] - 1)
                };
                Vector2f[] texCoords = {
                        model.getTextureCoordinates().get(face.getTextureCoords()[0] - 1),
                        model.getTextureCoordinates().get(face.getTextureCoords()[1] - 1),
                        model.getTextureCoordinates().get(face.getTextureCoords()[2] - 1)
                };
                Vector3f[] vertices = {
                        model.getVertices().get(face.getVertices()[0] - 1),
                        model.getVertices().get(face.getVertices()[1] - 1),
                        model.getVertices().get(face.getVertices()[2] - 1)
                };
                {
                    GL32.glNormal3f(normals[0].getX(), normals[0].getY(), normals[0].getZ());
                    GL32.glTexCoord2f(texCoords[0].x, texCoords[0].y);
                    GL32.glVertex3f(vertices[0].getX(), vertices[0].getY(), vertices[0].getZ());
                    GL32.glNormal3f(normals[1].getX(), normals[1].getY(), normals[1].getZ());
                    GL32.glTexCoord2f(texCoords[1].x, texCoords[1].y);
                    GL32.glVertex3f(vertices[1].getX(), vertices[1].getY(), vertices[1].getZ());
                    GL32.glNormal3f(normals[2].getX(), normals[2].getY(), normals[2].getZ());
                    GL32.glTexCoord2f(texCoords[2].x, texCoords[2].y);
                    GL32.glVertex3f(vertices[2].getX(), vertices[2].getY(), vertices[2].getZ());
                }
            }
        }
        GL32.glEnd();
    }

    /**
     * @return the loaded <code>Obj</code>
     */
    public static OBJModel loadModel(String modelPath, Class<?> clazz) {
        return loadModel(new Scanner(Objects.requireNonNull(clazz.getClassLoader().getResourceAsStream(modelPath))));
    }

    /**
     * @param sc the <code>Obj</code> to be loaded
     * @return the loaded <code>Obj</code>
     */
    public static OBJModel loadModel(Scanner sc) {
        OBJModel model = new OBJModel();
        while (sc.hasNextLine()) {
            String ln = sc.nextLine();
            if (!(ln == null || ln.equals("") || ln.startsWith("#"))) {
                String[] split = ln.split(" ");
                String command = split[0];
                if ("v".equals(command)) {
                    model.getVertices().add(new Vector3f(
                            Float.parseFloat(split[1]),
                            Float.parseFloat(split[2]),
                            Float.parseFloat(split[3])
                    ));
                } else if ("vn".equals(command)) {
                    model.getNormals().add(new Vector3f(
                            Float.parseFloat(split[1]),
                            Float.parseFloat(split[2]),
                            Float.parseFloat(split[3])
                    ));
                } else if ("vt".equals(command)) {
                    model.getTextureCoordinates().add(new Vector2f(
                            Float.parseFloat(split[1]),
                            Float.parseFloat(split[2])
                    ));
                } else if ("f".equals(command)) {
                    model.getFaces().add(new OBJModel.Face(
                            new int[]{
                                    Integer.parseInt(split[1].split("/")[0]),
                                    Integer.parseInt(split[2].split("/")[0]),
                                    Integer.parseInt(split[3].split("/")[0])
                            },
                            new int[]{
                                    Integer.parseInt(split[1].split("/")[1]),
                                    Integer.parseInt(split[2].split("/")[1]),
                                    Integer.parseInt(split[3].split("/")[1])
                            },
                            new int[]{
                                    Integer.parseInt(split[1].split("/")[2]),
                                    Integer.parseInt(split[2].split("/")[2]),
                                    Integer.parseInt(split[3].split("/")[2])
                            }
                    ));
                } else if ("s".equals(command)) {
                    model.setSmoothShadingEnabled(!ln.contains("off"));
                } else {
                    System.err.println("[OBJ] Unknown Line: " + ln);
                }
            }
        }
        sc.close();
        return model;
    }
}