package fr.azures.mod.sgcraft_reborn.client.model;

import net.minecraft.util.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class SMEGGeometry {

    public static List<int[]> triangulateFace(int[][] polygons, SMEGModel.Face.Vertex[] vertices) {
        List<int[]> triangles = new ArrayList<>();
        for (int[] polygon : polygons) {
            if (polygon.length < 3) {
                continue;
            }

            for (int i = 1; i < polygon.length - 1; i++) {
                int[] triangle = new int[]{polygon[0], polygon[i], polygon[i + 1]};

                if (triangle[0] != triangle[i] && triangle[0] != triangle[i + 1] && triangle[i] != triangle[i + 1]) {
                    triangles.add(triangle);
                }
            }
        }
        return triangles;
    }

    public static Vector3f calculateNormal(SMEGModel.Face.Vertex v0, SMEGModel.Face.Vertex v1, SMEGModel.Face.Vertex v2) {
        float ux = (float) (v1.x - v0.x);
        float uy = (float) (v1.y - v0.y);
        float uz = (float) (v1.z - v0.z);
        float vx = (float) (v2.x - v0.x);
        float vy = (float) (v2.y - v0.y);
        float vz = (float) (v2.z - v0.z);

        Vector3f normal = new Vector3f(
                uy * vz - uz * vy,
                uz * vx - ux * vz,
                ux * vy - uy * vx
        );
        normal.normalize();
        return normal;
    }
}
