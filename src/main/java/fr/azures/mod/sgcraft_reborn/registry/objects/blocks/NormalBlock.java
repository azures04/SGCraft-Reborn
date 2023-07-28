package fr.azures.mod.sgcraft_reborn.registry.objects.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class NormalBlock extends Block {

	public NormalBlock() {
        super(Properties.create(Material.ROCK).hardnessAndResistance(5).notSolid());
	}

}
