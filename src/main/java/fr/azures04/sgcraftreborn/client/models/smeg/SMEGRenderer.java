package fr.azures04.sgcraftreborn.client.models.smeg;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class SMEGRenderer {

    public static void render(SMEGModel model, double x, double y, double z) {
        render(model, x, y, z, new HashMap<>());
    }

    public static void render(SMEGModel model, double x, double y, double z, Map<String, String> state) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        for (int i = 0; i < model.faces.length; i++) {
            buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
            SMEGModel.Face face = model.faces[i];
            Minecraft.getInstance().getTextureManager().bindTexture(model.getTexture(face.texture, state));
            for (int j = 0; j < face.triangles.length; j++) {
                int[] triangle = face.triangles[j];
                for (int k = 0; k < 3; k++) {
                    int vertexIndex = triangle[k];
                    double[] vertex = face.vertices[vertexIndex];
                    buf
                        .pos((x + vertex[0]) + 0.5, y + vertex[1], (z + vertex[2]) + 0.5)
                        .tex(vertex[6], vertex[7])
                        .normal((float) vertex[3], (float) vertex[4], (float) vertex[5])
                    .endVertex();
                }
            }
            tess.draw();
        }
    }

    public static void renderWithRotation(SMEGModel model, double x, double y, double z, EnumFacing facing) {
        renderWithRotation(model, x, y, z, facing, new HashMap<>());
    }

    public static void renderWithRotation(SMEGModel model, double x, double y, double z, EnumFacing facing, Map<String, String> state) {
        GlStateManager.pushMatrix();
        GlStateManager.translated(x + 0.5, y + 0.5, z + 0.5);
        GlStateManager.rotatef(getAngleFromFacing(facing), 0, 1, 0);
        render(model, -0.5, -0.5, -0.5, state);
        GlStateManager.popMatrix();
    }
    private static float getAngleFromFacing(EnumFacing facing) {
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
