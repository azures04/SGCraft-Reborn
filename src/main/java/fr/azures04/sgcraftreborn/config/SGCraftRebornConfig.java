package fr.azures04.sgcraftreborn.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class SGCraftRebornConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Options
    public static ForgeConfigSpec.BooleanValue ALLOW_CRAFTING_CRYSTALS;
    public static ForgeConfigSpec.BooleanValue ALLOW_CRAFTING_NAQUADAH;
    public static ForgeConfigSpec.BooleanValue ENABLE_NAQUADAH_ORE;
    public static ForgeConfigSpec.BooleanValue ADD_ORES_TO_EXISTING_WORLDS;

    //Config
    public static ForgeConfigSpec.IntValue GEN_ISOLATED_ODDS;
    public static ForgeConfigSpec.IntValue MAX_ISOLATED_NODES;
    public static ForgeConfigSpec.IntValue GEN_UNDER_LAVA_ODDS;
    public static ForgeConfigSpec.IntValue MAX_NODES_UNDER_LAVA;

    static {
        BUILDER.push("options");
            ALLOW_CRAFTING_CRYSTALS = BUILDER
                .comment("Enables recipes for crafting core and controller crystals from vanilla items.")
                .define("allowCraftingCrystals", false);
            ALLOW_CRAFTING_NAQUADAH = BUILDER
                .comment("Enables a recipe for crafting naquadah from vanilla items. Defaults to false.")
                .define("allowCraftingNaquadah", false);
            ENABLE_NAQUADAH_ORE = BUILDER
                .comment("Enables generating naquadah ore in the world.")
                .define("enableNaquadahOre", true);
            ADD_ORES_TO_EXISTING_WORLDS = BUILDER
                .comment("If true, naquadah ore will be added to previously-generated chunks.\n" +
                "Otherwise, only new worlds. or chunks generated after SGCraft is installed, will contain naquadah ore.")
                .define("addOresToExistingWorlds", true);
        BUILDER.pop();

        BUILDER.push("naquadah");
            GEN_ISOLATED_ODDS = BUILDER
                .comment("Reciprocal probability of isolated naquadah ore being generated in a chunk. Lower numbers give more ore.")
                .defineInRange("genIsolatedOdds", 8, 1, 100);
            MAX_ISOLATED_NODES = BUILDER
                .comment("Maximum number of isolated naquadah ore clusters per chunk. Higher numbers give more ore.")
                .defineInRange("maxIsolatedNodes", 4, 1, 64);
            GEN_UNDER_LAVA_ODDS = BUILDER
                .comment("Reciprocal probability of naquadah being generated under lava in a chunk. Lower numbers give more ore.")
                .defineInRange("genUnderLavaOdds", 4, 1, 64);
            MAX_NODES_UNDER_LAVA = BUILDER
                .comment("Maximum number of naquadah ore clusters under lava per chunk. Higher numbers give more ore.")
                .defineInRange("maxNodesUnderLava", 4, 1, 6);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

}
