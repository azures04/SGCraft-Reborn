package fr.azures04.sgcraftreborn.common.containers;

import fr.azures04.sgcraftreborn.common.registries.ModContainers;
import fr.azures04.sgcraftreborn.common.registries.tiles.RFPowerUnitTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;

public class RFPowerUnitContainer extends Container {

    private final RFPowerUnitTileEntity powerUnit;
    private int clientEnergyScaled = 0;

    public RFPowerUnitContainer(int windowId, PlayerInventory playerInv, BlockPos pos) {
        super(ModContainers.RF_POWER_UNIT, windowId);
        this.powerUnit = (RFPowerUnitTileEntity) playerInv.player.world.getTileEntity(pos);

        this.trackInt(new IntReferenceHolder() {
            @Override
            public int get() {
                double max = 4000000.0;
                double current = powerUnit.getStoredForgeEnergy();
                return (int) ((current / max) * 10000);
            }

            @Override
            public void set(int value) {
                clientEnergyScaled = value;
            }
        });
    }

    public double getEnergyScaled() {
        return this.clientEnergyScaled / 10000.0;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return playerIn.getDistanceSq(
                powerUnit.getPos().getX() + 0.5D,
                powerUnit.getPos().getY() + 0.5D,
                powerUnit.getPos().getZ() + 0.5D
        ) <= 64.0D;
    }
}