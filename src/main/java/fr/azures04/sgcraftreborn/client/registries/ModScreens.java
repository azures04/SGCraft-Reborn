package fr.azures04.sgcraftreborn.client.registries;

import fr.azures04.sgcraftreborn.client.screens.RFPowerUnitScreen;
import fr.azures04.sgcraftreborn.client.screens.StargateBaseCamouflageScreen;
import fr.azures04.sgcraftreborn.client.screens.StargateControllerFuelScreen;
import fr.azures04.sgcraftreborn.common.registries.ModContainers;
import net.minecraft.client.gui.ScreenManager;

public class ModScreens {

    public static void registerScreens() {
        ScreenManager.registerFactory(ModContainers.CONTROLLER_FUEL, StargateControllerFuelScreen::new);
        ScreenManager.registerFactory(ModContainers.BASE_CAMOUFLAGE, StargateBaseCamouflageScreen::new);
        ScreenManager.registerFactory(ModContainers.RF_POWER_UNIT, RFPowerUnitScreen::new);
    }

}
