package fr.azures.mod.sgcraft_reborn.registry;

import fr.azures.mod.sgcraft_reborn.registry.objects.tiles.StargateControllerTile;
import fr.azures.mod.sgcraft_reborn.utils.Constants;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModTilesEntities {

    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_TYPES = DeferredRegister
    		.create(ForgeRegistries.TILE_ENTITIES, Constants.MOD_ID);
	
    public static final RegistryObject<TileEntityType<StargateControllerTile>> STARGATE_CONTROLLER = TILE_ENTITY_TYPES
    		.register("stargate_controller_tile", () -> TileEntityType.Builder.of(StargateControllerTile::new, ModBlocks.STARGATE_CONTROLLER.get()).build(null));
    
}
