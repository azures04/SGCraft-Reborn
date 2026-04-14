package fr.azures04.sgcraftreborn.common.listeners;

import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.config.SGCraftRebornConfig;
import fr.azures04.sgcraftreborn.common.registries.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldGenHandler {

    @SubscribeEvent
    public static void onChunkLoad(ChunkDataEvent.Load event) {
        if (!SGCraftRebornConfig.ADD_ORES_TO_EXISTING_WORLDS.get()) return;
        NBTTagCompound nbt = event.getData();
        if (nbt.getBoolean("sgcraft_naquadah_generated")) return;
        generateNaquadah(event.getChunk(), event.getWorld());
        nbt.setBoolean("sgcraft_naquadah_generated", true);
    }

    @SubscribeEvent
    public static void onChunkSave(ChunkDataEvent.Save event) {

    }

    private static void generateNaquadah(IChunk chunk, IWorld world) {
        Random random = new Random();
        ChunkPos chunkPos = chunk.getPos();

        int chunkX = chunkPos.getXStart();
        int chunkZ = chunkPos.getZStart();

        int maxNodes = SGCraftRebornConfig.MAX_ISOLATED_NODES.get();
        int odds = SGCraftRebornConfig.GEN_ISOLATED_ODDS.get();

        for (int i = 0; i < maxNodes; i++) {
            if (random.nextInt(odds) == 0) {
                int x = chunkX + random.nextInt(16);
                int y = 16 + random.nextInt(48);
                int z = chunkZ + random.nextInt(16);

                BlockPos pos = new BlockPos(x, y, z);
                if (world.getBlockState(pos).getBlock() == Blocks.STONE) {
                    world.setBlockState(pos, ModBlocks.NAQUADAH_ORE.getDefaultState(), 2);
                }
            }
        }

        int maxLavaNodes = SGCraftRebornConfig.MAX_NODES_UNDER_LAVA.get();
        int lavaOdds = SGCraftRebornConfig.GEN_UNDER_LAVA_ODDS.get();

        for (int i = 0; i < maxLavaNodes; i++) {
            if (random.nextInt(lavaOdds) == 0) {
                int x = chunkX + random.nextInt(16);
                int y = 1 + random.nextInt(10);
                int z = chunkZ + random.nextInt(16);

                BlockPos pos = new BlockPos(x, y, z);
                if (world.getBlockState(pos).getBlock() == Blocks.STONE) {
                    world.setBlockState(pos, ModBlocks.NAQUADAH_ORE.getDefaultState(), 2);
                }
            }
        }
    }
}