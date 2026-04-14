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
    public NBTTagCompound write(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<String, ExtendedPos> entry : stargates.entrySet()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("a", entry.getKey());
            tag.setInt("x", entry.getValue().getX());
            tag.setInt("y", entry.getValue().getY());
            tag.setInt("z", entry.getValue().getZ());
            tag.setInt("d", entry.getValue().getDimension());
            list.add(tag);
        }
        nbt.setTag("stargates", list);
        return nbt;
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
        WorldSavedDataStorage storage = ((WorldServer) world).getMapStorage();
        DimensionType dimension = world.getDimension().getType();
        StargateWorldData data = storage.func_212426_a(dimension, StargateWorldData::new, NAME);
        if (data == null) {
            data = new StargateWorldData();
            storage.func_212424_a(dimension, NAME, data);
        }
        return data;
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
        for (WorldServer targetWorld : server.func_212370_w()) {
            StargateWorldData data = StargateWorldData.get(targetWorld);
            if (data.exists(address)) {
                return data.findStargate(address);
            }
        }

        return null;
    }
}