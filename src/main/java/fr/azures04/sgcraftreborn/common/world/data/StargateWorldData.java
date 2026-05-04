package fr.azures04.sgcraftreborn.common.world.data;

import fr.azures04.sgcraftreborn.common.util.math.ExtendedPos;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.Map;

public class StargateWorldData extends WorldSavedData {

    public static final String NAME = "sgcraftreborn_stargates";
    private Map<String, ExtendedPos> stargates = new HashMap<>();

    public StargateWorldData() {
        super(NAME);
    }

    public StargateWorldData(String name) {
        super(name);
    }

    @Override
    public void read(CompoundNBT nbt) {
        stargates.clear();
        ListNBT list = nbt.getList("stargates", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT entry = list.getCompound(i);
            String address = entry.getString("a");
            ExtendedPos pos = new ExtendedPos(
                    entry.getInt("x"),
                    entry.getInt("y"),
                    entry.getInt("z"),
                    entry.getInt("d")
            );
            stargates.put(address, pos);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        for (Map.Entry<String, ExtendedPos> entry : stargates.entrySet()) {
            CompoundNBT tag = new CompoundNBT();
            tag.putString("a", entry.getKey());
            tag.putInt("x", entry.getValue().getX());
            tag.putInt("y", entry.getValue().getY());
            tag.putInt("z", entry.getValue().getZ());
            tag.putInt("d", entry.getValue().getDimension());
            list.add(tag);
        }
        compound.put("stargates", list);
        return compound;
    }

    public void register(String address, ExtendedPos pos) {
        stargates.put(address, pos);
        markDirty();
    }

    public void unregister(String address) {
        stargates.remove(address);
        markDirty();
    }

    public static StargateWorldData get(World world) {
        if (!(world instanceof ServerWorld)) {
            throw new RuntimeException("Attempted to get StargateWorldData on client side!");
        }

        ServerWorld serverWorld = (ServerWorld) world;
        DimensionSavedDataManager storage = serverWorld.getSavedData();

        return storage.getOrCreate(StargateWorldData::new, NAME);
    }

    public boolean exists(String address) {
        if (stargates.containsKey(address)) {
            return true;
        }

        for (String storedAddress : stargates.keySet()) {
            if (storedAddress.startsWith(address)) {
                return true;
            }
        }
        return false;
    }

    public ExtendedPos findStargate(String address) {
        if (stargates.containsKey(address)) {
            return stargates.get(address);
        }

        for (Map.Entry<String, ExtendedPos> entry : stargates.entrySet()) {
            if (entry.getKey().startsWith(address)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static ExtendedPos findStargateUniversally(MinecraftServer server, String address) {
        for (ServerWorld targetWorld : server.getWorlds()) {
            StargateWorldData data = StargateWorldData.get(targetWorld);
            if (data.exists(address)) {
                return data.findStargate(address);
            }
        }

        return null;
    }
}