package fr.azures04.sgcraftreborn.common.registries;

import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.registries.tiles.ComputerCraftInterfaceTileEntity;
import fr.azures04.sgcraftreborn.common.registries.tiles.RFPowerUnitTileEntity;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateBaseTileEntity;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateControllerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import java.util.ArrayList;
import java.util.List;

public class ModTilesEntities {

    public static TileEntityType<StargateBaseTileEntity> STARGATE_BASE_BLOCK;
    public static TileEntityType<StargateControllerTileEntity> STARGATE_CONTROLLER_BLOCK;
    public static TileEntityType<RFPowerUnitTileEntity> RF_POWER_UNIT_BLOCK;
    public static TileEntityType<ComputerCraftInterfaceTileEntity> COMPUTER_CRAFT_INTERFACE_BLOCK;

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
        RF_POWER_UNIT_BLOCK = register("rf_power_unit", TileEntityType.Builder.create(RFPowerUnitTileEntity::new));
        COMPUTER_CRAFT_INTERFACE_BLOCK = register("computer_craft_interface", TileEntityType.Builder.create(ComputerCraftInterfaceTileEntity::new));
    }
}