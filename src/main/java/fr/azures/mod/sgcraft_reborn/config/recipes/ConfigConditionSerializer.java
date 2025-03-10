package fr.azures.mod.sgcraft_reborn.config.recipes;

import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class ConfigConditionSerializer implements IConditionSerializer<ConfigRecipesConditions> {
    @Override
    public void write(JsonObject json, ConfigRecipesConditions value) {
        json.addProperty("configKey", value.configKey);
    }

    @Override
    public ConfigRecipesConditions read(JsonObject json) {
        return new ConfigRecipesConditions(json.get("configKey").getAsString());
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation("sgcraft_reborn", "optionals_recipes");
    }
}