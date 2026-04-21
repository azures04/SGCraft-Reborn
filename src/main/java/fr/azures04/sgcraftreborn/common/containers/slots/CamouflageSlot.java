package fr.azures04.sgcraftreborn.common.containers.slots;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class CamouflageSlot extends SlotItemHandler {

    public CamouflageSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return stack.getItem() instanceof ItemBlock;
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 1;
    }
}
