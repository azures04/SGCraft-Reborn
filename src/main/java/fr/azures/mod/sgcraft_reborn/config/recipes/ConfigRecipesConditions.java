package fr.azures.mod.sgcraft_reborn.config.recipes;

import fr.azures.mod.sgcraft_reborn.config.ModConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;

public class ConfigRecipesConditions implements ICondition {
    private static final ResourceLocation NAME = new ResourceLocation("modid", "config_recipe");
    public final String configKey;

    public ConfigRecipesConditions(String configKey) {
        this.configKey = configKey;
    }

    @Override
    public ResourceLocation getID() {
        return NAME;
    }

    @Override
    public boolean test() {
        switch (configKey) {
            case "enableCustomRecipe1":
                return ModConfig.allowCraftingCrystals.get();
            case "enableCustomRecipe2":
                return ModConfig.allowCraftingNaquadah.get();
            default:
                return false;
        }
    }
}
