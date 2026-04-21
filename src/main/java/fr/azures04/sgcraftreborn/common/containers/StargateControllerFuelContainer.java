package fr.azures04.sgcraftreborn.common.containers;

import fr.azures04.sgcraftreborn.common.config.SGCraftRebornConfig;
import fr.azures04.sgcraftreborn.common.containers.slots.NaquadahFuelSlot;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateControllerTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class StargateControllerFuelContainer extends Container {

    private final StargateControllerTileEntity controller;
    private int lastEnergyScaled = -1;
    private int clientEnergyScaled = 0;

    public StargateControllerFuelContainer(InventoryPlayer playerInv, BlockPos pos) {
        super();

        this.controller = (StargateControllerTileEntity) playerInv.player.world.getTileEntity(pos);
        IItemHandler inventory = controller.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(RuntimeException::new);

        this.addSlot(new NaquadahFuelSlot(inventory, 0, 174, 84));
        this.addSlot(new NaquadahFuelSlot(inventory, 1, 192, 84));
        this.addSlot(new NaquadahFuelSlot(inventory, 2, 174, 102));
        this.addSlot(new NaquadahFuelSlot(inventory, 3, 192, 102));

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 48 + col * 18, 124 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 48 + col * 18, 182));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            itemstack = stackInSlot.copy();

            if (index < 4) {
                if (!this.mergeItemStack(stackInSlot, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (NaquadahFuelSlot.isFuel(stackInSlot)) {
                    if (!this.mergeItemStack(stackInSlot, 0, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.getDistanceSq(controller.getPos()) <= 8.0D;
    }

    public double getEnergyScaled() {
        return clientEnergyScaled / 10000.0;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        double fuel = controller.getFuelLevel();
        double max = SGCraftRebornConfig.ENERGY_PER_FUEL_ITEM.get();
        int currentEnergyScaled = max > 0 ? (int) ((fuel / max) * 10000) : 0;

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
        if (id == 0) {
            this.clientEnergyScaled = data;
        }
    }

}
