package fr.azures04.sgcraftreborn.common.containers;

import fr.azures04.sgcraftreborn.common.config.SGCraftRebornConfig;
import fr.azures04.sgcraftreborn.common.containers.slots.NaquadahFuelSlot;
import fr.azures04.sgcraftreborn.common.registries.ModContainers;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateControllerTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class StargateControllerFuelContainer extends Container {

    private final StargateControllerTileEntity controller;
    private int clientEnergyScaled = 0;

    public StargateControllerFuelContainer(int windowId, PlayerInventory playerInv, BlockPos pos) {
        super(ModContainers.CONTROLLER_FUEL, windowId);

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

        // NOUVEAU SYSTEME 1.14.4 : Synchronisation automatique Serveur -> Client
        this.trackInt(new IntReferenceHolder() {
            @Override
            public int get() {
                // Lu par le serveur
                double fuel = controller.getFuelLevel();
                double max = SGCraftRebornConfig.ENERGY_PER_FUEL_ITEM.get();
                return max > 0 ? (int) ((fuel / max) * 10000) : 0;
            }

            @Override
            public void set(int value) {
                // Reçu par le client
                clientEnergyScaled = value;
            }
        });
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
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
    public boolean canInteractWith(PlayerEntity playerIn) {
        return playerIn.getDistanceSq(
                controller.getPos().getX() + 0.5D,
                controller.getPos().getY() + 0.5D,
                controller.getPos().getZ() + 0.5D
        ) <= 64.0D;
    }

    public double getEnergyScaled() {
        return clientEnergyScaled / 10000.0;
    }
}