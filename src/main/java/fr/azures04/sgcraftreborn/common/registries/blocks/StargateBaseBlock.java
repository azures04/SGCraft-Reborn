package fr.azures04.sgcraftreborn.common.registries.blocks;

import fr.azures04.sgcraftreborn.SGCraftReborn;
import fr.azures04.sgcraftreborn.client.screens.StargateControllerScreen;
import fr.azures04.sgcraftreborn.common.config.SGCraftRebornConfig;
import fr.azures04.sgcraftreborn.common.registries.ModBlocks;
import fr.azures04.sgcraftreborn.common.registries.ModItems;
import fr.azures04.sgcraftreborn.common.registries.structures.StargateStructure;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateBaseTileEntity;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateControllerTileEntity;
import fr.azures04.sgcraftreborn.common.registries.tiles.states.StargateVortexState;
import fr.azures04.sgcraftreborn.common.util.math.ExtendedPos;
import net.minecraft.block.Block;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Fluids;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.*;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;

public class StargateBaseBlock extends Block implements ILiquidContainer, IBucketPickupHandler {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty INVISIBLE = BooleanProperty.create("invisible");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public StargateBaseBlock(Properties properties) {
        super(properties);
        setDefaultState(this.stateContainer.getBaseState()
            .with(FACING, EnumFacing.NORTH)
            .with(INVISIBLE, false)
            .with(WATERLOGGED, false)
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
        builder.add(FACING, INVISIBLE, WATERLOGGED);
    }

    @Override
    public IBlockState getStateForPlacement(BlockItemUseContext context) {
        return (IBlockState) this.getDefaultState()
            .with(FACING, context.getNearestLookingDirection().getOpposite())
            .with(INVISIBLE, false)
            .with(WATERLOGGED, false);
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
        if (state.getBlock() != newState.getBlock()) {

            if (!worldIn.isRemote) {
                StargateBaseTileEntity base = (StargateBaseTileEntity) worldIn.getTileEntity(pos);
                if (base != null) {
                    StargateStructure.showStructure(worldIn, pos, state.get(FACING));

                    if (SGCraftRebornConfig.LOG_STARGATE_EVENTS.get() && base.isMerged()) {
                        SGCraftReborn.LOGGER.log(Level.INFO, String.format("STARGATE REMOVED DIM: %s, X: %d Y: %d Z: %d", worldIn.dimension.getType().getId(), pos.getX(), pos.getY(), pos.getZ()));
                    }

                    if (base.hasChevronUpgrade()) {
                        InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(ModItems.STARGATE_CHEVRON_UPGRADE));
                        base.setHasChevronUpgrade(false);
                    }
                    if (base.hasIrisUpgrade()) {
                        InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(ModItems.STARGATE_IRIS_UPGRADE));
                        base.setHasIrisUpgrade(false);
                    }
                    for (int i = 0; i < base.getInventory().getSlots(); i++) {
                        ItemStack slotItem = base.getInventory().getStackInSlot(i);
                        InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), slotItem);
                    }
                }
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) return true;
        StargateBaseTileEntity base = (StargateBaseTileEntity) worldIn.getTileEntity(pos);

        if (base != null) {;
            if (player.isSneaking()) {
                if (player.getHeldItem(hand).getItem() == ModItems.STARGATE_CHEVRON_UPGRADE) {
                    if (!base.hasChevronUpgrade()) {
                        base.setHasChevronUpgrade(true);
                        if (!player.isCreative()) {
                            player.getHeldItem(hand).shrink(1);
                            return true;
                        }
                    }
                }
                if (player.getHeldItem(hand).getItem() == ModItems.STARGATE_IRIS_UPGRADE) {
                    if (!base.hasIrisUpgrade()) {
                        base.setHasIrisUpgrade(true);
                        if (!player.isCreative()) {
                            player.getHeldItem(hand).shrink(1);
                            return true;
                        }
                    }
                }
                if (player.getHeldItem(hand).getItem() == Items.WATER_BUCKET) {
                    return true;
                }
            } else {
                if (hand == EnumHand.MAIN_HAND) {
                    if (base.isMerged()) {
                        NetworkHooks.openGui((EntityPlayerMP) player, (IInteractionObject) base, pos);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (worldIn.isRemote) return;
        boolean isPowered = worldIn.isBlockPowered(pos);
        StargateBaseTileEntity base = (StargateBaseTileEntity) worldIn.getTileEntity(pos);
        if (base != null) {
            if (base.hasIrisUpgrade()) {
                base.setIrisDeployed(isPowered);
            }
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    @Override
    public int getOpacity(IBlockState state, IBlockReader worldIn, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean propagatesSkylightDown(IBlockState state, net.minecraft.world.IBlockReader reader, net.minecraft.util.math.BlockPos pos) {
        return true;
    }

    @Override
    public IFluidState getFluidState(IBlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public boolean canContainFluid(IBlockReader worldIn, BlockPos pos, IBlockState state, Fluid fluidIn) {
        return state.get(INVISIBLE) && fluidIn == Fluids.WATER;
    }

    @Override
    public boolean receiveFluid(IWorld worldIn, BlockPos pos, IBlockState state, IFluidState fluidStateIn) {
        if (!state.get(INVISIBLE)) {
            return false;
        }

        if (!state.get(WATERLOGGED) && fluidStateIn.getFluid() == Fluids.WATER) {
            if (!worldIn.isRemote()) {
                worldIn.setBlockState(pos, state.with(WATERLOGGED, true), 3);
                worldIn.getPendingFluidTicks().scheduleTick(pos, fluidStateIn.getFluid(), fluidStateIn.getFluid().getTickRate(worldIn));
            }
            return true;
        }
        return false;
    }

    @Override
    public Fluid pickupFluid(IWorld worldIn, BlockPos pos, IBlockState state) {
        if (state.get(WATERLOGGED)) {
            worldIn.setBlockState(pos, state.with(WATERLOGGED, false), 3);
            return Fluids.WATER;
        }
        return Fluids.EMPTY;
    }


    @Override
    public void onExplosionDestroy(World worldIn, BlockPos pos, Explosion explosionIn) {
        if (!worldIn.isRemote) {
            StargateBaseTileEntity base = (StargateBaseTileEntity) worldIn.getTileEntity(pos);
            if (base != null && base.getVortexState() != StargateVortexState.IDLE) {

                int radius = SGCraftRebornConfig.EXPLOSION_RADIUS.get();

                if (radius > 0) {
                    boolean causesFire = SGCraftRebornConfig.EXPLOSION_FLAME.get();

                    worldIn.createExplosion(null, pos.getX() + 0.5, pos.getY() + 2.5, pos.getZ() + 0.5, (float)radius, causesFire);
                }
            }
        }
    }

    @Override
    public void onBlockExploded(IBlockState state, World world, BlockPos pos, Explosion explosion) {
        super.onBlockExploded(state, world, pos, explosion);
    }
}
