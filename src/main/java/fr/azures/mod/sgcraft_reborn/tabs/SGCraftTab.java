package fr.azures.mod.sgcraft_reborn.tabs;

import fr.azures.mod.sgcraft_reborn.registry.ModItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class SGCraftTab extends ItemGroup {

	public SGCraftTab() {
		super("SGCraft Reborn");
	}

	@Override
	public ItemStack createIcon() {
		return new ItemStack(ModItems.NAQUADAH.get());
	}

}
