package fr.azures04.sgcraftreborn.common.registries;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ModItemGroups {

    public static final ItemGroup SGCRAFT_REBORN = new ItemGroup("sgcraftreborn") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModBlocks.STARGATE_BASE);
        }
    };

}
