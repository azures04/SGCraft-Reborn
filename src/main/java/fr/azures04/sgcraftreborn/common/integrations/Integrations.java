package fr.azures04.sgcraftreborn.common.integrations;

import net.minecraftforge.fml.ModList;

public class Integrations {

    public static void setup() {
        if (ModList.get().isLoaded("computercraft")) {
            CCTPeripheralProvider.register();
        }
    }

}
