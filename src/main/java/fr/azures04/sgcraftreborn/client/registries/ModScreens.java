package fr.azures04.sgcraftreborn.client.registries;

import fr.azures04.sgcraftreborn.client.screens.RFPowerUnitScreen;
import fr.azures04.sgcraftreborn.client.screens.StargateBaseCamouflageScreen;
import fr.azures04.sgcraftreborn.client.screens.StargateControllerFuelScreen;
import fr.azures04.sgcraftreborn.client.screens.StargateControllerScreen;
import fr.azures04.sgcraftreborn.common.registries.ModContainers;
import fr.azures04.sgcraftreborn.common.registries.blocks.states.StargateControllerStatus;
import fr.azures04.sgcraftreborn.common.util.math.ExtendedPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

import java.util.HashMap;
import java.util.function.Function;

public class ModScreens {

    private static final HashMap<String, Function<Object[], Screen>> SCREENS = new HashMap<>();

    static {
        SCREENS.put("controller_main", (args) -> {
            ExtendedPos pos = (ExtendedPos) args[0];
            StargateControllerStatus status = (StargateControllerStatus) args[1];
            boolean hasChevron = (boolean) args[2];
            return new StargateControllerScreen(new StringTextComponent(""), pos, status, hasChevron);
        });
    }

    public static void open(String id, Object... args) {
        if (SCREENS.containsKey(id)) {
            Minecraft.getInstance().displayGuiScreen(SCREENS.get(id).apply(args));
        }
    }

    public static void registerContainerScreens() {
        ScreenManager.registerFactory(ModContainers.CONTROLLER_FUEL, StargateControllerFuelScreen::new);
        ScreenManager.registerFactory(ModContainers.BASE_CAMOUFLAGE, StargateBaseCamouflageScreen::new);
        ScreenManager.registerFactory(ModContainers.RF_POWER_UNIT, RFPowerUnitScreen::new);
    }

}
