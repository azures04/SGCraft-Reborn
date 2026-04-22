package fr.azures04.sgcraftreborn.common.integrations;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import fr.azures04.sgcraftreborn.common.registries.tiles.ComputerCraftInterfaceTileEntity;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateBaseTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CCTPeripheralProvider implements IPeripheralProvider {

    @Nullable
    @Override
    public IPeripheral getPeripheral(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);

        if (tileEntity instanceof ComputerCraftInterfaceTileEntity) {
            ComputerCraftInterfaceTileEntity interfaceTe = (ComputerCraftInterfaceTileEntity) tileEntity;

            if (interfaceTe.getPeripheralAdapter() instanceof IPeripheral) {
                return (IPeripheral) interfaceTe.getPeripheralAdapter();
            }

            StargateBaseTileEntity stargate = interfaceTe.findStargate();
            if (stargate != null) {
                CCTPeripheral peripheral = new CCTPeripheral(stargate);
                stargate.addComputerAdapter(peripheral);
                interfaceTe.setPeripheralAdapter(peripheral);
                return peripheral;
            }
        }
        return null;
    }

    public static void register() {
        ComputerCraftAPI.registerPeripheralProvider(new CCTPeripheralProvider());
    }
}
