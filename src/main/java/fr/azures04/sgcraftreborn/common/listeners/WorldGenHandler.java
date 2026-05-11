package fr.azures04.sgcraftreborn.common.listeners;

import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.config.SGCraftRebornConfig;
import fr.azures04.sgcraftreborn.common.registries.ModBlocks;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldGenHandler {

    // 1. File d'attente thread-safe pour stocker les chunks nécessitant la génération rétroactive
    private static final ConcurrentLinkedQueue<ChunkPos> PENDING_RETRO_GEN = new ConcurrentLinkedQueue<>();

    @SubscribeEvent
    public static void onChunkLoad(ChunkDataEvent.Load event) {
        if (!SGCraftRebornConfig.ADD_ORES_TO_EXISTING_WORLDS.get()) {
            return;
        }

        IWorld world = event.getWorld();
        if (world.isRemote()) return;

        CompoundNBT nbt = event.getData();

        if (!nbt.getBoolean("sgcraft_naquadah_generated")) {
            nbt.putBoolean("sgcraft_naquadah_generated", true);
            PENDING_RETRO_GEN.add(event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side.isClient() || event.phase == TickEvent.Phase.START || PENDING_RETRO_GEN.isEmpty()) {
            return;
        }

        if (!(event.world instanceof ServerWorld)) {
            return;
        }

        ServerWorld serverWorld = (ServerWorld) event.world;

        int processed = 0;
        int maxPerTick = 3;

        int size = PENDING_RETRO_GEN.size();
        for (int i = 0; i < size && processed < maxPerTick; i++) {
            ChunkPos chunkPos = PENDING_RETRO_GEN.poll();
            if (chunkPos == null) break;

            if (serverWorld.chunkExists(chunkPos.x, chunkPos.z)) {
                IChunk chunk = serverWorld.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);
                if (chunk != null) {
                    generateNaquadah(chunk, serverWorld);
                    processed++;
                    continue;
                }
            }

            PENDING_RETRO_GEN.add(chunkPos);
        }
    }

    public static void generateNaquadah(IChunk chunk, IWorld world) {
        if (world == null) return;

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