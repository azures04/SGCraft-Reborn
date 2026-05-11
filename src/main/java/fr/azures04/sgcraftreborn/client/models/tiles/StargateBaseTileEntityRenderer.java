package fr.azures04.sgcraftreborn.client.models.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.registries.blocks.StargateBaseBlock;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateBaseTileEntity;
import fr.azures04.sgcraftreborn.common.registries.tiles.states.StargateIrisState;
import fr.azures04.sgcraftreborn.common.registries.tiles.states.StargateVortexState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.CapabilityItemHandler;
import org.lwjgl.opengl.GL11;


public class StargateBaseTileEntityRenderer extends TileEntityRenderer<StargateBaseTileEntity> {

    final static int numRingSegments = 32;
    final static double ringInnerRadius = 2.0;
    final static double ringMidRadius = 2.25;
    final static double ringOuterRadius = 2.5;
    final static double ringDepth = 0.5;
    final static double ringOverlap = 1 / 64.0;
    final static double ringZOffset = 0.0001;
    final static double chevronInnerRadius = 2.25;
    final static double chevronOuterRadius = ringOuterRadius + 1 / 16.0;
    final static double chevronWidth = (chevronOuterRadius - chevronInnerRadius) * 1.5;
    final static double chevronDepth = 0.125;
    final static double chevronBorderWidth = chevronWidth / 6;
    final static double chevronMotionDistance = 1 / 8.0;

    final static int textureTilesWide = 32;
    final static int textureTilesHigh = 2;
    final static double textureScaleU = 1.0 / (textureTilesWide * 16);
    final static double textureScaleV = 1.0 / (textureTilesHigh * 16);

    final static int ringFaceTextureIndex = 0x01;
    final static int ringSymbolTextureIndex = 0x20;
    final static int chevronTextureIndex = 0x03;
    final static int chevronLitTextureIndex = 0x02;

    final static double ringSymbolTextureLength = 512.0;
    final static double ringSymbolTextureHeight = 16.0;
    final static double ringSymbolSegmentWidth = ringSymbolTextureLength / numRingSegments;

    final static int ehGridRadialSize = 5;
    final static int ehGridPolarSize = numRingSegments;
    final static double ehBandWidth = ringInnerRadius / ehGridRadialSize;

    final static double numIrisBlades = 12;

    static int[][] chevronEngagementSequences = {
            {9, 3, 4, 5, 6, 0, 1, 2, 9},
            {7, 3, 4, 5, 8, 0, 1, 2, 6}
    };

    static double[] s = new double[numRingSegments + 1];
    static double[] c = new double[numRingSegments + 1];

    static {
        for (int i = 0; i <= numRingSegments; i++) {
            double a = 2 * Math.PI * i / numRingSegments;
            s[i] = Math.sin(a);
            c[i] = Math.cos(a);
        }
    }

    private double u0, v0;
    private float nX, nY, nZ;

    private enum RingType {
        Inner,
        Outer
    }

    private static final ResourceLocation CHEVRON_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/tileentity/stargate.png");
    private static final ResourceLocation CHEVRON_LIT_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/tileentity/stargate_lit.png");

