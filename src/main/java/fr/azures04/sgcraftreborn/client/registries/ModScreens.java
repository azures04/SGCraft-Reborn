package fr.azures04.sgcraftreborn.client.registries;

import fr.azures04.sgcraftreborn.SGCraftReborn;
import fr.azures04.sgcraftreborn.client.screens.StargateControllerScreen;
import fr.azures04.sgcraftreborn.common.registries.blocks.states.StargateControllerStatus;
import fr.azures04.sgcraftreborn.common.util.math.ExtendedPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.function.Function;

public class ModScreens {

    public static final HashMap<String, Function<Object[], GuiScreen>> SCREENS = new HashMap<>();

    static {
        SCREENS.put("controller_main", (args) -> {
            ExtendedPos pos = (ExtendedPos) args[0];
            StargateControllerStatus status = (StargateControllerStatus) args[1];
            boolean hasChevron = (boolean) args[2];

            return new StargateControllerScreen(pos, status, hasChevron);
        });
    }

    public static void openScreen(String id, Object... args) {
        if (SCREENS.containsKey(id)) {
            GuiScreen screen = SCREENS.get(id).apply(args);
            Minecraft.getInstance().displayGuiScreen(screen);
        } else {
            SGCraftReborn.LOGGER.log(Level.INFO, "Unknown gui");
        }
    }

}
