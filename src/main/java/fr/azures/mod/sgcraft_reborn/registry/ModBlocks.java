package fr.azures.mod.sgcraft_reborn.registry;

import java.util.function.Supplier;

import fr.azures.mod.sgcraft_reborn.SGCraftReborn;
import fr.azures.mod.sgcraft_reborn.registry.objects.blocks.NormalBlock;
import fr.azures.mod.sgcraft_reborn.registry.objects.blocks.StargateBaseBlock;
import fr.azures.mod.sgcraft_reborn.registry.objects.blocks.StargateControllerBlock;
import fr.azures.mod.sgcraft_reborn.utils.Constants;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {

	public static final DeferredRegister<Block> BLOCKS =  DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);

	public static final RegistryObject<Block> NAQUADAH_ORE = createBlock("naquadah_ore", () -> new NormalBlock());
	public static final RegistryObject<Block> NAQUADAH_BLOCK = createBlock("naquadah_block", () -> new NormalBlock());
	public static final RegistryObject<Block> STARGATE_BASE = createBlock("stargate_base", () -> new StargateBaseBlock());
	public static final RegistryObject<Block> STARGATE_RING = createBlock("stargate_ring", () -> new NormalBlock());
	public static final RegistryObject<Block> STARGATE_CHEVRON = createBlock("stargate_chevron", () -> new NormalBlock());
	public static final RegistryObject<Block> STARGATE_CONTROLLER = createBlock("stargate_controller", () -> new StargateControllerBlock());
	
	public static RegistryObject<Block> createBlock(String name, Supplier<? extends Block> supplier) {
		RegistryObject<Block> block = BLOCKS.register(name, supplier);
		ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().group(SGCraftReborn.SG_CRAFT_TAB)));
		return block;
	}
	
}
