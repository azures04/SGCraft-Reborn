package fr.azures04.sgcraftreborn.client.models.tiles;

import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.config.SGCraftRebornConfig;
import fr.azures04.sgcraftreborn.common.registries.blocks.StargateBaseBlock;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateBaseTileEntity;
import fr.azures04.sgcraftreborn.common.registries.tiles.states.StargateIrisState;
import fr.azures04.sgcraftreborn.common.registries.tiles.states.StargateVortexState;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.items.CapabilityItemHandler;
import org.lwjgl.opengl.GL13;

import java.util.Objects;

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

    @Override
    public void render(StargateBaseTileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage) {
        if (!tileEntityIn.isMerged()) return;

        IBlockState state = tileEntityIn.getWorld().getBlockState(tileEntityIn.getPos());

        if (!(state.getBlock() instanceof StargateBaseBlock)) {
            return;
        }

        EnumFacing facing = state.get(StargateBaseBlock.FACING);

        GlStateManager.pushMatrix();
        GlStateManager.translated(x + 0.5, y + 0.5, z + 0.5);

        if (facing == EnumFacing.UP) {
            GlStateManager.rotatef(90, 1, 0, 0);
        } else if (facing == EnumFacing.DOWN) {
            GlStateManager.rotatef(-90, 1, 0, 0);
        } else {
            GlStateManager.rotatef(-facing.getHorizontalAngle(), 0, 1, 0);
        }

        GlStateManager.translated(0, 2.0, 0);

        GlStateManager.enableRescaleNormal();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.bindTexture(new ResourceLocation(Constants.MOD_ID, "textures/tileentity/stargate.png"));
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        renderRing(buffer, tessellator, ringMidRadius - ringOverlap, ringOuterRadius, RingType.Outer, ringZOffset);
        renderInnerRing(tileEntityIn, buffer, tessellator, partialTicks);
        renderChevrons(tileEntityIn, buffer, tessellator);

        if (tileEntityIn.hasIrisUpgrade()) {
            renderIris(tileEntityIn, buffer, tessellator, partialTicks);
        }


        renderCamouflage(tileEntityIn);

        if (tileEntityIn.getVortexState() != StargateVortexState.IDLE && tileEntityIn.getVortexState() != StargateVortexState.DIALLING) {
            renderEventHorizon(tileEntityIn, buffer, tessellator);
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }

    private void renderCamouflage(StargateBaseTileEntity tileEntityIn) {
        tileEntityIn.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(inventory -> {
            BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();

            int combinedLight = tileEntityIn.getWorld().getCombinedLight(tileEntityIn.getPos(), 0);
            int lightX = combinedLight % 65536;
            int lightY = combinedLight / 65536;

            GlStateManager.pushMatrix();

            GlStateManager.activeTexture(GL13.GL_TEXTURE1);
            GlStateManager.enableTexture2D();
            GL13.glMultiTexCoord2f(GL13.GL_TEXTURE1, (float) lightX, (float) lightY);

            GlStateManager.activeTexture(GL13.GL_TEXTURE0);
            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            RenderHelper.enableStandardItemLighting();

            EnumFacing facing = tileEntityIn.getWorld()
                    .getBlockState(tileEntityIn.getPos())
                    .get(StargateBaseBlock.FACING);

            float rotY = facing.getHorizontalAngle();

            for (int i = 0; i < 5; i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (stack.isEmpty()) continue;
                Block block = Block.getBlockFromItem(stack.getItem());
                if (block == Blocks.AIR) continue;
                IBlockState state = block.getDefaultState();

                GlStateManager.pushMatrix();

                float offsetX = 0;
                float offsetZ = 0;
                float finalRot = rotY;

                switch (facing) {
                    case EAST:
                        offsetX = i - 2.5f;
                        offsetZ = -1.5f;
                        finalRot = rotY - 90.0f;
                        break;
                    case WEST:
                        offsetX = i - 2.5f;
                        offsetZ = 0.5f;
                        finalRot = rotY - 90.0f;
                        break;
                    case NORTH:
                        offsetX = i - 1.5f;
                        offsetZ = -0.5f;
                        finalRot = rotY - 90;
                        break;
                    case SOUTH:
                        offsetX = i - 3.5f;
                        offsetZ = -0.5f;
                        finalRot = rotY - 90.0f;
                        break;
                    default:
                        offsetX = i - 3.5f;
                        offsetZ = -0.5f;
                        break;
                }

                GlStateManager.translatef(offsetX, -2.5f, offsetZ);

                GlStateManager.translatef(0.5f, 0.5f, 0.5f);
                GlStateManager.rotatef(finalRot, 0.0f, 1.0f, 0.0f);
                GlStateManager.translatef(-0.5f, -0.5f, -0.5f);

                dispatcher.renderBlockBrightness(state, 1.0f);

                GlStateManager.popMatrix();
            }

            RenderHelper.disableStandardItemLighting();

            GlStateManager.activeTexture(GL13.GL_TEXTURE1);
            GlStateManager.enableTexture2D();
            GlStateManager.activeTexture(GL13.GL_TEXTURE0);

            GlStateManager.popMatrix();
        });
    }

    private void renderInnerRing(StargateBaseTileEntity te, BufferBuilder buffer, Tessellator tessellator, float partialTicks) {
        GlStateManager.pushMatrix();
        double currentAngle = te.getLastRingAngle() + (te.getRingAngle() - te.getLastRingAngle()) * partialTicks;
        double symbolOffset = (360.0 / 39.0) / 2.0;

        GlStateManager.rotatef((float) (currentAngle + symbolOffset), 0, 0, 1);
        renderRing(buffer, tessellator, ringInnerRadius, ringMidRadius, RingType.Inner, 0);
        GlStateManager.popMatrix();
    }

    private void renderRing(BufferBuilder buffer, Tessellator tessellator, double r1, double r2, RingType type, double dz) {
        double z = ringDepth / 2 + dz;
        double u = 0, du = 0, dv = 0;

        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);

        for (int i = 0; i < numRingSegments; i++) {
            selectTile(0x00);

            if (type == RingType.Outer) {
                setNormal((float) c[i], (float) s[i], 0);
                vertex(buffer, r2 * c[i], r2 * s[i], z, 0, 0); vertex(buffer, r2 * c[i], r2 * s[i], -z, 0, 16);
                vertex(buffer, r2 * c[i + 1], r2 * s[i + 1], -z, 16, 16); vertex(buffer, r2 * c[i + 1], r2 * s[i + 1], z, 16, 0);
            }
            if (type == RingType.Inner) {
                setNormal((float) -c[i], (float) -s[i], 0);
                vertex(buffer, r1 * c[i], r1 * s[i], -z, 0, 0); vertex(buffer, r1 * c[i], r1 * s[i], z, 0, 16);
                vertex(buffer, r1 * c[i + 1], r1 * s[i + 1], z, 16, 16); vertex(buffer, r1 * c[i + 1], r1 * s[i + 1], -z, 16, 0);
            }

            setNormal(0, 0, -1);
            vertex(buffer, r1 * c[i], r1 * s[i], -z, 0, 16); vertex(buffer, r1 * c[i + 1], r1 * s[i + 1], -z, 16, 16);
            vertex(buffer, r2 * c[i + 1], r2 * s[i + 1], -z, 16, 0); vertex(buffer, r2 * c[i], r2 * s[i], -z, 0, 0);

            setNormal(0, 0, 1);
            if (type == RingType.Outer) {
                selectTile(ringFaceTextureIndex);
                u = 0; du = 16; dv = 16;
            } else {
                selectTile(ringSymbolTextureIndex);
                u = ringSymbolTextureLength - (i + 1) * ringSymbolSegmentWidth;
                du = ringSymbolSegmentWidth; dv = ringSymbolTextureHeight;
            }
            vertex(buffer, r1 * c[i], r1 * s[i], z, u + du, dv); vertex(buffer, r2 * c[i], r2 * s[i], z, u + du, 0);
            vertex(buffer, r2 * c[i + 1], r2 * s[i + 1], z, u, 0); vertex(buffer, r1 * c[i + 1], r1 * s[i + 1], z, u, dv);
        }
        tessellator.draw();
    }

    private void renderChevrons(StargateBaseTileEntity te, BufferBuilder buffer, Tessellator tessellator) {
        int numChevrons = te.hasChevronUpgrade() ? 9 : 7;
        int i0 = numChevrons > 7 ? 0 : 1;
        int k = te.getDialledAddress().length() > 7 ? 1 : 0;

        float angle = te.getChevronAngle();

        for (int i = i0; i < i0 + numChevrons; i++) {
            int j = chevronEngagementSequences[k][i];
            boolean engaged = te.getNumEngagedChevrons() > j;

            GlStateManager.pushMatrix();
            GlStateManager.rotatef(90 - (i - 4) * angle, 0, 0, 1);
            renderChevron(buffer, tessellator, engaged);
            GlStateManager.popMatrix();
        }
    }

    private void renderChevron(BufferBuilder buffer, Tessellator tessellator, boolean engaged) {
        double r1 = chevronInnerRadius, r2 = chevronOuterRadius;
        double z2 = ringDepth / 2, z1 = z2 + chevronDepth;
        double w1 = chevronBorderWidth, w2 = w1 * 1.25;
        double y1 = chevronWidth / 4;
        double y2 = chevronWidth / 2;

        if (engaged) GlStateManager.translated(-chevronMotionDistance, 0, 0);

        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
        selectTile(chevronTextureIndex);
        setNormal(0, 0, 1);
        vertex(buffer, r2, y2, z1, 0, 2); vertex(buffer, r1, y1, z1, 0, 16); vertex(buffer, r1 + w1, y1 - w1, z1, 4, 12); vertex(buffer, r2, y2 - w2, z1, 4, 2);
        vertex(buffer, r2, y2, z1, 0, 0); vertex(buffer, r2, y2, z2, 0, 4); vertex(buffer, r1, y1, z2, 16, 4); vertex(buffer, r1, y1, z1, 16, 0);
        vertex(buffer, r2, y2, z1, 16, 0); vertex(buffer, r2, y2 - w2, z1, 12, 0); vertex(buffer, r2, y2 - w2, z2, 12, 4); vertex(buffer, r2, y2, z2, 16, 4);
        vertex(buffer, r1 + w1, y1 - w1, z1, 4, 12); vertex(buffer, r1, y1, z1, 0, 16); vertex(buffer, r1, -y1, z1, 16, 16); vertex(buffer, r1 + w1, -y1 + w1, z1, 12, 12);
        vertex(buffer, r1, y1, z1, 0, 0); vertex(buffer, r1, y1, z2, 0, 4); vertex(buffer, r1, -y1, z2, 16, 4); vertex(buffer, r1, -y1, z1, 16, 0);
        vertex(buffer, r2, -y2 + w2, z1, 12, 0); vertex(buffer, r1 + w1, -y1 + w1, z1, 12, 12); vertex(buffer, r1, -y1, z1, 16, 16); vertex(buffer, r2, -y2, z1, 16, 0);
        vertex(buffer, r1, -y1, z1, 0, 0); vertex(buffer, r1, -y1, z2, 0, 4); vertex(buffer, r2, -y2, z2, 16, 4); vertex(buffer, r2, -y2, z1, 16, 0);
        vertex(buffer, r2, -y2, z1, 0, 0); vertex(buffer, r2, -y2, z2, 0, 4); vertex(buffer, r2, -y2 + w2, z2, 4, 4); vertex(buffer, r2, -y2 + w2, z1, 4, 0);
        vertex(buffer, r2, -y2, z2, 0, 0); vertex(buffer, r1, -y1, z2, 0, 16); vertex(buffer, r1, y1, z2, 16, 16); vertex(buffer, r2, y2, z2, 16, 0);
        tessellator.draw();

        selectTile(chevronLitTextureIndex);
        if (!engaged) GlStateManager.color4f(0.5f, 0.5f, 0.5f, 1.0f);
        else GlStateManager.disableLighting();

        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
        setNormal(0, 0, 1);
        vertex(buffer, r2, y2 - w2, z1, 0, 4); vertex(buffer, r1 + w1, y1 - w1, z1, 4, 16); vertex(buffer, r1 + w1, 0, z1, 8, 16); vertex(buffer, r2, 0, z1, 8, 4);
        vertex(buffer, r2, 0, z1, 8, 4); vertex(buffer, r1 + w1, 0, z1, 8, 16); vertex(buffer, r1 + w1, -y1 + w1, z1, 12, 16); vertex(buffer, r2, -y2 + w2, z1, 16, 4);
        vertex(buffer, r2, y2 - w2, z2, 0, 0); vertex(buffer, r2, y2 - w2, z1, 0, 4); vertex(buffer, r2, -y2 + w2, z1, 16, 4); vertex(buffer, r2, -y2 + w2, z2, 16, 0);
        tessellator.draw();

        GlStateManager.color4f(1f, 1f, 1f, 1f);
        GlStateManager.enableLighting();
    }
    private void renderEventHorizon(StargateBaseTileEntity te, BufferBuilder buffer, Tessellator tessellator) {
        GlStateManager.pushMatrix();
        this.bindTexture(new ResourceLocation(Constants.MOD_ID, "textures/tileentity/eventhorizon.png"));
        GlStateManager.disableCull();
        GlStateManager.disableLighting();


        boolean useTransparency = fr.azures04.sgcraftreborn.common.config.SGCraftRebornConfig.TRANSPARENCY.get();
        if (useTransparency) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }

        double rclip = 2.5;
        if (te.getIrisState() != StargateIrisState.OPEN) {
            double phase = te.getLastIrisPhase() + (te.getIrisPhase() - te.getLastIrisPhase()) * Minecraft.getInstance().getRenderPartialTicks();
            rclip = 2.5 * (phase / 60.0);
        }

        double[][] grid = te.getEventHorizonGrid()[0];

        for (int i = 1; i < ehGridRadialSize; i++) {
            buffer.begin(8, DefaultVertexFormats.POSITION_TEX_NORMAL);
            for (int j = 0; j <= ehGridPolarSize; j++) {
                ehVertex(buffer, grid, i, j, rclip);
                ehVertex(buffer, grid, i + 1, j, rclip);
            }
            tessellator.draw();
        }

        buffer.begin(6, DefaultVertexFormats.POSITION_TEX_NORMAL);

        buffer.pos(0, 0, ehClip(grid[1][0] * 0.09, 0, rclip)).tex(0, 0).normal(0, 0, 1).endVertex();
        for (int j = 0; j <= ehGridPolarSize; j++) {
            ehVertex(buffer, grid, 1, j, rclip);
        }
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        if (useTransparency) {
            GlStateManager.disableBlend();
        }

        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    private void renderIris(StargateBaseTileEntity te, BufferBuilder buffer, Tessellator tessellator, float partialTicks) {
        this.bindTexture(new ResourceLocation(Constants.MOD_ID, "textures/tileentity/iris.png"));

        double phase = te.getLastIrisPhase() + (te.getIrisPhase() - te.getLastIrisPhase()) * partialTicks;
        double aperture = phase / 60.0;
        double a = 0.8 * aperture;

        for (int i = 0; i < numIrisBlades; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.rotatef((float) (360.0 * i / numIrisBlades), 0, 0, 1);
            renderIrisBlade(buffer, tessellator, a);
            GlStateManager.popMatrix();
        }
    }

    private void renderIrisBlade(BufferBuilder buffer, Tessellator tessellator, double a) {
        double aa = a * 60, r = 2.31, w1 = 2.40, w2 = 1.85, h0 = 0.16, h1 = 1.00;
        double h = h1 - (h1 - h0) * a * a;
        double u = w2 / w1, v = h / h1, v0 = h0 / h1;
        double z0 = 0.1, z1 = 0.01;

        GlStateManager.pushMatrix();
        GlStateManager.translated(r, 0, 0);
        GlStateManager.rotatef((float) -aa, 0, 0, 1);

        buffer.begin(6, DefaultVertexFormats.POSITION_TEX_NORMAL);
        setNormal(0, 0, 1);
        vertexUV(buffer, -w1, 0, z0, 0, 0);
        vertexUV(buffer, 0, 0, z0 + z1, 1, 0);
        vertexUV(buffer, 0, h0, z0 + z1, 1, v0);
        vertexUV(buffer, -w1 + w2, h, z0, u, v);
        vertexUV(buffer, -w1, h, z0, 0, v);
        tessellator.draw();

        buffer.begin(6, DefaultVertexFormats.POSITION_TEX_NORMAL);
        vertexUV(buffer, -w1, 0, z0, 0, 0);
        vertexUV(buffer, -w1, h, z0, 0, v);
        vertexUV(buffer, -w1 + w2, h, z0, u, v);
        vertexUV(buffer, 0, h0, z0 - z1, 1, v0);
        vertexUV(buffer, 0, 0, z0 - z1, 1, 0);
        tessellator.draw();

        GlStateManager.popMatrix();
    }

    private void ehVertex(BufferBuilder buffer, double[][] grid, int i, int j, double rclip) {
        double r = i * ehBandWidth;
        double x = r * c[j];
        double y = r * s[j];

        double visualZ = grid[j][i] * 0.15;


        double z = ehClip(visualZ, r, rclip);

        buffer.pos(x, y, z).tex(x, y).normal(0, 0, 1).endVertex();
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

    private void vertex(BufferBuilder buffer, double x, double y, double z, double u, double v) {
        buffer.pos(x, y, z).tex(u0 + u * textureScaleU, v0 + v * textureScaleV).normal(nX, nY, nZ).endVertex();
    }

    private void vertexUV(BufferBuilder buffer, double x, double y, double z, double u, double v) {
        buffer.pos(x, y, z).tex(u, v).normal(nX, nY, nZ).endVertex();
    }
}