package fr.azures04.sgcraftreborn.common.containers;

import fr.azures04.sgcraftreborn.common.containers.slots.CamouflageSlot;
import fr.azures04.sgcraftreborn.common.registries.ModContainers;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateBaseTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class StargateBaseCamouflageContainer extends Container {

    public final StargateBaseTileEntity base;

    public StargateBaseCamouflageContainer(int windowId, PlayerInventory playerInv, BlockPos pos) {
        super(ModContainers.BASE_CAMOUFLAGE, windowId);
        this.base = (StargateBaseTileEntity) playerInv.player.world.getTileEntity(pos);
        IItemHandler inventory = base.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(RuntimeException::new);

        for (int i = 0; i < 5; i++) {
            this.addSlot(new CamouflageSlot(inventory, i, 48 + (i * 18), 104));
        }

        int playerSlotsX = 48;
        int playerSlotsY = 126;

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9,playerSlotsX + col * 18,playerSlotsY + row * 18));
            }
        }

        int hotbarY = playerSlotsY + 58;
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, playerSlotsX + col * 18, hotbarY));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return playerIn.getDistanceSq(
            base.getPos().getX() + 0.5D,
            base.getPos().getY() + 0.5D,
            base.getPos().getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            itemstack = stackInSlot.copy();

            if (index < 5) {
                if (!this.mergeItemStack(stackInSlot, 5, 41, true)) return ItemStack.EMPTY;
            } else {
                if (stackInSlot.getItem() instanceof BlockItem) {
                    if (!this.mergeItemStack(stackInSlot, 0, 5, false)) return ItemStack.EMPTY;
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();
        }
        return itemstack;
    }

}
