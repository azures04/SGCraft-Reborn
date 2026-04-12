package fr.azures04.sgcraftreborn.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class SGCraftRebornConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Options
    public static ForgeConfigSpec.BooleanValue ADD_ORES_TO_EXISTING_WORLDS;
    public static ForgeConfigSpec.BooleanValue ENABLE_NAQUADAH_ORE;
    public static ForgeConfigSpec.BooleanValue ALLOW_CRAFTING_CRYSTALS;
    public static ForgeConfigSpec.BooleanValue ALLOW_CRAFTING_NAQUADAH;
    public static ForgeConfigSpec.BooleanValue AUGMENT_STRUCTURES;
    public static ForgeConfigSpec.IntValue STRUCTURE_AUGMENTATION_CHANCE;
    public static ForgeConfigSpec.IntValue CHUNK_LOADING_RANGE;
    public static ForgeConfigSpec.BooleanValue LOG_STARGATE_EVENTS;

    //Naquadah
    public static ForgeConfigSpec.IntValue GEN_ISOLATED_ODDS;
    public static ForgeConfigSpec.IntValue MAX_ISOLATED_NODES;
    public static ForgeConfigSpec.IntValue GEN_UNDER_LAVA_ODDS;
    public static ForgeConfigSpec.IntValue MAX_NODES_UNDER_LAVA;

    //DHD
    public static ForgeConfigSpec.IntValue LINK_RANGE_X;
    public static ForgeConfigSpec.IntValue LINK_RANGE_Y;
    public static ForgeConfigSpec.IntValue LINK_RANGE_Z;

    //Stargate
    public static ForgeConfigSpec.BooleanValue CLOSE_FROM_EITHER_END;
    public static ForgeConfigSpec.BooleanValue ONE_WAY_TRAVEL;
    public static ForgeConfigSpec.IntValue SECONDS_TO_STAY_OPEN;
    public static ForgeConfigSpec.IntValue GATE_OPENINGS_PER_FUEL_ITEM;
    public static ForgeConfigSpec.IntValue MINUTES_OPEN_PER_FUEL_ITEM;
    public static ForgeConfigSpec.DoubleValue DISTANCE_FACTOR_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue INTER_DIMENSION_MULTIPLIER;
    public static ForgeConfigSpec.IntValue ENERGY_PER_FUEL_ITEM;
    public static ForgeConfigSpec.IntValue MAX_ENERGY_BUFFER;
    public static ForgeConfigSpec.IntValue EXPLOSION_RADIUS;
    public static ForgeConfigSpec.BooleanValue EXPLOSION_FLAME;
    public static ForgeConfigSpec.BooleanValue EXPLOSION_SMOKE;
    public static ForgeConfigSpec.BooleanValue TRANSPARENCY;
    public static ForgeConfigSpec.DoubleValue SOUND_VOLUME;
    public static ForgeConfigSpec.BooleanValue VARIABLE_CHEVRON_POSITIONS;

    //Iris
    public static ForgeConfigSpec.BooleanValue PRESERVE_INVENTORY;

    static {
        BUILDER.push("options");
            ADD_ORES_TO_EXISTING_WORLDS = BUILDER
                .comment("If true, naquadah ore will be added to previously-generated chunks.\n" +
                        "Otherwise, only new worlds. or chunks generated after SGCraft is installed, will contain naquadah ore.")
                .define("addOresToExistingWorlds", true);
            ENABLE_NAQUADAH_ORE = BUILDER
                .comment("Enables generating naquadah ore in the world.")
                .define("enableNaquadahOre", true);
            ALLOW_CRAFTING_CRYSTALS = BUILDER
                .comment("Enables recipes for crafting core and controller crystals from vanilla items.")
                .define("allowCraftingCrystals", false);
            ALLOW_CRAFTING_NAQUADAH = BUILDER
                .comment("Enables a recipe for crafting naquadah from vanilla items. Defaults to false.")
                .define("allowCraftingNaquadah", false);
            AUGMENT_STRUCTURES = BUILDER
                .comment("If true, stargates will be found assocated with certain structures in the world. Use with caution -- may be incompatible with other world generation mods.")
                .define("augmentStructures", false);
            STRUCTURE_AUGMENTATION_CHANCE = BUILDER
                .comment("Percent probability that a stargate will be associated with an eligible structure. Only effective if augmentStructures is true.")
                .defineInRange("structureAugmentationChance", 25, 0, 100);
            CHUNK_LOADING_RANGE = BUILDER
                .comment("Number of additional chunks surrounding a stargate to keep loaded. A value of 0 keeps just the chunk containing the gate loaded. A value of 1 keeps a 3x3 area loaded, etc.")
                .defineInRange("chunkLoadingRange", 1, 0, 3);
            LOG_STARGATE_EVENTS = BUILDER
                .comment("Write a message to the server log each time a stargate is merged or unmerged.")
                .define("logStargateEvents", false);
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

        BUILDER.push("dhd");
            LINK_RANGE_X = BUILDER
                .comment("Maximum distance between a stargate and its associated controller block.")
                .defineInRange("linkRangeX", 5, 1, 15);
            LINK_RANGE_Y = BUILDER
                .comment("Maximum distance between a stargate and its associated controller block.")
                .defineInRange("linkRangeY", 1, 1, 15);
            LINK_RANGE_Z = BUILDER
                .comment("Maximum distance between a stargate and its associated controller block.")
                .defineInRange("linkRangeZ", 6, 1, 15);
        BUILDER.pop();

        BUILDER.push("stargate");
            CLOSE_FROM_EITHER_END = BUILDER
                .comment("Whether a stargate connection can be closed from either end, or only from the initiating end.")
                .define("closeFromEitherEnd", true);
            ONE_WAY_TRAVEL = BUILDER
                .comment("Whether a wormhole can be traversed in both directions, or only from the initiating side.")
                .define("oneWayTravel", false);
            SECONDS_TO_STAY_OPEN = BUILDER
                .comment("Number of seconds a connection stays open before closing automatically. Setting this to 0 will cause the gate to stay open until energy runs out.")
                .defineInRange("secondsToStayOpen", 300, 0, 999999);
            GATE_OPENINGS_PER_FUEL_ITEM = BUILDER
                .comment("Maximum number of times a gate can be opened using the fuel from one piece of naquadah. Will be less for long-distance and cross-dimension connections.")
                .defineInRange("gateOpeningsPerFuelItem", 24, 2, 1024);
            MINUTES_OPEN_PER_FUEL_ITEM = BUILDER
                .comment("Maximum number of minutes for which the fuel from one piece of naquadah will keep a gate open. Will be less for long-distance and cross-dimension connections.")
                .defineInRange("minutesOpenPerFuelItem", 80, 10, 320);
            DISTANCE_FACTOR_MULTIPLIER = BUILDER
                .comment("A coefficient governing the dependence of energy use on distance.")
                .defineInRange("distanceFactorMultiplier", 1.0D, 1.0D, 4.0D);
            INTER_DIMENSION_MULTIPLIER = BUILDER
                .comment("All energy use is multiplied by this factor when connecting between dimensions.")
                .defineInRange("interDimensionMultiplier", 4.0D, 1.0D, 8.0D);
            ENERGY_PER_FUEL_ITEM = BUILDER.
                comment("Number of stargate energy units (SEU) obtained from one piece of naquadah.")
                .defineInRange("energyPerFuelItem", 96000, 96000, 384000);
            MAX_ENERGY_BUFFER = BUILDER
                .comment("Capacity of stargate internal energy buffer in SEU.")
                .defineInRange("maxEnergyBuffer", 1000, 1000, 10000);
            EXPLOSION_RADIUS = BUILDER
                .comment("Size of explosion made by destroying an open stargate. Set to 0 to prevent explosions.")
                .defineInRange("explosionRadius", 0, 10, 40);
            EXPLOSION_FLAME = BUILDER
                .comment("Whether a stargate explosion sets things on fire.")
                .define("explosionFlame", true);
            EXPLOSION_SMOKE = BUILDER
                .comment("Whether a stargate explosion gives off smoke.")
                .define("explosionSmoke", true);
            TRANSPARENCY = BUILDER
                .comment("If true, the event horizon is rendered with partial transparency.")
                .define("transparency", true);
            SOUND_VOLUME = BUILDER
                .comment("Relative volume of stargate sounds")
                .defineInRange("soundVolume", 1.0D, 0.0D, 10.0D);
            VARIABLE_CHEVRON_POSITIONS = BUILDER
                .comment("If true, positions of chevrons vary depending on the number of chevrons and whether base camouflage is present. If false, chevrons are always in the same positions.")
                .define("variableChevronPositions", true);
        BUILDER.pop();

        BUILDER.push("iris");
            PRESERVE_INVENTORY = BUILDER
                .comment("If true, prevents the items in a player's inventory from being destroyed when the player is killed by an iris. The items will still be dropped, however, and armour being worn will take damage from the impact.")
                .define("preserveInventory", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

}
