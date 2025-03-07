package fr.azures.mod.sgcraft_reborn.client.model;

import fr.azures.mod.sgcraft_reborn.registry.ModTilesEntities;
import fr.azures.mod.sgcraft_reborn.registry.objects.tiles.renderer.StargateControllerTileRenderer;
import fr.azures.mod.sgcraft_reborn.utils.Constants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventSubscriber {
    @SubscribeEvent
    public static void onRegisterModels(ModelRegistryEvent event) {
        ClientRegistry.bindTileEntityRenderer(ModTilesEntities.STARGATE_CONTROLLER.get(), StargateControllerTileRenderer::new);
    }
}