package fr.azures04.sgcraftreborn.registries.blocks;

import fr.azures04.sgcraftreborn.client.models.ISpecialItemRenderer;
import fr.azures04.sgcraftreborn.client.models.tiles.items.StargateControllerISTER;
import fr.azures04.sgcraftreborn.registries.blocks.states.StargateControllerStatus;
import fr.azures04.sgcraftreborn.registries.tiles.StargateControllerTileEntity;
import fr.azures04.sgcraftreborn.registries.world.StargateAddressing;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
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

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class StargateControllerBlock extends Block implements ISpecialItemRenderer {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<StargateControllerStatus> STATUS = EnumProperty.create("status", StargateControllerStatus.class);

    public StargateControllerBlock(Properties properties) {
        super(properties);
        setDefaultState(this.stateContainer.getBaseState()
            .with(FACING, EnumFacing.NORTH)
            .with(STATUS, StargateControllerStatus.UNLINKED)
        );
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(IBlockState state, IBlockReader world) {
        return new StargateControllerTileEntity();
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
        builder.add(STATUS);
    }

    @Override
    public IBlockState getStateForPlacement(BlockItemUseContext context) {
        return (IBlockState) this.getDefaultState()
            .with(FACING, context.getPlacementHorizontalFacing())
            .with(STATUS, StargateControllerStatus.UNLINKED);
    }

    @Override
    public Callable<TileEntityItemStackRenderer> getISTER() {
        return StargateControllerISTER::new;
    }

    @Override
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) return false;
        StargateControllerTileEntity controller = (StargateControllerTileEntity) worldIn.getTileEntity(pos);
        player.sendMessage(new TextComponentString(StargateAddressing.formatAddress(controller.getLinkedStargateTE(worldIn).getAddress())));
        return true;
    }

    @Override
    public void onBlockAdded(IBlockState state, World worldIn, BlockPos pos, IBlockState oldState) {
        StargateControllerTileEntity controller = (StargateControllerTileEntity) worldIn.getTileEntity(pos);
        controller.getLinkedStargateTE(worldIn);
    }
}
