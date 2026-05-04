package fr.azures04.sgcraftreborn;

import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.config.SGCraftRebornConfig;
import fr.azures04.sgcraftreborn.common.config.conditions.CraftingConditions;
import fr.azures04.sgcraftreborn.common.integrations.Integrations;
import fr.azures04.sgcraftreborn.common.network.StargateNetwork;
import fr.azures04.sgcraftreborn.common.registries.ModRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Constants.MOD_ID)
public class SGCraftReborn {
    public static final Logger LOGGER = LogManager.getLogger();

    public SGCraftReborn() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().register(ModRegistry.class);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> fr.azures04.sgcraftreborn.client.SGCraftRebornClient.init());

        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SGCraftRebornConfig.SPEC);
        CraftingHelper.register(CraftingConditions.Serializer.INSTANCE);
    }

    private void setup(final FMLCommonSetupEvent event) {
        DeferredWorkQueue.runLater(() -> {
            StargateNetwork.registerPackets();
        });
        Integrations.setup();
    }
}