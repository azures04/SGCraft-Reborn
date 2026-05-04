package fr.azures04.sgcraftreborn.common.config.conditions;

import com.google.gson.JsonObject;
import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.config.SGCraftRebornConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class CraftingConditions implements ICondition {

    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "config");
    private final String key;

    public CraftingConditions(String key) {
        this.key = key;
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test() {
        switch (key) {
            case "allowCraftingCrystals":
                return SGCraftRebornConfig.ALLOW_CRAFTING_CRYSTALS.get();
            case "allowCraftingNaquadah":
                return SGCraftRebornConfig.ALLOW_CRAFTING_NAQUADAH.get();
            default:
                return false;
        }
    }

    public static class Serializer implements IConditionSerializer<CraftingConditions> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, CraftingConditions value) {
            json.addProperty("key", value.key);
        }

        @Override
        public CraftingConditions read(JsonObject json) {
            return new CraftingConditions(json.get("key").getAsString());
        }

        @Override
        public ResourceLocation getID() {
            return CraftingConditions.ID;
        }
    }
}