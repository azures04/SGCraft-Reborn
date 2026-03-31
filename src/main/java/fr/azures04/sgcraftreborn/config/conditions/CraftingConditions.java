package fr.azures04.sgcraftreborn.config.conditions;

import com.google.gson.JsonObject;
import fr.azures04.sgcraftreborn.Constants;
import fr.azures04.sgcraftreborn.config.SGCraftRebornConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IConditionSerializer;

import java.util.function.BooleanSupplier;

public class CraftingConditions implements IConditionSerializer {

    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "config");

    @Override
    public BooleanSupplier parse(JsonObject json) {
        String key = json.get("key").getAsString();
        switch (key) {
            case "allowCraftingCrystals":
                return () -> SGCraftRebornConfig.ALLOW_CRAFTING_CRYSTALS.get();
            case "allowCraftingNaquadah":
                return () -> SGCraftRebornConfig.ALLOW_CRAFTING_NAQUADAH.get();
            default:
                return () -> false;
        }
    }
}