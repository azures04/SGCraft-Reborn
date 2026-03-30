package fr.azures04.sgcraftreborn.registries;

import fr.azures04.sgcraftreborn.Constants;
import fr.azures04.sgcraftreborn.client.models.ISpecialItemRenderer;
import fr.azures04.sgcraftreborn.registries.blocks.StargateBaseBlock;
import fr.azures04.sgcraftreborn.registries.blocks.StargateControllerBlock;
import fr.azures04.sgcraftreborn.registries.tiles.StargateControllerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.List;

public class ModRegistry {

    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        for (Block block : ModBlocks.BLOCKS_TO_REGISTER) {
            registry.register(block);
        }
    }

    @SubscribeEvent
    public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        for (Block block : ModBlocks.BLOCKS_TO_REGISTER) {
            Item.Properties properties = new Item.Properties().group(ItemGroup.MISC);
            if (block instanceof ISpecialItemRenderer) {
                properties = properties.setTEISR(((ISpecialItemRenderer) block)::getISTER);
            }
            ItemBlock blockItem = new ItemBlock(block, properties);
            blockItem.setRegistryName(block.getRegistryName());
            registry.register(blockItem);
        }
    }

    @SubscribeEvent
    public static void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> event) {
        ModTilesEntities.TILE_ENTITY_TYPES.forEach(event.getRegistry()::register);
    }

}