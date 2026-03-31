package fr.azures04.sgcraftreborn.registries;

import fr.azures04.sgcraftreborn.Constants;
import net.minecraft.item.Item;
import java.util.ArrayList;
import java.util.List;

public class ModItems {

    public static final List<Item> ITEMS_TO_REGISTER = new ArrayList<>();

    public static final Item NAQUADAH;
    public static final Item NAQUADAH_INGOT;
    public static final Item STARGATE_CORE_CRYSTAL;
    public static final Item STARGATE_IRIS_BLADE;
    public static final Item STARGATE_IRIS_UPGRADE;
    public static final Item STARGATE_CHEVRON_UPGRADE;
    public static final Item STARGATE_CONTROLLER_CRYSTAL;

    static {
        NAQUADAH = register("naquadah", new Item(new Item.Properties().group(ModItemGroups.SGCRAFT_REBORN)));
        NAQUADAH_INGOT = register("naquadah_ingot", new Item(new Item.Properties().group(ModItemGroups.SGCRAFT_REBORN)));
        STARGATE_CORE_CRYSTAL = register("stargate_core_crystal", new Item(new Item.Properties().group(ModItemGroups.SGCRAFT_REBORN)));
        STARGATE_IRIS_BLADE = register("stargate_iris_blade", new Item(new Item.Properties().group(ModItemGroups.SGCRAFT_REBORN)));
        STARGATE_IRIS_UPGRADE = register("stargate_iris_upgrade", new Item(new Item.Properties().group(ModItemGroups.SGCRAFT_REBORN)));
        STARGATE_CHEVRON_UPGRADE = register("stargate_chevron_upgrade", new Item(new Item.Properties().group(ModItemGroups.SGCRAFT_REBORN)));
        STARGATE_CONTROLLER_CRYSTAL = register("stargate_controller_crystal", new Item(new Item.Properties().group(ModItemGroups.SGCRAFT_REBORN)));
    }

    private static <T extends Item> T register(String name, T item) {
        item.setRegistryName(Constants.MOD_ID, name);
        ITEMS_TO_REGISTER.add(item);
        return item;
    }
}