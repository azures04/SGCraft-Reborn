package fr.azures.mod.sgcraft_reborn.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ModConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.BooleanValue allowCraftingNaquadah;
    public static ForgeConfigSpec.BooleanValue allowCraftingCrystals;
    
    static {
        BUILDER.push("Craftings");

        allowCraftingCrystals = BUILDER.comment("Allows cystals crafting")
                .define("allowCraftingCrystals", true);

        allowCraftingNaquadah = BUILDER.comment("Allows naquadah crafting")
                .define("allowCraftingNaquadah", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
