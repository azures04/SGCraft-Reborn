package fr.azures04.sgcraftreborn.common.api;

import fr.azures04.sgcraftreborn.common.registries.tiles.StargateBaseTileEntity;
import fr.azures04.sgcraftreborn.common.registries.tiles.states.StargateVortexState;

public abstract class StargateAbstractAPI {

    protected final StargateBaseTileEntity stargate;

    public StargateAbstractAPI(StargateBaseTileEntity stargate) {
        this.stargate = stargate;
    }

    public Object[] getStargateState() {
        String state = stargate.getVortexState().name();
        int chevrons = stargate.getNumEngagedChevrons();
        String direction = "None";
        if (stargate.getVortexState() != StargateVortexState.IDLE) {
            direction = stargate.isInitiator() ? "Initiator" : "Receiver";
        }

        return new Object[] {
            state,
            chevrons,
            direction
        };
    }

    public Object[] getEnergyAvailable() {
        return new Object[] {
            stargate.getEnergyInBuffer()
        };
    }

    public Object[] getEnergyToDial(String address) {
        StargateBaseTileEntity remoteGate = stargate.getRemoteGate(address);

        if (remoteGate != null) {
            double distanceFactor = StargateBaseTileEntity.distanceFactorForCoordDifference(stargate, remoteGate);
            double energySGU = stargate.getEnergyToOpen() * distanceFactor;
            double energyFE = energySGU * 80.0;

            return new Object[] {
                energyFE
            };
        }
        return new Object[] {
            0.0
        };
    }

    public Object[] getLocalAddress() {
        return new Object[] {
            stargate.getAddress()
        };
    }

    public Object[] getRemoteAddress() {
        return new Object[] {
            stargate.getDialledAddress()
        };
    }

    public void dial(String address) {
        stargate.startDialing(address, null, true, 1.0);
    }

    public void disconnect() {
        stargate.disconnect();
    }

    public Object[] getIrisState() {
        return new Object[] {
            stargate.getIrisState().name()
        };
    }

    public void openIris() {
        stargate.setIrisDeployed(false);
    }

    public void closeIris() {
        stargate.setIrisDeployed(true);
    }

    public void sendMessage(Object... args) {
        // stargate.sendMessageToRemote(args);
    }

    public abstract void queueEvent(String eventName, Object... args);

}
