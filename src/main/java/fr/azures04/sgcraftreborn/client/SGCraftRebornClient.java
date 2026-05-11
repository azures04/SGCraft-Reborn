package fr.azures04.sgcraftreborn.client;

import fr.azures04.sgcraftreborn.client.registries.ModRender;
import fr.azures04.sgcraftreborn.client.registries.ModScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class SGCraftRebornClient {

    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SGCraftRebornClient::clientSetup);
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        ModScreens.registerContainerScreens();
        ModRender.registerRenderTypes();
        ModRender.registerCustomTilesRenderer();
    }

}
