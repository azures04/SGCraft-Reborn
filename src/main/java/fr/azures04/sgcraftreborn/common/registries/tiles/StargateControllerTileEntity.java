package fr.azures04.sgcraftreborn.common.registries.tiles;

import fr.azures04.sgcraftreborn.SGCraftReborn;
import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.config.SGCraftRebornConfig;
import fr.azures04.sgcraftreborn.common.inventories.StargateControllerFuelContainer;
import fr.azures04.sgcraftreborn.common.registries.ModTilesEntities;
import fr.azures04.sgcraftreborn.common.registries.blocks.StargateControllerBlock;
import fr.azures04.sgcraftreborn.common.registries.blocks.states.StargateControllerStatus;
import fr.azures04.sgcraftreborn.common.registries.tiles.states.StargateVortexState;
import fr.azures04.sgcraftreborn.common.world.StargateAddressing;
import fr.azures04.sgcraftreborn.common.world.data.StargateWorldData;
import fr.azures04.sgcraftreborn.common.util.math.ExtendedPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class StargateControllerTileEntity extends TileEntity implements IInteractionObject {

    private double fuelLevel;
    private ExtendedPos linkedStargate;
    private final ItemStackHandler inventory = new ItemStackHandler(4);
    private final LazyOptional<IItemHandler> inventoryHolder = LazyOptional.of(() -> inventory);

    public StargateControllerTileEntity() {
        super(ModTilesEntities.STARGATE_CONTROLLER_BLOCK);
    }

    public StargateControllerTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public ExtendedPos getLinkedStargate() {
        return linkedStargate;
    }

    public double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(double fuelLevel) {
        this.fuelLevel = fuelLevel;
        markDirty();
    }

    public void setLinkedStargate(ExtendedPos linkedStargate) {
        this.linkedStargate = linkedStargate;
        markDirty();
    }

    private StargateBaseTileEntity searchNearbyStargate(World world) {
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

    private void linkToStargate(StargateBaseTileEntity gate, World world) {
        if (gate.getControllerPos() != null) {
            TileEntity existing = world.getTileEntity(gate.getControllerPos());
            if (existing instanceof StargateControllerTileEntity) {
                return;
            }
        }
        setLinkedStargate(new ExtendedPos(gate.getPos(), world.getDimension().getType().getId()));
        gate.setControllerPos(new ExtendedPos(pos, world.getDimension().getType().getId()));
        IBlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(StargateControllerBlock.STATUS, StargateControllerStatus.LINKED), 3);
        markDirty();
    }

    public StargateBaseTileEntity getLinkedStargateTE(World world) {
        if (linkedStargate != null) {
            TileEntity te = world.getTileEntity(linkedStargate);
            if (te instanceof StargateBaseTileEntity && ((StargateBaseTileEntity) te).isMerged()) {
                return (StargateBaseTileEntity) te;
            }
            unlink();
        }
        StargateBaseTileEntity gate = searchNearbyStargate(world);
        if (gate != null) linkToStargate(gate, world);
        return gate;
    }

    public void unlink() {
        setLinkedStargate(null);
        IBlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(StargateControllerBlock.STATUS, StargateControllerStatus.UNLINKED), 3);
        markDirty();
    }

    @Override
    public void read(NBTTagCompound compound) {
        super.read(compound);
        fuelLevel = compound.getDouble("fuelLevel");
        if (compound.contains("connectedX", 3)) {
            linkedStargate = new ExtendedPos(
                compound.getInt("connectedX"),
                compound.getInt("connectedY"),
                compound.getInt("connectedZ"),
                compound.getInt("connectedD")
            );
        }
    }

    @Override
    public NBTTagCompound write(NBTTagCompound compound) {
        compound.setDouble("fuelLevel", fuelLevel);
        if (linkedStargate != null) {
            compound.setInt("connectedX", linkedStargate.getX());
            compound.setInt("connectedY", linkedStargate.getY());
            compound.setInt("connectedZ", linkedStargate.getZ());
            compound.setInt("connectedD", linkedStargate.getDimension());
        }
        return super.write(compound);
    }

    @Override
    public void onLoad() {
        if (!world.isRemote) {
            getLinkedStargateTE(world);
        }
    }

    public String dial(String address) {
        if (!StargateAddressing.isValidAddress(address)) {
            throw StargateAddressing.StargateAddressingException.INVALID_ADDRESS;
        }

        StargateBaseTileEntity localGate = getLinkedStargateTE(world);
        if (localGate == null) {
            throw StargateAddressing.StargateAddressingException.NOT_LINKED;
        }
        if (!localGate.isMerged()) {
            throw StargateAddressing.StargateAddressingException.NOT_MERGED;
        }

        ExtendedPos remotePos = StargateWorldData.get(world).findStargate(address);

        if (address.length() == 9) {
            if (!localGate.hasChevronUpgrade()) {
                throw StargateAddressing.StargateAddressingException.MISSING_CHEVRON_UPGRADE;
            }
            remotePos = StargateWorldData.findStargateUniversally(Objects.requireNonNull(world.getServer()), address);
        }

        if (remotePos == null) {
            throw StargateAddressing.StargateAddressingException.NOT_AT_THIS_ADDRESS;
        }

        MinecraftServer server = world.getServer();
        WorldServer remoteWorld = server.getWorld(DimensionType.getById(remotePos.getDimension()));
        TileEntity remoteTe = remoteWorld.getTileEntity(remotePos.getPos());
        if (!(remoteTe instanceof StargateBaseTileEntity)) {
            throw StargateAddressing.StargateAddressingException.GATE_NOT_FOUND;
        }

        StargateBaseTileEntity remoteGate = (StargateBaseTileEntity) remoteTe;
        if (!remoteGate.isMerged()) {
            throw StargateAddressing.StargateAddressingException.NOT_MERGED;
        }

        if (localGate.getVortexState() != StargateVortexState.IDLE) {
            throw StargateAddressing.StargateAddressingException.GATE_BUSY;
        }
        if (remoteGate.getVortexState() != StargateVortexState.IDLE) {
            throw StargateAddressing.StargateAddressingException.GATE_BUSY;
        }

        if (address.length() == 9 && !remoteGate.hasChevronUpgrade()) {
            throw StargateAddressing.StargateAddressingException.MISSING_CHEVRON_UPGRADE;
        }

        localGate.startDialing(address, remotePos, true);

        return null;
    }

    public void onStargateStateChanged(StargateVortexState gateState) {
        if (world == null || world.isRemote) return;
        IBlockState currentState = world.getBlockState(this.pos);

        if (currentState.getBlock() instanceof StargateControllerBlock) {
            if (gateState == StargateVortexState.IDLE) {
                world.setBlockState(this.pos, currentState.with(StargateControllerBlock.STATUS, StargateControllerStatus.LINKED), 3);
            } else {
                world.setBlockState(this.pos, currentState.with(StargateControllerBlock.STATUS, StargateControllerStatus.ACTIVATED), 3);
            }
        }
    }

    @Override
    public void remove() {
        if (linkedStargate != null) {
            TileEntity base = world.getTileEntity(linkedStargate);
            if (base instanceof StargateBaseTileEntity) {
                ((StargateBaseTileEntity) base).setControllerPos(null);
            }
        }
        super.remove();
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.write(nbt);
        return new SPacketUpdateTileEntity(this.pos, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.read(pkt.getNbtCompound());
        super.onDataPacket(net, pkt);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.write(new NBTTagCompound());
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new StargateControllerFuelContainer(playerInventory, this.pos);
    }

    @Override
    public String getGuiID() {
        return Constants.MOD_ID + ":controller_fuel";
    }

    @Override
    public ITextComponent getName() {
        return new TextComponentTranslation("container.sgcraftreborn.dhd");
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {
        return null;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return inventoryHolder.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void invalidateCaps() {
        super.invalidateCaps();
        inventoryHolder.invalidate();
    }

}
