package fr.azures04.sgcraftreborn.common.util.math;

import fr.azures04.sgcraftreborn.common.config.SGCraftRebornConfig;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateBaseTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Stargates {

    public static StargateBaseTileEntity searchNearbyStargate(World world, BlockPos pos) {
        AxisAlignedBB box = new AxisAlignedBB(pos).grow(
                SGCraftRebornConfig.LINK_RANGE_X.get(),
                SGCraftRebornConfig.LINK_RANGE_Y.get(),
                SGCraftRebornConfig.LINK_RANGE_Z.get()
        );

        for (BlockPos checkPos : BlockPos.getAllInBoxMutable(
                (int)box.minX, (int)box.minY, (int)box.minZ,
                (int)box.maxX, (int)box.maxY, (int)box.maxZ)) {

            TileEntity te = world.getTileEntity(checkPos);
            if (te instanceof StargateBaseTileEntity) {
                StargateBaseTileEntity gate = (StargateBaseTileEntity) te;
                if (gate.isMerged()) return gate;
            }
        }
        return null;
    }


}
