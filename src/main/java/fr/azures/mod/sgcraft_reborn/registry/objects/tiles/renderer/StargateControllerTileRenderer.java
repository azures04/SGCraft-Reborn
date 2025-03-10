package fr.azures.mod.sgcraft_reborn.registry.objects.tiles.renderer;

import java.io.IOException;
import java.util.logging.Logger;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fr.azures.mod.sgcraft_reborn.client.model.SMEGModel;
import fr.azures.mod.sgcraft_reborn.registry.objects.blocks.StargateControllerBlock;
import fr.azures.mod.sgcraft_reborn.registry.objects.tiles.StargateControllerTile;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class StargateControllerTileRenderer extends TileEntityRenderer<StargateControllerTile> {

    private SMEGModel model;
    private static final Logger LOGGER = Logger.getLogger(StargateControllerTileRenderer.class.getName());
    private static ResourceLocation[] TEXTURES = {
    		new ResourceLocation("sgcraft_reborn", "textures/tileentity/dhd_top.png"),
    		new ResourceLocation("sgcraft_reborn", "textures/tileentity/dhd_side.png"),
    		new ResourceLocation("sgcraft_reborn", "textures/tileentity/dhd_detail.png"),
    		new ResourceLocation("sgcraft_reborn", "textures/tileentity/dhd_button_disabled.png")
    };
    
    public StargateControllerTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        try {
            model = SMEGModel.loadFromFile(new ResourceLocation("sgcraft_reborn", "models/block/dhd.smeg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(StargateControllerTile tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        if (model == null) {
            LOGGER.warning("Model is null, skipping rendering");
            return;
        }
        
        matrixStack.pushPose();
        matrixStack.translate(0.5, 0, 0.5);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(-tileEntity.getBlockState().getValue(StargateControllerBlock.FACING).toYRot()));
        
        RenderSystem.disableCull();


        for (SMEGModel.Face face : model.getFaces()) {
        	IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(TEXTURES[face.texture]));
            if (face.vertices == null || face.triangles == null) {
                LOGGER.warning("Skipping face due to missing vertices or triangles");
                continue;
            }
            for (int[] triangle : face.triangles) {
                if (triangle.length != 3) {
                    LOGGER.warning("Invalid triangle definition, skipping");
                    continue;
                }

                for (int perm = 0; perm < 9; perm++) {
                    SMEGModel.Face.Vertex v0, v1, v2;
                    switch (perm) {
                        case 0:
                            v0 = face.vertices.get(triangle[0]);
                            v1 = face.vertices.get(triangle[1]);
                            v2 = face.vertices.get(triangle[2]);
                            break;
                        case 1:
                            v0 = face.vertices.get(triangle[1]);
                            v1 = face.vertices.get(triangle[0]);
                            v2 = face.vertices.get(triangle[2]);
                            break;
                        case 2:
                            v0 = face.vertices.get(triangle[2]);
                            v1 = face.vertices.get(triangle[1]);
                            v2 = face.vertices.get(triangle[0]);
                            break;
                        case 3:
                            v0 = face.vertices.get(triangle[0]);
                            v1 = face.vertices.get(triangle[2]);
                            v2 = face.vertices.get(triangle[1]);
                            break;
                        case 4:
                            v0 = face.vertices.get(triangle[1]);
                            v1 = face.vertices.get(triangle[2]);
                            v2 = face.vertices.get(triangle[0]);
                            break;
                        case 5:
                            v0 = face.vertices.get(triangle[2]);
                            v1 = face.vertices.get(triangle[0]);
                            v2 = face.vertices.get(triangle[1]);
                            break;
                        case 6:
                            v0 = face.vertices.get(triangle[0]);
                            v1 = face.vertices.get(triangle[2]);
                            v2 = face.vertices.get(triangle[0]);
                            break;
                        case 7:
                            v0 = face.vertices.get(triangle[1]);
                            v1 = face.vertices.get(triangle[0]);
                            v2 = face.vertices.get(triangle[1]);
                            break;

                        default:
                            continue;
                    }

                    addVertex(vertexBuilder, matrixStack, v0, combinedOverlay, combinedLight);
                    addVertex(vertexBuilder, matrixStack, v1, combinedOverlay, combinedLight);
                    addVertex(vertexBuilder, matrixStack, v2, combinedOverlay, combinedLight);
                }
            }
        }
        
        RenderSystem.enableCull();
        matrixStack.popPose();
    }

    private void addVertex(IVertexBuilder vertexBuilder, MatrixStack matrixStack, SMEGModel.Face.Vertex vertex, int combinedOverlay, int combinedLight) {
        vertexBuilder.vertex(matrixStack.last().pose(), (float) vertex.x, (float) vertex.y, (float) vertex.z)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .uv((float) vertex.u, (float) vertex.v)
                .overlayCoords(combinedOverlay)
                .uv2(combinedLight)
                .normal(matrixStack.last().normal(), (float) vertex.nx, (float) vertex.ny, (float) vertex.nz)
                .endVertex();
    }
}