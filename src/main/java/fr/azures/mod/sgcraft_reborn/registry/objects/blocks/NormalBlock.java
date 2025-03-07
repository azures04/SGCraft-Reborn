package fr.azures.mod.sgcraft_reborn.registry.objects.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class NormalBlock extends Block {

	public NormalBlock() {
        super(Properties.of(Material.STONE).harvestLevel(5).air());
	}

}
