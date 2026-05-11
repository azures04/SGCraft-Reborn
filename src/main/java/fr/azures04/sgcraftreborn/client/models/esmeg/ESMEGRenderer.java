package fr.azures04.sgcraftreborn.client.models.esmeg;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture; // IMPORTANT
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import java.util.Map;

public class ESMEGRenderer {

    public static void render(ESMEGModel model, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, Map<String, String> state) {
        Matrix4f matrix = matrixStack.getLast().getMatrix();
        Matrix3f normalMatrix = matrixStack.getLast().getNormal();

        for (int i = 0; i < model.faces.length; i++) {
            ESMEGModel.Face face = model.faces[i];
            ResourceLocation texLoc = model.getTexture(face.texture, state);
            IVertexBuilder builder = buffer.getBuffer(RenderType.getEntityCutout(texLoc));

            for (int j = 0; j < face.triangles.length; j++) {
                int[] triangle = face.triangles[j];

                for (int k = 0; k < 4; k++) {
                    int vertexIndex = triangle[Math.min(k, 2)];
                    double[] v = face.vertices[vertexIndex];

                    Vector3f norm = new Vector3f((float) v[3], (float) v[4], (float) v[5]);
                    norm.transform(normalMatrix);

                    builder.pos(matrix, (float) v[0], (float) v[1], (float) v[2])
                        .color(255, 255, 255, 255)
                        .tex((float) v[6], (float) v[7])
                        .overlay(OverlayTexture.NO_OVERLAY)
                        .lightmap(combinedLight)
                        .normal(norm.getX(), norm.getY(), norm.getZ())
                        .endVertex();
                }
            }
        }
    }

    public static void renderWithRotation(ESMEGModel model, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, Direction facing) {
        renderWithRotation(model, matrixStack, buffer, combinedLight, combinedOverlay, facing, new java.util.HashMap<>());
    }

    public static void renderWithRotation(ESMEGModel model, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, Direction facing, Map<String, String> state) {
        matrixStack.push();
        matrixStack.translate(0.5, 0.0, 0.5);
        matrixStack.rotate(Vector3f.YP.rotationDegrees(getAngleFromFacing(facing)));

        render(model, matrixStack, buffer, combinedLight, combinedOverlay, state);
        matrixStack.pop();
    }

    private static float getAngleFromFacing(Direction facing) {
        switch (facing) {
            case NORTH:
                return 0f;
            case WEST:
                return 90f;
            case SOUTH:
                return 180f;
            case EAST:
                return 270f;
            default:
                return 0f;
        }
    }
}