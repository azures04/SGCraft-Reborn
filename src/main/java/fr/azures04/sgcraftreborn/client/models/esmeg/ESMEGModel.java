package fr.azures04.sgcraftreborn.client.models.esmeg;

import fr.azures04.sgcraftreborn.exceptions.MalformedSMEGException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.state.IProperty;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.HashMap;
import java.util.Map;

public class ESMEGModel {

    public String[] textures;
    public Map<String, Map<String, String>> overrides;
    public double[] bounds;
    public double[][] boxes;
    public Face[] faces;

    public static class Face {
        public int texture;
        public double[][] vertices;
        public int[][] triangles;
    }

    public AxisAlignedBB getBounds() {
        if (bounds.length != 6) throw new MalformedSMEGException("Malformed texture : [bounds] size must be 6");
        return new AxisAlignedBB(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
    }

    public AxisAlignedBB[] getBoxes() {
        if (boxes.length == 0) {
            return new AxisAlignedBB[] {
                new AxisAlignedBB(0, 0, 0, 1, 1, 1)
            };
        }
        AxisAlignedBB[] result = new AxisAlignedBB[boxes.length];
        for (int i = 0; i < boxes.length; i++) {
            result[i] = new AxisAlignedBB(boxes[i][0], boxes[i][1], boxes[i][2], boxes[i][3], boxes[i][4], boxes[i][5]);
        }
        return result;
    }

    public ResourceLocation[] getTextures() {
        ResourceLocation[] result = new ResourceLocation[textures.length];
        for (int i = 0; i < textures.length; i++) {
            result[i] = new ResourceLocation(textures[i]);
        }
        return result;
    }

    public ResourceLocation getTexture(int index) {
        if (textures == null || index >= textures.length || textures[index] == null) {
            return new ResourceLocation("minecraft", "missingno");
        }
        return new ResourceLocation(textures[index]);
    }

    public ResourceLocation getTexture(int index, Map<String, String> currentState) {
        for (Map.Entry<String, Map<String, String>> override : overrides.entrySet()) {
            if (matchesState(override.getKey(), currentState)) {
                Map<String, String> overrideTextures = override.getValue();
                String indexStr = String.valueOf(index);
                if (overrideTextures.containsKey(indexStr)) {
                    return new ResourceLocation(overrideTextures.get(indexStr));
                }
            }
        }
        return getTexture(index);
    }

    private boolean matchesState(String condition, Map<String, String> currentState) {
        for (String part : condition.split(",")) {
            String[] kv = part.split("=");
            String key = kv[0].trim();
            String value = kv[1].trim();
            if (!currentState.getOrDefault(key, "").equals(value)) {
                return false;
            }
        }
        return true;
    }

    public static class BlockStateParser {
        public static Map<String, String> fromBlockState(IBlockState state) {
            Map<String, String> result = new HashMap<>();
            for (Map.Entry<IProperty<?>, Comparable<?>> entry : state.getValues().entrySet()) {
                String valueName = entry.getValue() instanceof IStringSerializable ?
                        ((IStringSerializable) entry.getValue()).getName() :
                        entry.getValue().toString().toLowerCase();

                result.put(entry.getKey().getName(), valueName);
            }
            return result;
        }
    }
}
