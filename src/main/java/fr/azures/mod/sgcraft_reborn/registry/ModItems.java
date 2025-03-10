package fr.azures.mod.sgcraft_reborn.registry;

import fr.azures.mod.sgcraft_reborn.SGCraftReborn;
import fr.azures.mod.sgcraft_reborn.utils.Constants;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
	
	public static final DeferredRegister<Item> ITEMS =  DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);

	public static final RegistryObject<Item> NAQUADAH = ITEMS.register("naquadah", () -> new Item(new Item.Properties().tab(SGCraftReborn.SG_CRAFT_TAB)));
	public static final RegistryObject<Item> NAQUADAH_NNGOT = ITEMS.register("naquadah_ingot", () -> new Item(new Item.Properties().tab(SGCraftReborn.SG_CRAFT_TAB)));
	public static final RegistryObject<Item> SG_CHEVRON_UPGRADE = ITEMS.register("sg_chevron_upgrade", () -> new Item(new Item.Properties().tab(SGCraftReborn.SG_CRAFT_TAB)));
	public static final RegistryObject<Item> SG_CONTROLLER_CRYSTALL = ITEMS.register("sg_controller_crystal", () -> new Item(new Item.Properties().tab(SGCraftReborn.SG_CRAFT_TAB)));
	public static final RegistryObject<Item> SG_CORE_CRYSTAL = ITEMS.register("sg_core_crystal", () -> new Item(new Item.Properties().tab(SGCraftReborn.SG_CRAFT_TAB)));
	public static final RegistryObject<Item> SG_IRIS_BLADE = ITEMS.register("sg_iris_blade", () -> new Item(new Item.Properties().tab(SGCraftReborn.SG_CRAFT_TAB)));
	public static final RegistryObject<Item> SG_IRIS_UPGRADE = ITEMS.register("sg_iris_upgrade", () -> new Item(new Item.Properties().tab(SGCraftReborn.SG_CRAFT_TAB)));
	
}
