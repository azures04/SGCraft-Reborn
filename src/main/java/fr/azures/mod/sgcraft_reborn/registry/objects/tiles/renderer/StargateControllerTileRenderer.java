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

    private static final ResourceLocation TEXTURE = new ResourceLocation("sgcraft_reborn", "textures/block/dhd_side.png");
    private SMEGModel model;
    private static final Logger LOGGER = Logger.getLogger(StargateControllerTileRenderer.class.getName());
    
    
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
            matrixStack.translate(0.5, 0, 0.5); // Center the model
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(-tileEntity.getBlockState().getValue(StargateControllerBlock.FACING).toYRot()));
            
            RenderSystem.disableCull(); // Disable culling for debugging

            IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));

            for (SMEGModel.Face face : model.getFaces()) {
                if (face.vertices == null || face.triangles == null) {
                    LOGGER.warning("Skipping face due to missing vertices or triangles");
                    continue;
                }
                for (int[] triangle : face.triangles) {
                    if (triangle.length != 3) {
                        LOGGER.warning("Invalid triangle definition, skipping");
                        continue;
                    }
                    for (int vertexIndex : triangle) {
                        if (vertexIndex < 0 || vertexIndex >= face.vertices.length) {
                            LOGGER.warning("Invalid vertex index: " + vertexIndex);
                            continue;
                        }
                        SMEGModel.Face.Vertex vertex = face.vertices[vertexIndex];
                        vertexBuilder.vertex(matrixStack.last().pose(), (float) vertex.x, (float) vertex.y, (float) vertex.z)
                                .color(1.0f, 1.0f, 1.0f, 1.0f)
                                .uv((float) vertex.u, (float) vertex.v)
                                .overlayCoords(combinedOverlay)
                                .uv2(combinedLight)
                                .normal(matrixStack.last().normal(), (float) vertex.nx, (float) vertex.ny, (float) vertex.nz)
                                .endVertex();
                    }
                }
            }
            
            RenderSystem.enableCull(); // Re-enable culling
            matrixStack.popPose();
    }

}