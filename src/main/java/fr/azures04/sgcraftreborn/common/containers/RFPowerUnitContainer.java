package fr.azures04.sgcraftreborn.common.containers;

import fr.azures04.sgcraftreborn.common.registries.tiles.RFPowerUnitTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RFPowerUnitContainer extends Container {

    private final RFPowerUnitTileEntity powerUnit;
    private int lastEnergyScaled = -1;
    private int clientEnergyScaled = 0;

    public RFPowerUnitContainer(InventoryPlayer playerInv, BlockPos pos) {
        super();
        this.powerUnit = (RFPowerUnitTileEntity) playerInv.player.world.getTileEntity(pos);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        double max = 4000000.0;
        double current = powerUnit.getStoredForgeEnergy();
        int currentEnergyScaled = (int) ((current / max) * 10000);

        for (IContainerListener listener : this.listeners) {
            if (this.lastEnergyScaled != currentEnergyScaled) {
                listener.sendWindowProperty(this, 0, currentEnergyScaled);
            }
        }
        this.lastEnergyScaled = currentEnergyScaled;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateProgressBar(int id, int data) {
        if (id == 0) this.clientEnergyScaled = data;
    }

    public double getEnergyScaled() {
        return this.clientEnergyScaled / 10000.0;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.getDistanceSq(powerUnit.getPos()) <= 64.0D;
    }
}