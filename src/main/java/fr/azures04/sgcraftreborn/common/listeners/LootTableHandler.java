package fr.azures04.sgcraftreborn.common.listeners;

import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.registries.ModItems;
import net.minecraft.world.storage.loot.EmptyLootEntry;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LootTableHandler {

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        String name = event.getName().toString();

        if (name.equals("minecraft:chests/desert_pyramid") || name.equals("minecraft:chests/jungle_temple") || name.equals("minecraft:chests/stronghold_corridor")) {
            event.getTable().addPool(LootPool.builder()
                .name("sgcraft_crystals")
                .rolls(RandomValueRange.of(1, 1))
                .addEntry(ItemLootEntry.builder(ModItems.STARGATE_CORE_CRYSTAL)
                        .weight(10)
                        .quality(1))
                .addEntry(ItemLootEntry.builder(ModItems.STARGATE_CONTROLLER_CRYSTAL)
                        .weight(5)
                        .quality(1))
                .addEntry(EmptyLootEntry.func_216167_a().weight(85))
            .build());
        }
    }

}
