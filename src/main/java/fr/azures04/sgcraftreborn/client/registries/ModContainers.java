package fr.azures04.sgcraftreborn.client.registries;

import fr.azures04.sgcraftreborn.client.screens.RFPowerUnitScreen;
import fr.azures04.sgcraftreborn.client.screens.StargateBaseCamouflageScreen;
import fr.azures04.sgcraftreborn.client.screens.StargateControllerFuelScreen;
import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.containers.RFPowerUnitContainer;
import fr.azures04.sgcraftreborn.common.containers.StargateBaseCamouflageContainer;
import fr.azures04.sgcraftreborn.common.containers.StargateControllerFuelContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.FMLPlayMessages;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ModContainers {

    public static final HashMap<String, Function<FMLPlayMessages.OpenContainer, GuiScreen>> CONTAINERS = new HashMap<>();

    static {
        register("controller_fuel", StargateControllerFuelContainer::new, StargateControllerFuelScreen::new);
        register("base_camouflage", StargateBaseCamouflageContainer::new, StargateBaseCamouflageScreen::new);
        register("rf_power_unit", RFPowerUnitContainer::new, RFPowerUnitScreen::new);
    }

    private static <C extends Container, S extends GuiScreen> void register(String id, BiFunction<InventoryPlayer, BlockPos, C> containerFactory, Function<C, S> screenFactory) {
        CONTAINERS.put(Constants.MOD_ID + ":" + id, (openCon) -> {
            BlockPos pos = openCon.getAdditionalData().readBlockPos();
            InventoryPlayer inv = Minecraft.getInstance().player.inventory;
            C container = containerFactory.apply(inv, pos);
            container.windowId = openCon.getWindowId();
            return screenFactory.apply(container);
        });
    }

    public static GuiScreen openContainer(FMLPlayMessages.OpenContainer containerToOpen) {
        String guiIdentifier = containerToOpen.getId().toString();
        if (CONTAINERS.containsKey(guiIdentifier)) {
            return CONTAINERS.get(guiIdentifier).apply(containerToOpen);
        }
        return null;
    }

}
