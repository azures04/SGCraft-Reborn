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
            stargate.getAvailableEnergy()
        };
    }

    public Object[] getEnergyToDial(String address) {
        String cleanAddress = address.replace("-", "");

        StargateBaseTileEntity remoteGate = stargate.getRemoteGate(cleanAddress);

        if (remoteGate != null) {
            double distanceFactor = StargateBaseTileEntity.distanceFactorForCoordDifference(stargate, remoteGate);
            double energySGU = stargate.getEnergyToOpen() * distanceFactor;

            return new Object[] {
                energySGU
            };
        }

        return new Object[] {
            -1.0,
            "Gate not found or offline"
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
        String cleanAddress = address.replace("-", "");
        double distFactor = 1.0;

        StargateBaseTileEntity remoteGate = stargate.getRemoteGate(cleanAddress);
        if (remoteGate != null) {
            distFactor = StargateBaseTileEntity.distanceFactorForCoordDifference(stargate, remoteGate);
        }

        stargate.startDialing(cleanAddress, null, true, distFactor);
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
        stargate.sendMessageAcrossVortex(args);
    }

    public abstract void queueEvent(String eventName, Object... args);

}
