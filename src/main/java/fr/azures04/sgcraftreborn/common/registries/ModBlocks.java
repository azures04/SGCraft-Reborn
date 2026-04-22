package fr.azures04.sgcraftreborn.common.registries;

import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.registries.blocks.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

public class ModBlocks {

    public static final List<Block> BLOCKS_TO_REGISTER = new ArrayList<>();

    public static final Block NAQUADAH_ORE;
    public static final Block NAQUADAH_BLOCK;
    public static final StargateBaseBlock STARGATE_BASE;
    public static final StargateRingBlock STARGATE_RING;
    public static final StargateChevronBlock STARGATE_CHEVRON;
    public static final StargateControllerBlock STARGATE_CONTROLLER;

    public static ComputerCraftInterfaceBlock CC_INTERFACE;
    public static final RFPowerUnitBlock RF_POWER_UNIT;

    private static final Block.Properties defaultProperties = Block.Properties.create(Material.IRON).hardnessAndResistance(15.0F);

    static {
        STARGATE_RING = register("stargate_ring", new StargateRingBlock(defaultProperties.lightValue(0).variableOpacity()));
        STARGATE_CHEVRON = register("stargate_chevron", new StargateChevronBlock(defaultProperties.lightValue(0).variableOpacity()));
        STARGATE_BASE = register("stargate_base", new StargateBaseBlock(defaultProperties.lightValue(0).variableOpacity()));
        STARGATE_CONTROLLER = register("stargate_controller", new StargateControllerBlock(defaultProperties));
        NAQUADAH_ORE = register("naquadah_ore", new Block(defaultProperties));
        NAQUADAH_BLOCK = register("naquadah_block", new Block(defaultProperties));

        if (ModList.get().isLoaded("computercraft")) {
            CC_INTERFACE = register("cc_interface", new ComputerCraftInterfaceBlock(defaultProperties));
        }
        RF_POWER_UNIT = register("rf_power_unit", new RFPowerUnitBlock(defaultProperties));
    }

    private static <T extends Block> T register(String name, T block) {
        block.setRegistryName(Constants.MOD_ID, name);
        BLOCKS_TO_REGISTER.add(block);
        return block;
    }
}