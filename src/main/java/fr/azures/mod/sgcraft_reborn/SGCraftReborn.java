package fr.azures.mod.sgcraft_reborn;

import fr.azures.mod.sgcraft_reborn.registry.ModBlocks;
import fr.azures.mod.sgcraft_reborn.registry.ModItems;
import fr.azures.mod.sgcraft_reborn.registry.ModTilesEntities;
import fr.azures.mod.sgcraft_reborn.registry.objects.tiles.renderer.StargateControllerTileRenderer;
import fr.azures.mod.sgcraft_reborn.tabs.SGCraftTab;
import fr.azures.mod.sgcraft_reborn.utils.Constants;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
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
	}
	
	private void setup(FMLCommonSetupEvent event) {
		
	}
	
	private void clientSetup(FMLClientSetupEvent event) {
		
	}
	
    @SubscribeEvent
    public static void onRegisterModels(ModelRegistryEvent event) {
        ClientRegistry.bindTileEntityRenderer(ModTilesEntities.STARGATE_CONTROLLER.get(), StargateControllerTileRenderer::new);
        System.out.println("Prout");
    }
	
}
