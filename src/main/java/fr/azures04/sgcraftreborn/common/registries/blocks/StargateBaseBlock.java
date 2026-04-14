package fr.azures04.sgcraftreborn.common.registries.blocks;

import fr.azures04.sgcraftreborn.SGCraftReborn;
import fr.azures04.sgcraftreborn.common.config.SGCraftRebornConfig;
import fr.azures04.sgcraftreborn.common.registries.structures.StargateStructure;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateBaseTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;

public class StargateBaseBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty INVISIBLE = BooleanProperty.create("invisible");

    public StargateBaseBlock(Properties properties) {
        super(properties);
        setDefaultState(this.stateContainer.getBaseState()
            .with(FACING, EnumFacing.NORTH)
            .with(INVISIBLE, false)
        );
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(IBlockState state, IBlockReader world) {
        return new StargateBaseTileEntity();
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockReader world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        builder.add(FACING);
        builder.add(INVISIBLE);
    }

    @Override
    public IBlockState getStateForPlacement(BlockItemUseContext context) {
        return (IBlockState) this.getDefaultState()
            .with(FACING, context.getNearestLookingDirection().getOpposite())
            .with(INVISIBLE, false);
    }

    @Override
    public void onBlockAdded(IBlockState state, World worldIn, BlockPos pos, IBlockState oldState) {
        if (!worldIn.isRemote) {
            StargateStructure.notifyNearbyBases(worldIn, pos);
        }
        super.onBlockAdded(state, worldIn, pos, oldState);
    }

    @Override
    public void onReplaced(IBlockState state, World worldIn, BlockPos pos, IBlockState newState, boolean isMoving) {
        if (!worldIn.isRemote) {
            if (state.getBlock() != newState.getBlock()) {
                StargateStructure.showStructure(worldIn, pos, state.get(FACING));
                StargateBaseTileEntity base = (StargateBaseTileEntity) worldIn.getTileEntity(pos);
                if (SGCraftRebornConfig.LOG_STARGATE_EVENTS.get()) {
                    if (base.isMerged()) {
                        SGCraftReborn.LOGGER.log(Level.INFO, String.format("STARGATE REMOVED DIM: %s, X: %d Y: %d Z: %d", worldIn.dimension.getType().getId(), pos.getX(), pos.getY(), pos.getZ()));
                    }
                }
            }
        }
        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) return true;

        if (hand == EnumHand.MAIN_HAND) {
            StargateBaseTileEntity base = (StargateBaseTileEntity) worldIn.getTileEntity(pos);

            if (base != null) {
                player.sendMessage(new TextComponentString("VORTEX STATE : " + base.getVortexState()));
                player.sendMessage(new TextComponentString("LOCAL ADDRESS : " + base.getAddress()));
                player.sendMessage(new TextComponentString("REMOTE ADDRESS : " + base.getDialledAddress()));
                player.sendMessage(new TextComponentString("TIMEOUT : " + base.timeout));
            }
        }

        return true;
    }
}
