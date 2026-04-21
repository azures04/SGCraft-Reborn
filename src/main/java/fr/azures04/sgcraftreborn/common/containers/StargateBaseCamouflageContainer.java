package fr.azures04.sgcraftreborn.common.containers;

import fr.azures04.sgcraftreborn.common.containers.slots.CamouflageSlot;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateBaseTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class StargateBaseCamouflageContainer extends Container {

    public final StargateBaseTileEntity te;

    public StargateBaseCamouflageContainer(InventoryPlayer playerInv, BlockPos pos) {
        this.te = (StargateBaseTileEntity) playerInv.player.world.getTileEntity(pos);
        IItemHandler inventory = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(RuntimeException::new);

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
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.getDistanceSq(te.getPos()) <= 64.0D;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            itemstack = stackInSlot.copy();

            if (index < 5) {
                if (!this.mergeItemStack(stackInSlot, 5, 41, true)) return ItemStack.EMPTY;
            } else {
                if (stackInSlot.getItem() instanceof ItemBlock) {
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
