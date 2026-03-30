package fr.azures04.sgcraftreborn.registries;

import fr.azures04.sgcraftreborn.Constants;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import fr.azures04.sgcraftreborn.registries.tiles.*;

import java.util.ArrayList;
import java.util.List;

public class ModTilesEntities {

    public static TileEntityType<StargateBaseTileEntity> STARGATE_BASE_BLOCK;
    public static TileEntityType<StargateControllerTileEntity> STARGATE_CONTROLLER_BLOCK;

    public static final List<TileEntityType<?>> TILE_ENTITY_TYPES = new ArrayList<>();

    private static <T extends TileEntity> TileEntityType<T> register(String name, TileEntityType.Builder<T> builder) {
        TileEntityType<T> type = builder.build(null);
        type.setRegistryName(Constants.MOD_ID, name);
        TILE_ENTITY_TYPES.add(type);
        return type;
    }

    static {
        STARGATE_BASE_BLOCK = register("stargate_base", TileEntityType.Builder.create(StargateBaseTileEntity::new));
        STARGATE_CONTROLLER_BLOCK = register("stargate_controller", TileEntityType.Builder.create(StargateControllerTileEntity::new));
    }
}