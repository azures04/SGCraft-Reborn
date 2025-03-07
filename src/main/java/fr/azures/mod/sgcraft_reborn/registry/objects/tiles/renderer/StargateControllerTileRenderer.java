package fr.azures.mod.sgcraft_reborn.registry.objects.tiles.renderer;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import fr.azures.mod.sgcraft_reborn.registry.objects.blocks.StargateControllerBlock;
import fr.azures.mod.sgcraft_reborn.registry.objects.tiles.StargateControllerTile;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StargateControllerTileRenderer extends TileEntityRenderer<StargateControllerTile> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("modid", "textures/block/special_block.png");
    private static final Logger LOGGER = Logger.getLogger(StargateControllerTileRenderer.class.getName());

    private BaseModel model;

    public StargateControllerTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        model = BaseModel.fromResource(new ResourceLocation("modid", "models/special_model.smeg"));
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

        Trans3 t = new Trans3(matrixStack.last().pose());
        IRenderTarget renderTarget = new VertexBuilderRenderTarget(vertexBuilder, combinedOverlay, combinedLight);

        model.render(t, renderTarget, new Texture(TEXTURE));

        RenderSystem.enableCull(); // Re-enable culling
        matrixStack.popPose();
    }

    private static class VertexBuilderRenderTarget implements IRenderTarget {
        private final IVertexBuilder vertexBuilder;
        private final int combinedOverlay;
        private final int combinedLight;

        public VertexBuilderRenderTarget(IVertexBuilder vertexBuilder, int combinedOverlay, int combinedLight) {
            this.vertexBuilder = vertexBuilder;
            this.combinedOverlay = combinedOverlay;
            this.combinedLight = combinedLight;
        }

        @Override
        public void setTexture(ITexture texture) {
            // Handle setting texture if needed
        }

        @Override
        public void beginTriangle() {
            // No need to handle beginTriangle for IVertexBuilder
        }

        @Override
        public void setNormal(Vector3 normal) {
            vertexBuilder.normal((float) normal.x, (float) normal.y, (float) normal.z);
        }

        @Override
        public void addVertex(Vector3 position, double u, double v) {
            vertexBuilder.vertex((float) position.x, (float) position.y, (float) position.z)
                    .color(1.0f, 1.0f, 1.0f, 1.0f)
                    .uv((float) u, (float) v)
                    .overlayCoords(combinedOverlay)
                    .uv2(combinedLight)
                    .endVertex();
        }

        @Override
        public void endFace() {
            // No need to handle endFace for IVertexBuilder
        }
    }

    private static class Texture implements ITexture {
        private final ResourceLocation texture;

        public Texture(ResourceLocation texture) {
            this.texture = texture;
        }

        @Override
        public ResourceLocation getTexture() {
            return texture;
        }
    }

    // Stub classes to replace gcewing.sg classes
    private static class BaseModel {
        public double[] bounds;
        public Face[] faces;
        public double[][] boxes;

        public static class Face {
            int texture;
            double[][] vertices;
            int[][] triangles;
            Vector3 normal;
        }

        static BaseModel fromResource(ResourceLocation location) {
            // Implement loading logic here or mock it
            return new BaseModel();
        }

        public AxisAlignedBB getBounds() {
            return new AxisAlignedBB(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
        }

        void prepare() {
            for (Face face : faces) {
                double[][] p = face.vertices;
                int[] t = face.triangles[0];
                face.normal = Vector3.unit(Vector3.sub(p[t[1]], p[t[0]]).cross(Vector3.sub(p[t[2]], p[t[0]])));
            }
        }

        public void render(Trans3 t, IRenderTarget renderer, ITexture... textures) {
            Vector3 p = null, n = null;
            for (Face face : faces) {
                ITexture tex = textures[face.texture];
                if (tex != null) {
                    renderer.setTexture(tex);
                    for (int[] tri : face.triangles) {
                        renderer.beginTriangle();
                        for (int i = 0; i < 3; i++) {
                            int j = tri[i];
                            double[] c = face.vertices[j];
                            p = t.p(c[0], c[1], c[2]);
                            n = t.v(c[3], c[4], c[5]);
                            renderer.setNormal(n);
                            renderer.addVertex(p, c[6], c[7]);
                        }
                        renderer.endFace();
                    }
                }
            }
        }
    }

    private static class Trans3 {
        private final Matrix4f matrix;

        public Trans3(Matrix4f matrix) {
            this.matrix = matrix;
        }

        public Vector3 p(double x, double y, double z) {
            // Implement the transformation logic
            return new Vector3(x, y, z);
        }

        public Vector3 v(double x, double y, double z) {
            // Implement the transformation logic
            return new Vector3(x, y, z);
        }

        public void addBox(double x1, double y1, double z1, double x2, double y2, double z2, List list) {
            // Implement the logic to add a box to the list
        }
    }

    private static class Vector3 {
        public double x, y, z;

        public Vector3(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public static Vector3 sub(Vector3 a, Vector3 b) {
            return new Vector3(a.x - b.x, a.y - b.y, a.z - b.z);
        }

        public static Vector3 cross(Vector3 a, Vector3 b) {
            return new Vector3(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
        }

        public static Vector3 unit(Vector3 v) {
            double length = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
            return new Vector3(v.x / length, v.y / length, v.z / length);
        }
    }

    private interface IRenderTarget {
        void setTexture(ITexture texture);

        void beginTriangle();

        void setNormal(Vector3 normal);

        void addVertex(Vector3 position, double u, double v);

        void endFace();
    }

    private interface ITexture {
        ResourceLocation getTexture();
    }
}