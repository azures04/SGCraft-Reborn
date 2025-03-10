package fr.azures.mod.sgcraft_reborn;

import fr.azures.mod.sgcraft_reborn.config.ModConfig;
import fr.azures.mod.sgcraft_reborn.registry.ModBlocks;
import fr.azures.mod.sgcraft_reborn.registry.ModItems;
import fr.azures.mod.sgcraft_reborn.registry.ModTilesEntities;
import fr.azures.mod.sgcraft_reborn.tabs.SGCraftTab;
import fr.azures.mod.sgcraft_reborn.utils.Constants;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID) 
public class SGCraftReborn {
	
	public static final ItemGroup SG_CRAFT_TAB = new SGCraftTab();
	
	public SGCraftReborn() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		ModItems.ITEMS.register(bus);
		ModBlocks.BLOCKS.register(bus);
		ModTilesEntities.TILE_ENTITY_TYPES.register(bus);
		
		ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.SPEC, "sgcraft_reborn-common.toml");
	}
	
	private void setup(FMLCommonSetupEvent event) {
		
	}
	
	private void clientSetup(FMLClientSetupEvent event) {
		
	}
}
