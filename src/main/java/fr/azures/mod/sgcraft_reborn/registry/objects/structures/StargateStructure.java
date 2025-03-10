package fr.azures.mod.sgcraft_reborn.registry.objects.structures;

import fr.azures.mod.sgcraft_reborn.registry.ModBlocks;
import net.minecraft.block.Block;

public class StargateStructure {

	private static final Block[] BLOCKS_ORDER = {
			ModBlocks.STARGATE_RING.get(),
			ModBlocks.STARGATE_CHEVRON.get(),
			ModBlocks.STARGATE_RING.get(),
			ModBlocks.STARGATE_CHEVRON.get(),
			ModBlocks.STARGATE_RING.get(),
			ModBlocks.STARGATE_CHEVRON.get(),
			ModBlocks.STARGATE_RING.get(),
			ModBlocks.STARGATE_CHEVRON.get(),
			ModBlocks.STARGATE_RING.get(),
			ModBlocks.STARGATE_CHEVRON.get(),
			ModBlocks.STARGATE_RING.get(),
			ModBlocks.STARGATE_CHEVRON.get(),
			ModBlocks.STARGATE_RING.get(),
			ModBlocks.STARGATE_CHEVRON.get(),
			ModBlocks.STARGATE_RING.get()
	};
	
	public static boolean isStructureValid(Block[] pattern) {
		for (int i = 0; i < pattern.length; i++) {
			if (pattern[i] != BLOCKS_ORDER[i]) {
				return false;
			}
		}
		return true;
	}
}