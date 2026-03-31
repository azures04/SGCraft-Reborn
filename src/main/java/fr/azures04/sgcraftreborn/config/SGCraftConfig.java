package fr.azures04.sgcraftreborn.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class SGCraftConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.BooleanValue ALLOW_CRAFTING_CRYSTALS;
    public static ForgeConfigSpec.BooleanValue ALLOW_CRAFTING_NAQUADAH;

    static {
        BUILDER.push("options");

        ALLOW_CRAFTING_CRYSTALS = BUILDER
            .comment("Enables recipes for crafting core and controller crystals from vanilla items.")
            .define("allowCraftingCrystals", false);
        ALLOW_CRAFTING_NAQUADAH = BUILDER
            .comment("Enables a recipe for crafting naquadah from vanilla items. Defaults to false.")
            .define("allowCraftingNaquadah", false);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

}
