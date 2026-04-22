package fr.azures04.sgcraftreborn.common.listeners;

import fr.azures04.sgcraftreborn.SGCraftReborn;
import fr.azures04.sgcraftreborn.common.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LootTableHandler {

    // Tables vanilla que tu veux injecter
    private static final Map<ResourceLocation, ResourceLocation> INJECT_TABLES = new HashMap<>();

    static {
        INJECT_TABLES.put(
                new ResourceLocation("minecraft", "chests/simple_dungeon"),
                new ResourceLocation(Constants.MOD_ID, "inject/chests/simple_dungeon")
        );
        INJECT_TABLES.put(
                new ResourceLocation("minecraft", "chests/abandoned_mineshaft"),
                new ResourceLocation(Constants.MOD_ID, "inject/chests/abandoned_mineshaft")
        );
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation injectTable = INJECT_TABLES.get(event.getName());
        if (injectTable == null) return;

        LootTable table = event.getLootTableManager().getLootTableFromLocation(injectTable);
        if (table == LootTable.EMPTY_LOOT_TABLE) {
            SGCraftReborn.LOGGER.warn("Loot table d'injection introuvable : {}", injectTable);
            return;
        }

        for (LootPool pool : getPools(table)) {
            event.getTable().addPool(pool);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<LootPool> getPools(LootTable table) {
        try {
            Field poolsField = LootTable.class.getDeclaredField("pools");
            poolsField.setAccessible(true);
            return (List<LootPool>) poolsField.get(table);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Avec les mappings MCP le champ peut s'appeler "field_186466_c"
            try {
                Field poolsField = LootTable.class.getDeclaredField("field_186466_c");
                poolsField.setAccessible(true);
                return (List<LootPool>) poolsField.get(table);
            } catch (Exception ex) {
                throw new RuntimeException("Impossible d'accéder aux pools de la LootTable", ex);
            }
        }
    }
}