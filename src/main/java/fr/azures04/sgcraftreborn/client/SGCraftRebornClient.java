package fr.azures04.sgcraftreborn.client;

import fr.azures04.sgcraftreborn.client.models.tiles.StargateBaseTileEntityRenderer;
import fr.azures04.sgcraftreborn.client.models.tiles.StargateControllerTileEntityRenderer;
import fr.azures04.sgcraftreborn.client.registries.ModContainers;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateBaseTileEntity;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateControllerTileEntity;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class SGCraftRebornClient {

    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SGCraftRebornClient::clientSetup);
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.GUIFACTORY,() -> ModContainers::openContainer);
        ClientRegistry.bindTileEntitySpecialRenderer(StargateControllerTileEntity.class, new StargateControllerTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(StargateBaseTileEntity.class, new StargateBaseTileEntityRenderer());
    }

}