    public StargateBaseTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }


    @Override
    public void render(StargateBaseTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (!tileEntityIn.isMerged()) return;

        BlockState state = tileEntityIn.getBlockState();

        if (!(state.getBlock() instanceof StargateBaseBlock)) {
            return;
        }

        Direction facing = state.get(StargateBaseBlock.FACING);

        matrixStackIn.push();
        matrixStackIn.translate(0.5, 0.5, 0.5);

        if (facing == Direction.UP) {
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90));
        } else if (facing == Direction.DOWN) {
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90));
        } else {
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-facing.getHorizontalAngle()));
        }

        matrixStackIn.translate(0.0, 2.0, 0.0);

        ResourceLocation baseTexture = new ResourceLocation(Constants.MOD_ID, "textures/tileentity/stargate.png");
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntityCutout(baseTexture));

        renderRing(matrixStackIn, builder, ringMidRadius - ringOverlap, ringOuterRadius, RingType.Outer, ringZOffset, combinedLightIn, combinedOverlayIn);
        renderInnerRing(tileEntityIn, matrixStackIn, builder, partialTicks, combinedLightIn, combinedOverlayIn);
        renderChevrons(tileEntityIn, matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        if (tileEntityIn.hasIrisUpgrade()) {
            renderIris(tileEntityIn, matrixStackIn, bufferIn, partialTicks, combinedLightIn, combinedOverlayIn);
        }

        if (tileEntityIn.getVortexState() != StargateVortexState.IDLE && tileEntityIn.getVortexState() != StargateVortexState.DIALLING) {
            renderEventHorizon(tileEntityIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        }

        renderCamouflage(tileEntityIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        matrixStackIn.pop();
    }

    private void renderCamouflage(StargateBaseTileEntity tileEntityIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        tileEntityIn.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(inventory -> {
            BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();

            Direction facing = tileEntityIn.getBlockState().get(StargateBaseBlock.FACING);
            float rotY = facing.getHorizontalAngle();

            matrixStackIn.push();

            for (int i = 0; i < 5; i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (stack.isEmpty()) continue;
                Block block = Block.getBlockFromItem(stack.getItem());
                if (block == Blocks.AIR) continue;
                BlockState state = block.getDefaultState();

                matrixStackIn.push();

                float offsetX = 0;
                float offsetZ = 0;
                float finalRot = rotY;

                switch (facing) {
                    case EAST:
                        offsetX = i - 2.5f;
                        offsetZ = -0.5f;
                        finalRot = rotY - 90.0f;
                        break;
                    case WEST:
                        offsetX = i - 2.5f;
                        offsetZ = -0.5f;
                        finalRot = rotY - 90.0f;
                        break;
                    case NORTH:
                        offsetX = i - 2.5f;
                        offsetZ = -0.5f;
                        finalRot = rotY - 90;
                        break;
                    case SOUTH:
                        offsetX = i - 2.5f;
                        offsetZ = -0.5f;
                        finalRot = rotY - 90.0f;
                        break;
                    default:
                        offsetX = i - 3.5f;
                        offsetZ = -0.5f;
                        break;
                }

                matrixStackIn.translate(offsetX, -2.5f, offsetZ);

                matrixStackIn.translate(0.5f, 0.5f, 0.5f);
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(finalRot));
                matrixStackIn.translate(-0.5f, -0.5f, -0.5f);

                dispatcher.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, net.minecraftforge.client.model.data.EmptyModelData.INSTANCE);

                matrixStackIn.pop();
            }

            matrixStackIn.pop();
        });
    }

    private void renderInnerRing(StargateBaseTileEntity te, MatrixStack matrixStack, IVertexBuilder builder, float partialTicks, int combinedLight, int combinedOverlay) {
        matrixStack.push();
        double currentAngle = te.getLastRingAngle() + (te.getRingAngle() - te.getLastRingAngle()) * partialTicks;
        double symbolOffset = (360.0 / 39.0) / 2.0;
        matrixStack.rotate(Vector3f.ZP.rotationDegrees((float) (currentAngle + symbolOffset)));
        renderRing(matrixStack, builder, ringInnerRadius, ringMidRadius, RingType.Inner, 0, combinedLight, combinedOverlay);
        matrixStack.pop();
    }

    private void renderRing(MatrixStack matrixStack, IVertexBuilder builder, double r1, double r2, RingType type, double dz, int combinedLight, int combinedOverlay) {
        double z = ringDepth / 2 + dz;
        double u = 0, du = 0, dv = 0;

        Matrix4f matrix = matrixStack.getLast().getMatrix();
        Matrix3f normalMatrix = matrixStack.getLast().getNormal();

        for (int i = 0; i < numRingSegments; i++) {
            selectTile(0x00);

            if (type == RingType.Outer) {
                setNormal((float) c[i], (float) s[i], 0);
                vertex(builder, matrix, normalMatrix, r2 * c[i], r2 * s[i], z, 0, 0, combinedLight, combinedOverlay);
                vertex(builder, matrix, normalMatrix, r2 * c[i], r2 * s[i], -z, 0, 16, combinedLight, combinedOverlay);

                setNormal((float) c[i + 1], (float) s[i + 1], 0);
                vertex(builder, matrix, normalMatrix, r2 * c[i + 1], r2 * s[i + 1], -z, 16, 16, combinedLight, combinedOverlay);
                vertex(builder, matrix, normalMatrix, r2 * c[i + 1], r2 * s[i + 1], z, 16, 0, combinedLight, combinedOverlay);
            }

            if (type == RingType.Inner) {
                setNormal((float) -c[i], (float) -s[i], 0);
                vertex(builder, matrix, normalMatrix, r1 * c[i], r1 * s[i], -z, 0, 0, combinedLight, combinedOverlay);
                vertex(builder, matrix, normalMatrix, r1 * c[i], r1 * s[i], z, 0, 16, combinedLight, combinedOverlay);

                setNormal((float) -c[i + 1], (float) -s[i + 1], 0);
                vertex(builder, matrix, normalMatrix, r1 * c[i + 1], r1 * s[i + 1], z, 16, 16, combinedLight, combinedOverlay);
                vertex(builder, matrix, normalMatrix, r1 * c[i + 1], r1 * s[i + 1], -z, 16, 0, combinedLight, combinedOverlay);
            }

            setNormal(0, 0, -1);
            vertex(builder, matrix, normalMatrix, r1 * c[i], r1 * s[i], -z, 0, 16, combinedLight, combinedOverlay);
            vertex(builder, matrix, normalMatrix, r1 * c[i + 1], r1 * s[i + 1], -z, 16, 16, combinedLight, combinedOverlay);
            vertex(builder, matrix, normalMatrix, r2 * c[i + 1], r2 * s[i + 1], -z, 16, 0, combinedLight, combinedOverlay);
            vertex(builder, matrix, normalMatrix, r2 * c[i], r2 * s[i], -z, 0, 0, combinedLight, combinedOverlay);

            setNormal(0, 0, 1);
            if (type == RingType.Outer) {
                selectTile(ringFaceTextureIndex);
                u = 0; du = 16; dv = 16;
            } else {
                selectTile(ringSymbolTextureIndex);
                u = ringSymbolTextureLength - (i + 1) * ringSymbolSegmentWidth;
                du = ringSymbolSegmentWidth; dv = ringSymbolTextureHeight;
            }

            vertex(builder, matrix, normalMatrix, r1 * c[i], r1 * s[i], z, u + du, dv, combinedLight, combinedOverlay);
            vertex(builder, matrix, normalMatrix, r2 * c[i], r2 * s[i], z, u + du, 0, combinedLight, combinedOverlay);
            vertex(builder, matrix, normalMatrix, r2 * c[i + 1], r2 * s[i + 1], z, u, 0, combinedLight, combinedOverlay);
            vertex(builder, matrix, normalMatrix, r1 * c[i + 1], r1 * s[i + 1], z, u, dv, combinedLight, combinedOverlay);
        }
    }

    private void renderChevrons(StargateBaseTileEntity te, MatrixStack matrixStack, IVertexBuilder builder, int combinedLight, int combinedOverlay) {
        int numChevrons = te.hasChevronUpgrade() ? 9 : 7;
        int i0 = numChevrons > 7 ? 0 : 1;
        int k = te.getDialledAddress().length() > 7 ? 1 : 0;

        float angle = te.getChevronAngle();

        for (int i = i0; i < i0 + numChevrons; i++) {
            int j = chevronEngagementSequences[k][i];
            boolean engaged = te.getNumEngagedChevrons() > j;

            matrixStack.push();
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(90 - (i - 4) * angle));

            renderChevron(matrixStack, builder, engaged, combinedLight, combinedOverlay);

            matrixStack.pop();
        }
    }

    private void renderChevron(MatrixStack matrixStack, IVertexBuilder builder, boolean engaged, int combinedLight, int combinedOverlay) {
        double r1 = chevronInnerRadius, r2 = chevronOuterRadius;
        double z2 = ringDepth / 2, z1 = z2 + chevronDepth;
        double w1 = chevronBorderWidth, w2 = w1 * 1.25;
        double y1 = chevronWidth / 4;
        double y2 = chevronWidth / 2;

        matrixStack.push();

        if (engaged) {
            matrixStack.translate(-chevronMotionDistance, 0, 0);
        }

        Matrix4f matrix = matrixStack.getLast().getMatrix();
        Matrix3f normalMatrix = matrixStack.getLast().getNormal();

        selectTile(chevronTextureIndex);
        setNormal(0, 0, 1);

        vertex(builder, matrix, normalMatrix, r2, y2, z1, 0, 2, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r1, y1, z1, 0, 16, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r1 + w1, y1 - w1, z1, 4, 12, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r2, y2 - w2, z1, 4, 2, 255, 255, 255, combinedLight, combinedOverlay);

        vertex(builder, matrix, normalMatrix, r2, y2, z1, 0, 0, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r2, y2, z2, 0, 4, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r1, y1, z2, 16, 4, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r1, y1, z1, 16, 0, 255, 255, 255, combinedLight, combinedOverlay);

        vertex(builder, matrix, normalMatrix, r2, y2, z1, 16, 0, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r2, y2 - w2, z1, 12, 0, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r2, y2 - w2, z2, 12, 4, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r2, y2, z2, 16, 4, 255, 255, 255, combinedLight, combinedOverlay);

        vertex(builder, matrix, normalMatrix, r1 + w1, y1 - w1, z1, 4, 12, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r1, y1, z1, 0, 16, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r1, -y1, z1, 16, 16, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r1 + w1, -y1 + w1, z1, 12, 12, 255, 255, 255, combinedLight, combinedOverlay);

        vertex(builder, matrix, normalMatrix, r1, y1, z1, 0, 0, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r1, y1, z2, 0, 4, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r1, -y1, z2, 16, 4, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r1, -y1, z1, 16, 0, 255, 255, 255, combinedLight, combinedOverlay);

        vertex(builder, matrix, normalMatrix, r2, -y2 + w2, z1, 12, 0, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r1 + w1, -y1 + w1, z1, 12, 12, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r1, -y1, z1, 16, 16, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r2, -y2, z1, 16, 0, 255, 255, 255, combinedLight, combinedOverlay);

        vertex(builder, matrix, normalMatrix, r1, -y1, z1, 0, 0, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r1, -y1, z2, 0, 4, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r2, -y2, z2, 16, 4, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r2, -y2, z1, 16, 0, 255, 255, 255, combinedLight, combinedOverlay);

        vertex(builder, matrix, normalMatrix, r2, -y2, z1, 0, 0, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r2, -y2, z2, 0, 4, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r2, -y2 + w2, z2, 4, 4, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r2, -y2 + w2, z1, 4, 0, 255, 255, 255, combinedLight, combinedOverlay);

        vertex(builder, matrix, normalMatrix, r2, -y2, z2, 0, 0, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r1, -y1, z2, 0, 16, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r1, y1, z2, 16, 16, 255, 255, 255, combinedLight, combinedOverlay);
        vertex(builder, matrix, normalMatrix, r2, y2, z2, 16, 0, 255, 255, 255, combinedLight, combinedOverlay);

        selectTile(chevronLitTextureIndex);

        int colorRGB;
        int actualLight;
        double litZ = z1 + 0.002;

        if (!engaged) {
            colorRGB = 160;
            actualLight = combinedLight;
            setNormal(0, 0, 1);

            vertex(builder, matrix, normalMatrix, r2, y2 - w2, litZ, 0, 4, colorRGB, colorRGB, colorRGB, actualLight, combinedOverlay);
            vertex(builder, matrix, normalMatrix, r1 + w1, y1 - w1, litZ, 4, 16, colorRGB, colorRGB, colorRGB, actualLight, combinedOverlay);
            vertex(builder, matrix, normalMatrix, r1 + w1, 0, litZ, 8, 16, colorRGB, colorRGB, colorRGB, actualLight, combinedOverlay);
            vertex(builder, matrix, normalMatrix, r2, 0, litZ, 8, 4, colorRGB, colorRGB, colorRGB, actualLight, combinedOverlay);

            vertex(builder, matrix, normalMatrix, r2, 0, litZ, 8, 4, colorRGB, colorRGB, colorRGB, actualLight, combinedOverlay);
            vertex(builder, matrix, normalMatrix, r1 + w1, 0, litZ, 8, 16, colorRGB, colorRGB, colorRGB, actualLight, combinedOverlay);
            vertex(builder, matrix, normalMatrix, r1 + w1, -y1 + w1, litZ, 12, 16, colorRGB, colorRGB, colorRGB, actualLight, combinedOverlay);
            vertex(builder, matrix, normalMatrix, r2, -y2 + w2, litZ, 16, 4, colorRGB, colorRGB, colorRGB, actualLight, combinedOverlay);

            vertex(builder, matrix, normalMatrix, r2, y2 - w2, z2, 0, 0, colorRGB, colorRGB, colorRGB, actualLight, combinedOverlay);
            vertex(builder, matrix, normalMatrix, r2, y2 - w2, litZ, 0, 4, colorRGB, colorRGB, colorRGB, actualLight, combinedOverlay);
            vertex(builder, matrix, normalMatrix, r2, -y2 + w2, litZ, 16, 4, colorRGB, colorRGB, colorRGB, actualLight, combinedOverlay);
            vertex(builder, matrix, normalMatrix, r2, -y2 + w2, z2, 16, 0, colorRGB, colorRGB, colorRGB, actualLight, combinedOverlay);
        } else {
            colorRGB = 255;
            actualLight = 15728880;

            litGlowVertex(builder, matrix, r2, y2 - w2, litZ, 0, 4, colorRGB, actualLight, combinedOverlay);
            litGlowVertex(builder, matrix, r1 + w1, y1 - w1, litZ, 4, 16, colorRGB, actualLight, combinedOverlay);
            litGlowVertex(builder, matrix, r1 + w1, 0, litZ, 8, 16, colorRGB, actualLight, combinedOverlay);
            litGlowVertex(builder, matrix, r2, 0, litZ, 8, 4, colorRGB, actualLight, combinedOverlay);

            litGlowVertex(builder, matrix, r2, 0, litZ, 8, 4, colorRGB, actualLight, combinedOverlay);
            litGlowVertex(builder, matrix, r1 + w1, 0, litZ, 8, 16, colorRGB, actualLight, combinedOverlay);
            litGlowVertex(builder, matrix, r1 + w1, -y1 + w1, litZ, 12, 16, colorRGB, actualLight, combinedOverlay);
            litGlowVertex(builder, matrix, r2, -y2 + w2, litZ, 16, 4, colorRGB, actualLight, combinedOverlay);

            litGlowVertex(builder, matrix, r2, y2 - w2, z2, 0, 0, colorRGB, actualLight, combinedOverlay);
            litGlowVertex(builder, matrix, r2, y2 - w2, litZ, 0, 4, colorRGB, actualLight, combinedOverlay);
            litGlowVertex(builder, matrix, r2, -y2 + w2, litZ, 16, 4, colorRGB, actualLight, combinedOverlay);
            litGlowVertex(builder, matrix, r2, -y2 + w2, z2, 16, 0, colorRGB, actualLight, combinedOverlay);
        }

        matrixStack.pop();
    }

    private void litGlowVertex(IVertexBuilder builder, Matrix4f matrix, double x, double y, double z, double u, double v, int rgb, int light, int overlay) {
        builder.pos(matrix, (float) x, (float) y, (float) z)
                .color(rgb, rgb, rgb, 255)
                .tex((float) (u0 + u * textureScaleU), (float) (v0 + v * textureScaleV))
                .overlay(overlay)
                .lightmap(light)
                .normal(0, 1, 0)
                .endVertex();
    }

    private void renderEventHorizon(StargateBaseTileEntity te, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.push();

        ResourceLocation texture = new ResourceLocation(Constants.MOD_ID, "textures/tileentity/eventhorizon.png");

        boolean useTransparency = fr.azures04.sgcraftreborn.common.config.SGCraftRebornConfig.TRANSPARENCY.get();
        RenderType renderType = useTransparency ? RenderType.getEntityTranslucent(texture) : RenderType.getEntitySolid(texture);
        IVertexBuilder builder = bufferIn.getBuffer(renderType);

        Minecraft.getInstance().getTextureManager().bindTexture(texture);
        RenderSystem.recordRenderCall(() -> {
            Texture texObj = Minecraft.getInstance().getTextureManager().getTexture(texture);
            if (texObj != null) {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, texObj.getGlTextureId());
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
            }
        });

        Matrix4f matrix = matrixStackIn.getLast().getMatrix();
        Matrix3f normalMatrix = matrixStackIn.getLast().getNormal();

        int vortexLight = 15728880;

        double rclip = 2.5;
        if (te.getIrisState() != StargateIrisState.OPEN) {
            double phase = te.getLastIrisPhase() + (te.getIrisPhase() - te.getLastIrisPhase()) * Minecraft.getInstance().getRenderPartialTicks();
            rclip = 2.5 * (phase / 60.0);
        }

        double[][] grid = te.getEventHorizonGrid()[0];

        for (int i = 1; i < ehGridRadialSize; i++) {
            for (int j = 0; j < ehGridPolarSize; j++) {
                ehVertex(builder, matrix, normalMatrix, grid, i, j, rclip, vortexLight, combinedOverlayIn);
                ehVertex(builder, matrix, normalMatrix, grid, i + 1, j, rclip, vortexLight, combinedOverlayIn);
                ehVertex(builder, matrix, normalMatrix, grid, i + 1, j + 1, rclip, vortexLight, combinedOverlayIn);
                ehVertex(builder, matrix, normalMatrix, grid, i, j + 1, rclip, vortexLight, combinedOverlayIn);
            }
        }

        double centerZ = ehClip(grid[1][0] * 0.09, 0, rclip);

        for (int j = 0; j < ehGridPolarSize; j++) {
            builder.pos(matrix, 0, 0, (float) centerZ)
                    .color(200, 225, 255, 255)
                    .tex(0.5f, 0.5f)
                    .overlay(combinedOverlayIn)
                    .lightmap(vortexLight)
                    .normal(normalMatrix, 0, 0, 1)
                    .endVertex();

            ehVertex(builder, matrix, normalMatrix, grid, 1, j, rclip, vortexLight, combinedOverlayIn);
            ehVertex(builder, matrix, normalMatrix, grid, 1, j + 1, rclip, vortexLight, combinedOverlayIn);
            ehVertex(builder, matrix, normalMatrix, grid, 1, j + 1, rclip, vortexLight, combinedOverlayIn);
        }

        matrixStackIn.pop();
    }

    private void renderIrisBlade(MatrixStack matrixStack, IVertexBuilder builder, double a, int combinedLight, int combinedOverlay) {
        double aa = a * 60, r = 2.31, w1 = 2.40, w2 = 1.85, h0 = 0.16, h1 = 1.00;
        double h = h1 - (h1 - h0) * a * a;
        double u = w2 / w1, v = h / h1, v0 = h0 / h1;
        double z0 = 0.1, z1 = 0.01;

        matrixStack.push();
        matrixStack.translate(r, 0, 0);
        matrixStack.rotate(Vector3f.ZP.rotationDegrees((float) -aa));

        Matrix4f matrix = matrixStack.getLast().getMatrix();
        Matrix3f normalMatrix = matrixStack.getLast().getNormal();

        bladeVertex(builder, matrix, normalMatrix, -w1, 0, z0, 0, 0, 0, 0, 1, combinedLight, combinedOverlay);
        bladeVertex(builder, matrix, normalMatrix, 0, 0, z0 + z1, 1, 0, 0, 0, 1, combinedLight, combinedOverlay);
        bladeVertex(builder, matrix, normalMatrix, 0, h0, z0 + z1, 1, v0, 0, 0, 1, combinedLight, combinedOverlay);
        bladeVertex(builder, matrix, normalMatrix, 0, h0, z0 + z1, 1, v0, 0, 0, 1, combinedLight, combinedOverlay);

        bladeVertex(builder, matrix, normalMatrix, -w1, 0, z0, 0, 0, 0, 0, 1, combinedLight, combinedOverlay);
        bladeVertex(builder, matrix, normalMatrix, 0, h0, z0 + z1, 1, v0, 0, 0, 1, combinedLight, combinedOverlay);
        bladeVertex(builder, matrix, normalMatrix, -w1 + w2, h, z0, u, v, 0, 0, 1, combinedLight, combinedOverlay);
        bladeVertex(builder, matrix, normalMatrix, -w1 + w2, h, z0, u, v, 0, 0, 1, combinedLight, combinedOverlay);

        bladeVertex(builder, matrix, normalMatrix, -w1, 0, z0, 0, 0, 0, 0, 1, combinedLight, combinedOverlay);
        bladeVertex(builder, matrix, normalMatrix, -w1 + w2, h, z0, u, v, 0, 0, 1, combinedLight, combinedOverlay);
        bladeVertex(builder, matrix, normalMatrix, -w1, h, z0, 0, v, 0, 0, 1, combinedLight, combinedOverlay);
        bladeVertex(builder, matrix, normalMatrix, -w1, h, z0, 0, v, 0, 0, 1, combinedLight, combinedOverlay);

        bladeVertex(builder, matrix, normalMatrix, -w1, 0, z0, 0, 0, 0, 0, -1, combinedLight, combinedOverlay);
        bladeVertex(builder, matrix, normalMatrix, -w1, h, z0, 0, v, 0, 0, -1, combinedLight, combinedOverlay);
        bladeVertex(builder, matrix, normalMatrix, -w1 + w2, h, z0, u, v, 0, 0, -1, combinedLight, combinedOverlay);
        bladeVertex(builder, matrix, normalMatrix, -w1 + w2, h, z0, u, v, 0, 0, -1, combinedLight, combinedOverlay);

        bladeVertex(builder, matrix, normalMatrix, -w1, 0, z0, 0, 0, 0, 0, -1, combinedLight, combinedOverlay);
        bladeVertex(builder, matrix, normalMatrix, -w1 + w2, h, z0, u, v, 0, 0, -1, combinedLight, combinedOverlay);
        bladeVertex(builder, matrix, normalMatrix, 0, h0, z0 - z1, 1, v0, 0, 0, -1, combinedLight, combinedOverlay);
        bladeVertex(builder, matrix, normalMatrix, 0, h0, z0 - z1, 1, v0, 0, 0, -1, combinedLight, combinedOverlay);

        bladeVertex(builder, matrix, normalMatrix, -w1, 0, z0, 0, 0, 0, 0, -1, combinedLight, combinedOverlay);
        bladeVertex(builder, matrix, normalMatrix, 0, h0, z0 - z1, 1, v0, 0, 0, -1, combinedLight, combinedOverlay);
        bladeVertex(builder, matrix, normalMatrix, 0, 0, z0 - z1, 1, 0, 0, 0, -1, combinedLight, combinedOverlay);
        bladeVertex(builder, matrix, normalMatrix, 0, 0, z0 - z1, 1, 0, 0, 0, -1, combinedLight, combinedOverlay);

        matrixStack.pop();
    }

    private void bladeVertex(IVertexBuilder builder, Matrix4f matrix, Matrix3f normalMatrix, double x, double y, double z, double u, double v, float nx, float ny, float nz, int light, int overlay) {
        builder.pos(matrix, (float) x, (float) y, (float) z)
            .color(255, 255, 255, 255)
            .tex((float) u, (float) v)
            .overlay(overlay)
            .lightmap(light)
            .normal(normalMatrix, nx, ny, nz)
            .endVertex();
    }

    private void renderIris(StargateBaseTileEntity te, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, float partialTicks, int combinedLightIn, int combinedOverlayIn) {
        ResourceLocation irisTexture = new ResourceLocation(Constants.MOD_ID, "textures/tileentity/iris.png");

        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntityCutout(irisTexture));

        double phase = te.getLastIrisPhase() + (te.getIrisPhase() - te.getLastIrisPhase()) * partialTicks;
        double aperture = phase / 60.0;
        double a = 0.8 * aperture;

        for (int i = 0; i < numIrisBlades; i++) {
            matrixStackIn.push();
            matrixStackIn.rotate(Vector3f.ZP.rotationDegrees((float) (360.0 * i / numIrisBlades)));
            renderIrisBlade(matrixStackIn, builder, a, combinedLightIn, combinedOverlayIn);
            matrixStackIn.pop();
        }
    }

    private void ehVertex(IVertexBuilder builder, Matrix4f matrix, Matrix3f normalMatrix, double[][] grid, int i, int j, double rclip, int light, int overlay) {
        double r = i * ehBandWidth;
        double x = r * c[j];
        double y = r * s[j];
        double visualZ = grid[j][i] * 0.15;
        double z = ehClip(visualZ, r, rclip);

        float tiling = 3.0f;
        float maxRadius = (float) (ehGridRadialSize * ehBandWidth);
        float u = (float) (0.5 + (x / (maxRadius * 2.0)) * tiling);
        float v = (float) (0.5 + (y / (maxRadius * 2.0)) * tiling);

        builder.pos(matrix, (float) x, (float) y, (float) z)
            .color(200, 225, 255, 255)
            .tex(u, v)
            .overlay(overlay)
            .lightmap(light)
            .normal(normalMatrix, 0, 0, 1)
            .endVertex();
    }

    private double ehClip(double z, double r, double rclip) {
        if (r >= rclip) {
            return Math.min(z, 0);
        }
        return z;
    }

    private void selectTile(int index) {
        u0 = (index % textureTilesWide) * (textureScaleU * 16);
        v0 = ((double) index / textureTilesWide) * (textureScaleV * 16);
    }

    private void setNormal(float x, float y, float z) {
        this.nX = x; this.nY = y; this.nZ = z;
    }

    private void vertex(IVertexBuilder builder, Matrix4f matrix, Matrix3f normalMatrix, double x, double y, double z, double u, double v, int light, int overlay) {
        float finalU = (float) (u0 + u * textureScaleU);
        float finalV = (float) (v0 + v * textureScaleV);

        builder.pos(matrix, (float) x, (float) y, (float) z)
            .color(255, 255, 255, 255)
            .tex(finalU, finalV)
            .overlay(overlay)
            .lightmap(light)
            .normal(normalMatrix, nX, nY, nZ)
            .endVertex();
    }

    private void vertex(IVertexBuilder builder, Matrix4f matrix, Matrix3f normalMatrix, double x, double y, double z, double u, double v, int r, int g, int b, int light, int overlay) {
        float finalU = (float) (u0 + u * textureScaleU);
        float finalV = (float) (v0 + v * textureScaleV);

        builder.pos(matrix, (float) x, (float) y, (float) z)
            .color(r, g, b, 255)
            .tex(finalU, finalV)
            .overlay(overlay)
            .lightmap(light)
            .normal(normalMatrix, nX, nY, nZ)
            .endVertex();
    }
}