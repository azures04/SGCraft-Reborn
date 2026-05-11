package fr.azures04.sgcraftreborn.common.integrations;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
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

    public CCTPeripheral(StargateBaseTileEntity stargate) {
        super(stargate);
    }

    @Nonnull
    @Override
    public String getType() {
        return "stargate";
    }

    @LuaFunction
    public final Object[] stargateState() {
        return super.getStargateState();
    }

    @LuaFunction
    public final Object[] energyAvailable() {
        return super.getEnergyAvailable();
    }

    @LuaFunction
    public final Object[] energyToDial(String address) throws LuaException {
        return super.getEnergyToDial(address);
    }

    @LuaFunction
    public final Object[] localAddress() {
        return super.getLocalAddress();
    }

    @LuaFunction
    public final Object[] remoteAddress() {
        return super.getRemoteAddress();
    }

    @LuaFunction
    public final Object[] irisState() {
        return super.getIrisState();
    }

    @LuaFunction(mainThread = true)
    public final void dial(String address) {
        super.dial(address);
    }

    @LuaFunction(mainThread = true)
    public final void disconnect() {
        super.disconnect();
    }

    @LuaFunction(mainThread = true)
    public final void openIris() {
        super.openIris();
    }

    @LuaFunction(mainThread = true)
    public final void closeIris() {
        super.closeIris();
    }

    @LuaFunction(mainThread = true)
    public final void sendMessage(Object[] args) {
        super.sendMessage(args);
    }

    @Override
    public void attach(@Nonnull IComputerAccess computer) {
        connectedComputers.add(computer);
        stargate.addComputerAdapter(this);
    }

    @Override
    public void detach(@Nonnull IComputerAccess computer) {
        connectedComputers.remove(computer);
        if (connectedComputers.isEmpty()) {
            stargate.removeComputerAdapter(this);
        }
    }

    @Override
    public void queueEvent(String eventName, Object... args) {
        for (IComputerAccess computer : connectedComputers) {
            Object[] newArgs = new Object[args.length + 1];
            newArgs[0] = computer.getAttachmentName();
            System.arraycopy(args, 0, newArgs, 1, args.length);
            computer.queueEvent(eventName, newArgs);
        }
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return this == other;
    }
}