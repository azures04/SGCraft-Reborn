package fr.azures04.sgcraftreborn.common.registries.blocks;

import fr.azures04.sgcraftreborn.client.models.ISpecialItemRenderer;
import fr.azures04.sgcraftreborn.client.models.tiles.items.StargateControllerISTER;
import fr.azures04.sgcraftreborn.client.screens.StargateControllerScreen;
import fr.azures04.sgcraftreborn.common.registries.blocks.states.StargateControllerStatus;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateBaseTileEntity;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateControllerTileEntity;
import fr.azures04.sgcraftreborn.common.util.math.ExtendedPos;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Fluids;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;

public class StargateControllerBlock extends Block implements ISpecialItemRenderer, ILiquidContainer, IBucketPickupHandler {
    public static final DirectionProperty FACING;
    public static final EnumProperty <StargateControllerStatus> STATUS;
    public static final BooleanProperty WATERLOGGED;

    public StargateControllerBlock(Block.Properties properties) {
        super(properties);
        this.setDefaultState((IBlockState)((IBlockState)((IBlockState)((IBlockState) this.stateContainer.getBaseState())
                .with(FACING, EnumFacing.NORTH)).with(STATUS, StargateControllerStatus.UNLINKED)).with(WATERLOGGED, false));
    }

    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    public TileEntity createTileEntity(IBlockState state, IBlockReader world) {
        return new StargateControllerTileEntity();
    }

    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    public boolean isFullCube(IBlockState state) {
        return false;
    }

    public boolean isNormalCube(IBlockState state, IBlockReader world, BlockPos pos) {
        return false;
    }

    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    protected void fillStateContainer(StateContainer.Builder < Block, IBlockState > builder) {
        builder.add(new IProperty[] {
                FACING,
                STATUS,
                WATERLOGGED
        });
    }

    public IBlockState getStateForPlacement(BlockItemUseContext context) {
        return (IBlockState)((IBlockState)((IBlockState) this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing())).with(STATUS, StargateControllerStatus.UNLINKED)).with(WATERLOGGED, false);
    }

    public Callable < TileEntityItemStackRenderer > getISTER() {
        return StargateControllerISTER::new;
    }

    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote && side == EnumFacing.UP) {
            StargateControllerTileEntity controller = (StargateControllerTileEntity) worldIn.getTileEntity(pos);
            if (controller == null || !controller.isLinked()) return true;

            ExtendedPos linkedPos = controller.getLinkedStargate();
            if (linkedPos == null) return true;

            TileEntity baseTe = worldIn.getTileEntity(linkedPos);
            boolean hasChevron = (baseTe instanceof StargateBaseTileEntity)
                    && ((StargateBaseTileEntity) baseTe).hasChevronUpgrade();

            Minecraft.getInstance().displayGuiScreen(new StargateControllerScreen(new ExtendedPos(pos, worldIn.dimension.getType().getId()), (StargateControllerStatus) state.get(STATUS), hasChevron));
            return true;
        } else {
            if (!worldIn.isRemote) {
                EnumFacing blockFacing = (EnumFacing) state.get(FACING);
                if (side == blockFacing.getOpposite()) {
                    TileEntity te = worldIn.getTileEntity(pos);
                    if (te instanceof StargateControllerTileEntity) {
                        NetworkHooks.openGui((EntityPlayerMP) player, (IInteractionObject) te, pos);
                        return true;
                    }
                }
            }

            return true;
        }
    }

    public void onBlockAdded(IBlockState state, World worldIn, BlockPos pos, IBlockState oldState) {
        if (!worldIn.isRemote) {
            StargateControllerTileEntity controller = (StargateControllerTileEntity) worldIn.getTileEntity(pos);
            controller.getLinkedStargateTE(worldIn);
        }
    }

    @Override
    public void onReplaced(IBlockState state, World worldIn, BlockPos pos, IBlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (!worldIn.isRemote) {
                TileEntity te = worldIn.getTileEntity(pos);
                if (te instanceof StargateControllerTileEntity) {
                    StargateControllerTileEntity controller = (StargateControllerTileEntity) te;
                    controller.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent((inventory) -> {
                        for (int i = 0; i < inventory.getSlots(); ++i) {
                            ItemStack stack = inventory.getStackInSlot(i);
                            if (!stack.isEmpty()) {
                                InventoryHelper.spawnItemStack(worldIn, (double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), stack);
                            }
                        }
                    });
                }
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    public int getOpacity(IBlockState state, IBlockReader worldIn, BlockPos pos) {
        return 0;
    }

    public boolean propagatesSkylightDown(IBlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    public IFluidState getFluidState(IBlockState state) {
        return (Boolean) state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    public boolean canContainFluid(IBlockReader worldIn, BlockPos pos, IBlockState state, Fluid fluidIn) {
        return true;
    }

    public boolean receiveFluid(IWorld worldIn, BlockPos pos, IBlockState state, IFluidState fluidStateIn) {
        if (!(Boolean) state.get(WATERLOGGED) && fluidStateIn.getFluid() == Fluids.WATER) {
            if (!worldIn.isRemote()) {
                worldIn.setBlockState(pos, (IBlockState) state.with(WATERLOGGED, true), 3);
                worldIn.getPendingFluidTicks().scheduleTick(pos, fluidStateIn.getFluid(), fluidStateIn.getFluid().getTickRate(worldIn));
            }

            return true;
        } else {
            return false;
        }
    }

    public Fluid pickupFluid(IWorld worldIn, BlockPos pos, IBlockState state) {
        if ((Boolean) state.get(WATERLOGGED)) {
            worldIn.setBlockState(pos, (IBlockState) state.with(WATERLOGGED, false), 3);
            return Fluids.WATER;
        } else {
            return Fluids.EMPTY;
        }
    }

    static {
        FACING = BlockStateProperties.HORIZONTAL_FACING;
        STATUS = EnumProperty.create("status", StargateControllerStatus.class);
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
    }
}