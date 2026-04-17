package fr.azures04.sgcraftreborn.common.inventories.slots;

import fr.azures04.sgcraftreborn.common.registries.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class NaquadahFuelSlot extends SlotItemHandler {

    public NaquadahFuelSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return stack.getItem() == ModItems.NAQUADAH.getItem();
    }


}
