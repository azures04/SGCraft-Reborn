package fr.azures04.sgcraftreborn.registries;

import fr.azures04.sgcraftreborn.Constants;
import fr.azures04.sgcraftreborn.registries.blocks.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

public class ModBlocks {

    public static final List<Block> BLOCKS_TO_REGISTER = new ArrayList<>();

    public static final Block NAQUADAH_ORE;
    public static final Block NAQUADAH_BLOCK;
    public static final Block STARGATE_BASE;
    public static final Block STARGATE_RING;
    public static final Block STARGATE_CHEVRON;
    public static final Block STARGATE_CONTROLLER;

    static {
        STARGATE_RING = register("stargate_ring", new StargateRingBlock(Block.Properties.create(Material.IRON)));
        STARGATE_CHEVRON = register("stargate_chevron", new StargateChevronBlock(Block.Properties.create(Material.IRON)));
        STARGATE_BASE = register("stargate_base", new StargateBaseBlock(Block.Properties.create(Material.IRON)));
        STARGATE_CONTROLLER = register("stargate_controller", new StargateControllerBlock(Block.Properties.create(Material.IRON)));
        NAQUADAH_ORE = register("naquadah_ore", new Block(Block.Properties.create(Material.IRON)));
        NAQUADAH_BLOCK = register("naquadah_block", new Block(Block.Properties.create(Material.IRON)));
    }

    private static <T extends Block> T register(String name, T block) {
        block.setRegistryName(Constants.MOD_ID, name);
        BLOCKS_TO_REGISTER.add(block);
        return block;
    }
}