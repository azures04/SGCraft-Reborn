package fr.azures04.sgcraftreborn.common.world.data;

import fr.azures04.sgcraftreborn.common.util.math.ExtendedPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.storage.WorldSavedDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class StargateWorldData extends WorldSavedData {

    public static final String NAME = "sgcraftreborn_stargates";
    private Map<String, ExtendedPos> stargates = new TreeMap<>();

    public StargateWorldData() {
        super(NAME);
    }

    public StargateWorldData(String name) {
        super(name);
    }

    @Override
    public void read(NBTTagCompound nbt) {
        stargates.clear();
        NBTTagList list = nbt.getList("stargates", 10);
        for (int i = 0; i < list.size(); i++) {
            NBTTagCompound entry = list.getCompound(i);
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
    public NBTTagCompound write(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<String, ExtendedPos> entry : stargates.entrySet()) {
            NBTTagCompound tag = new NBTTagCompound();
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
        WorldSavedDataStorage storage = ((WorldServer) world).getSavedDataStorage();
        DimensionType dimension = world.getDimension().getType();
        StargateWorldData data = storage.get(dimension, StargateWorldData::new, NAME);
        if (data == null) {
            data = new StargateWorldData();
            storage.set(dimension, NAME, data);
        }
        return data;
    }

    public boolean exists(String address) {
        if (stargates.containsKey(address)) {
            return true;
        }
        return !((TreeMap<String, ExtendedPos>) stargates).subMap(address, address + "\uffff").isEmpty();
    }

    public ExtendedPos findStargate(String address) {
        if (stargates.containsKey(address)) {
            return stargates.get(address);
        }

        SortedMap<String, ExtendedPos> results = ((TreeMap<String, ExtendedPos>) stargates).subMap(address, address + "\uffff");
        if (!results.isEmpty()) {
            return results.get(results.firstKey());
        }
        return null;
    }

    public static ExtendedPos findStargateUniversally(MinecraftServer server, String address) {
        for (WorldServer targetWorld : server.getWorlds()) {
            StargateWorldData data = StargateWorldData.get(targetWorld);
            if (data.exists(address)) {
                return data.findStargate(address);
            }
        }

        return null;
    }
}