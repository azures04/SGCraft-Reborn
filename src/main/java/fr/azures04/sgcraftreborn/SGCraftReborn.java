package fr.azures04.sgcraftreborn;

import fr.azures04.sgcraftreborn.client.models.tiles.StargateControllerTileEntityRenderer;
import fr.azures04.sgcraftreborn.config.SGCraftRebornConfig;
import fr.azures04.sgcraftreborn.config.conditions.CraftingConditions;
import fr.azures04.sgcraftreborn.registries.ModRegistry;
import fr.azures04.sgcraftreborn.registries.tiles.StargateControllerTileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Constants.MOD_ID)
public class SGCraftReborn {
    public static final Logger LOGGER = LogManager.getLogger();

    public SGCraftReborn() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().register(ModRegistry.class);

        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SGCraftRebornConfig.SPEC);
        CraftingHelper.register(CraftingConditions.ID, new CraftingConditions());
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("SGCraft Reborn : Initialisation commune...");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(StargateControllerTileEntity.class, new StargateControllerTileEntityRenderer());
    }
}