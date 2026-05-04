package fr.azures04.sgcraftreborn.common.registries;

import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.containers.RFPowerUnitContainer;
import fr.azures04.sgcraftreborn.common.containers.StargateBaseCamouflageContainer;
import fr.azures04.sgcraftreborn.common.containers.StargateControllerFuelContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;

import java.util.ArrayList;
import java.util.List;

public class ModContainers {

    public static final List<ContainerType<?>> CONTAINERS_TO_REGISTER = new ArrayList<>();

    public static ContainerType<StargateControllerFuelContainer> CONTROLLER_FUEL;
    public static ContainerType<StargateBaseCamouflageContainer> BASE_CAMOUFLAGE;
    public static ContainerType<RFPowerUnitContainer> RF_POWER_UNIT;

    static {
        CONTROLLER_FUEL = register("controller_fuel", IForgeContainerType.create((windowId, inv, data) -> new StargateControllerFuelContainer(windowId, inv, data.readBlockPos())));
        BASE_CAMOUFLAGE = register("base_camouflage", IForgeContainerType.create((windowId, inv, data) -> new StargateBaseCamouflageContainer(windowId, inv, data.readBlockPos())));
        RF_POWER_UNIT = register("rf_power_unit", IForgeContainerType.create((windowId, inv, data) -> new RFPowerUnitContainer(windowId, inv, data.readBlockPos())));
    }

    private static <T extends ContainerType<?>> T register(String name, T containerType) {
        containerType.setRegistryName(Constants.MOD_ID, name);
        CONTAINERS_TO_REGISTER.add(containerType);
        return containerType;
    }

}
