package fr.azures04.sgcraftreborn.common.integrations;

import dan200.computercraft.api.peripheral.IPeripheral;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateBaseTileEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;

public class CCTIntegration {

    @CapabilityInject(IPeripheral.class)
    public static Capability<IPeripheral> CAPABILITY_PERIPHERAL = null;

    public static LazyOptional<IPeripheral> createPeripheral(StargateBaseTileEntity gate) {
        return LazyOptional.of(() -> new CCTPeripheral(gate));
    }

}
