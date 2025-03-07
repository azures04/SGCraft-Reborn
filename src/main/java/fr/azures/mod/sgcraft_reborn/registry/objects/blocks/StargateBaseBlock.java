package fr.azures.mod.sgcraft_reborn.registry.objects.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.Direction;

public class StargateBaseBlock extends Block {

	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
	
	public StargateBaseBlock() {
        super(Properties.of(Material.STONE).harvestLevel(5).air());
	}
    
    
}
