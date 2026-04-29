package fr.azures04.sgcraftreborn.common.integrations;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import fr.azures04.sgcraftreborn.common.api.StargateAbstractAPI;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateBaseTileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CCTPeripheral extends StargateAbstractAPI implements IPeripheral {

    private final List<IComputerAccess> connectedComputers = new ArrayList<>();

    private final String[] METHOD_NAMES = new String[] {
        "stargateState",
        "energyAvailable",
        "energyToDial",
        "localAddress",
        "remoteAddress",
        "dial",
        "disconnect",
        "irisState",
        "openIris",
        "closeIris",
        "sendMessage"
    };

    public CCTPeripheral(StargateBaseTileEntity stargate) {
        super(stargate);
    }

    @Nonnull
    @Override
    public String getType() {
        return "stargate";
    }

    @Nonnull
    @Override
    public String[] getMethodNames() {
        return METHOD_NAMES;
    }

    @Nullable
    @Override
    public Object[] callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method, @Nonnull Object[] args) throws LuaException, InterruptedException {
        switch (method) {
            case 0:
                return super.getStargateState();
            case 1:
                return super.getEnergyAvailable();
            case 2:
                if (args.length < 1 || !(args[0] instanceof String)) {
                    throw new LuaException("Expected string argument for address");
                }
                return super.getEnergyToDial((String) args[0]);
            case 3:
                return super.getLocalAddress();
            case 4:
                return super.getRemoteAddress();
            case 5:
                if (args.length < 1 || !(args[0] instanceof String)) {
                    throw new LuaException("Expected string argument for address");
                }
                super.dial((String) args[0]);
                return null;
            case 6:
                super.disconnect();
                return null;
            case 7:
                return super.getIrisState();
            case 8:
                super.openIris();
                return null;
            case 9:
                super.closeIris();
                return null;
            case 10:
                super.sendMessage(args);
                return null;
            default:
                throw new LuaException("Invalid method index");
        }
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return this == other;
    }

    @Override
    public void attach(@Nonnull IComputerAccess computer) {
        connectedComputers.add(computer);
    }

    @Override
    public void detach(@Nonnull IComputerAccess computer) {
        connectedComputers.remove(computer);
    }

    @Override
    public void queueEvent(String eventName, Object... args) {
        for (IComputerAccess computer : connectedComputers) {
            computer.queueEvent(eventName, args);
        }
    }

}
