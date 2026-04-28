package fr.azures04.sgcraftreborn.common.registries.tiles;

import fr.azures04.sgcraftreborn.SGCraftReborn;
import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.api.StargateAbstractAPI;
import fr.azures04.sgcraftreborn.common.config.SGCraftRebornConfig;
import fr.azures04.sgcraftreborn.common.containers.StargateBaseCamouflageContainer;
import fr.azures04.sgcraftreborn.common.registries.ModSounds;
import fr.azures04.sgcraftreborn.common.registries.ModTilesEntities;
import fr.azures04.sgcraftreborn.common.registries.blocks.StargateBaseBlock;
import fr.azures04.sgcraftreborn.common.registries.structures.StargateStructure;
import fr.azures04.sgcraftreborn.common.registries.tiles.states.StargateIrisState;
import fr.azures04.sgcraftreborn.common.registries.tiles.states.StargateVortexState;
import fr.azures04.sgcraftreborn.common.util.math.ExtendedPos;
import fr.azures04.sgcraftreborn.common.world.StargateAddressing;
import fr.azures04.sgcraftreborn.common.world.StargateTeleporter;
import fr.azures04.sgcraftreborn.common.world.data.StargateWorldData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class StargateBaseTileEntity extends TileEntity implements ITickable, IInteractionObject {

    private transient Set<StargateAbstractAPI> computerAdapters = Collections.newSetFromMap(new WeakHashMap<>());

    private static final float[][] CHEVRON_ANGLES = {
            { 45f, 45f, 40f },
            { 36f, 33f, 30f }
    };

    static final int DIALLING_TIME = 40;
    static final int INTER_DIALLING_TIME = 10;
    static final int TRANSIENT_DURATION = 20;

    private boolean isMerged, isInitiator, hasChevronUpgrade, hasIrisUpgrade, isClientInitialized;
    private ExtendedPos connectedLoc;
    private BlockPos controllerPos;
    private String dialledAddress = "", address = "";
    private double energyInBuffer, ringAngle, lastRingAngle;
    private int numEngagedChevrons, irisPhase, lastIrisPhase;

    private StargateIrisState irisState = StargateIrisState.OPEN;
    private StargateVortexState vortexState = StargateVortexState.IDLE;
    public int timeout, irisTimeout;

    public ItemStackHandler getInventory() {
        return inventory;
    }

    private final ItemStackHandler inventory = new ItemStackHandler(5);
    private final LazyOptional<IItemHandler> inventoryHolder = LazyOptional.of(() -> inventory);

    public final static int ehGridRadialSize = 5;
    public final static int ehGridPolarSize = 32;
    private double[][][] ehGrid;
    private final Random random = new Random();
    private double distanceFactor = 1.0;

    public StargateBaseTileEntity() {
        super(ModTilesEntities.STARGATE_BASE_BLOCK);
    }

    public StargateBaseTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public NBTTagCompound write(NBTTagCompound compound) {
        compound.putBoolean("isMerged", isMerged);
        compound.putBoolean("isInitiator", isInitiator);
        compound.putString("dialledAddress", dialledAddress == null ? "" : dialledAddress);
        compound.putString("address", address == null ? "" : address);
        compound.putDouble("energyInBuffer", energyInBuffer);
        compound.putInt("numEngagedChevrons", numEngagedChevrons);
        compound.putBoolean("hasChevronUpgrade", hasChevronUpgrade);
        compound.putBoolean("hasIrisUpgrade", hasIrisUpgrade);
        compound.putInt("irisState", irisState != null ? irisState.ordinal() : 0);
        compound.putInt("irisTimeout", irisTimeout);
        compound.putInt("vortexState", vortexState != null ? vortexState.ordinal() : 0);
        compound.putInt("timeout", timeout);

        if (connectedLoc != null) {
            compound.putInt("connectedX", connectedLoc.getX());
            compound.putInt("connectedY", connectedLoc.getY());
            compound.putInt("connectedZ", connectedLoc.getZ());
            compound.putInt("connectedD", connectedLoc.getDimension());
        }
        if (controllerPos != null) {
            compound.putInt("controllerX", controllerPos.getX());
            compound.putInt("controllerY", controllerPos.getY());
            compound.putInt("controllerZ", controllerPos.getZ());
        }
        compound.put("inventory", inventory.serializeNBT());
        compound.putDouble("distanceFactor", distanceFactor);
        return super.write(compound);
    }

    @Override
    public void read(NBTTagCompound compound) {
        super.read(compound);
        isMerged = compound.getBoolean("isMerged");
        isInitiator = compound.getBoolean("isInitiator");
        dialledAddress = compound.getString("dialledAddress");
        address = compound.getString("address");
        energyInBuffer = compound.getDouble("energyInBuffer");
        numEngagedChevrons = compound.getInt("numEngagedChevrons");
        hasChevronUpgrade = compound.getBoolean("hasChevronUpgrade");
        hasIrisUpgrade = compound.getBoolean("hasIrisUpgrade");
        irisState = StargateIrisState.values()[compound.getInt("irisState")];
        irisTimeout = compound.getInt("irisTimeout");
        vortexState = StargateVortexState.values()[compound.getInt("vortexState")];
        timeout = compound.getInt("timeout");

        if (compound.contains("connectedX", 3)) {
            connectedLoc = new ExtendedPos(compound.getInt("connectedX"), compound.getInt("connectedY"), compound.getInt("connectedZ"), compound.getInt("connectedD"));
        }
        if (compound.contains("controllerX", 3)) {
            controllerPos = new BlockPos(compound.getInt("controllerX"), compound.getInt("controllerY"), compound.getInt("controllerZ"));
        }

        if (compound.contains("inventory", 10)) {
            inventory.deserializeNBT(compound.getCompound("inventory"));
        }
        distanceFactor = compound.contains("distanceFactor", 6) ? compound.getDouble("distanceFactor") : 1.0;
    }

    public double getEnergyToOpen() {
        return (double) SGCraftRebornConfig.ENERGY_PER_FUEL_ITEM.get() / SGCraftRebornConfig.GATE_OPENINGS_PER_FUEL_ITEM.get();
    }

    public double getEnergyUsePerTick() {
        return (double) SGCraftRebornConfig.ENERGY_PER_FUEL_ITEM.get() / (SGCraftRebornConfig.MINUTES_OPEN_PER_FUEL_ITEM.get() * 60 * 20);
    }

    public static double distanceFactorForCoordDifference(TileEntity te1, TileEntity te2) {
        double dx = te1.getPos().getX() - te2.getPos().getX();
        double dz = te1.getPos().getZ() - te2.getPos().getZ();
        double d = Math.sqrt(dx * dx + dz * dz);

        double ld = Math.log(0.05 * d + 1);
        double lm = Math.log(0.05 * 16 * 1000000);
        double lr = ld / lm;
        double f = 1 + 14 * SGCraftRebornConfig.DISTANCE_FACTOR_MULTIPLIER.get() * lr * lr;

        if (te1.getWorld().getDimension().getType().getId() != te2.getWorld().getDimension().getType().getId()) {
            f *= SGCraftRebornConfig.INTER_DIMENSION_MULTIPLIER.get();
        }
        return f;
    }

    public boolean energyIsAvailable(double amount) {
        double available = energyInBuffer;

        if (controllerPos != null && world != null) {
            TileEntity te = world.getTileEntity(controllerPos);
            if (te instanceof StargateControllerTileEntity) {
                available += ((StargateControllerTileEntity) te).getAvailableEnergy();
            }
        }

        for (RFPowerUnitTileEntity powerUnit : getPowerUnits()) {
            available += powerUnit.getAvailableSGEnergy();
        }

        return available >= amount;
    }

    public boolean useEnergy(double amount) {
        if (amount <= energyInBuffer) {
            energyInBuffer -= amount;
            markDirty();
            return true;
        }

        double energyAvailable = energyInBuffer;
        StargateControllerTileEntity controller = null;

        if (controllerPos != null && world != null) {
            TileEntity te = world.getTileEntity(controllerPos);
            if (te instanceof StargateControllerTileEntity) {
                controller = (StargateControllerTileEntity) te;
                energyAvailable += controller.getAvailableEnergy();
            }
        }

        List<RFPowerUnitTileEntity> powerUnits = getPowerUnits();
        for (RFPowerUnitTileEntity powerUnit : powerUnits) {
            energyAvailable += powerUnit.getAvailableSGEnergy();
        }

        if (amount > energyAvailable) {
            return false;
        }

        double energyRequired = amount - energyInBuffer;
        double maxBuffer = SGCraftRebornConfig.MAX_ENERGY_BUFFER.get();

        for (RFPowerUnitTileEntity powerUnit : powerUnits) {
            if (energyRequired <= 0) break;

            double spaceInBuffer = maxBuffer - energyInBuffer;
            if (spaceInBuffer <= 0) break;

            double toExtract = Math.min(energyRequired, spaceInBuffer);
            double drawn = powerUnit.extractSGEnergy(toExtract);
            energyInBuffer += drawn;
            energyRequired -= drawn;
        }

        if (controller != null && energyRequired > 0) {
            double drawn = controller.drawEnergy(energyRequired);
            energyInBuffer += drawn;
        }

        if (amount <= energyInBuffer) {
            energyInBuffer -= amount;
            markDirty();
            return true;
        }

        return false;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, write(new NBTTagCompound()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        StargateVortexState oldState = this.vortexState;
        read(pkt.getNbtCompound());

        if (isMerged && this.vortexState != oldState) {
            if (this.vortexState == StargateVortexState.OPENING) {
                initiateOpeningTransient();
            } else if (this.vortexState == StargateVortexState.CLOSING) {
                initiateClosingTransient();
            }
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return write(new NBTTagCompound());
    }

    public void sync() {
        markDirty();
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    public void tick() {
        if (world.isRemote) {
            int targetPhase = (irisState == StargateIrisState.OPEN || irisState == StargateIrisState.OPENING) ? 60 : 0;
            if (!isClientInitialized) {
                irisPhase = targetPhase;
                lastIrisPhase = targetPhase;
                isClientInitialized = true;
            }

            lastRingAngle = ringAngle;
            lastIrisPhase = irisPhase;

            if (vortexState == StargateVortexState.DIALLING && timeout > 0 && dialledAddress != null) {
                timeout--;
                int cycleTime = DIALLING_TIME + INTER_DIALLING_TIME;
                int requiredChevrons = dialledAddress.length();
                int elapsedTime = (requiredChevrons * cycleTime + TRANSIENT_DURATION) - timeout;

                if (elapsedTime <= requiredChevrons * cycleTime && (elapsedTime % cycleTime) < DIALLING_TIME) {
                    ringAngle -= 5.0;
                    if (ringAngle < 0) ringAngle += 360.0;
                }
            }

            if (irisPhase < targetPhase) irisPhase++;
            if (irisPhase > targetPhase) irisPhase--;

            if (vortexState == StargateVortexState.ACTIVE || vortexState == StargateVortexState.OPENING || vortexState == StargateVortexState.CLOSING) {
                applyRandomImpulse();
                updateEventHorizon();
            }
            return;
        }

        if (!isMerged()) {
            if (vortexState != StargateVortexState.IDLE) disconnect();
            return;
        }

        if (irisTimeout > 0 && --irisTimeout <= 0) {
            StargateIrisState newState = (irisState == StargateIrisState.CLOSING) ? StargateIrisState.CLOSED : StargateIrisState.OPEN;
            fireComputerEvent("sgIrisStateChange", irisState, newState);
            irisState = newState;
            sync();
        }

        switch (vortexState) {
            case DIALLING:
                if (this.timeout > 0 && this.dialledAddress != null) {
                    this.timeout--;

                    int cycleTime = DIALLING_TIME + INTER_DIALLING_TIME;
                    int requiredChevrons = this.dialledAddress.length();
                    int totalDialTime = requiredChevrons * cycleTime + TRANSIENT_DURATION;
                    int elapsedTime = totalDialTime - this.timeout;

                    if (elapsedTime <= requiredChevrons * cycleTime && (elapsedTime % cycleTime) == DIALLING_TIME) {
                        this.numEngagedChevrons++;
                        float vol = SGCraftRebornConfig.SOUND_VOLUME.get().floatValue();
                        if (this.numEngagedChevrons != requiredChevrons) {
                            if (requiredChevrons == 9) {
                                world.playSound(null, pos, ModSounds.SG_DIAL9, SoundCategory.BLOCKS, vol, 1.0F);
                            } else {
                                world.playSound(null, pos, ModSounds.SG_DIAL7, SoundCategory.BLOCKS, vol, 1.0F);
                            }
                        }
                        String symbol = String.valueOf(this.dialledAddress.charAt(this.numEngagedChevrons - 1));
                        fireComputerEvent("sgChevronEngaged", this.numEngagedChevrons, symbol);
                        this.sync();
                    }

                    if (this.timeout == TRANSIENT_DURATION) {
                        fireComputerEvent("sgStargateStateChange", vortexState, StargateVortexState.OPENING);
                        vortexState = StargateVortexState.OPENING;
                        sync();
                    }
                } else {
                    disconnect();
                }
                break;

            case OPENING:
                if (this.timeout > 0) {
                    this.timeout--;
                    if (this.irisState == StargateIrisState.OPEN || this.irisState == StargateIrisState.OPENING) {
                        handleKawooshVaporization();
                    }
                } else {
                    numEngagedChevrons = dialledAddress != null ? dialledAddress.length() : 0;
                    fireComputerEvent("sgStargateStateChange", vortexState, StargateVortexState.ACTIVE);
                    vortexState = StargateVortexState.ACTIVE;
                    int openTime = SGCraftRebornConfig.SECONDS_TO_STAY_OPEN.get();
                    timeout = openTime > 0 ? openTime * 20 : 999999;
                    updateControllerBlockState();
                    sync();
                }
                break;

            case ACTIVE:
                if (timeout > 0) {
                    if (isInitiator) {
                        if (!useEnergy(getEnergyUsePerTick() * distanceFactor)) {
                            disconnect();
                            break;
                        }
                    }

                    timeout--;
                    if (irisState == StargateIrisState.OPEN) {
                        handleEntityTeleportation();
                    } else if (irisState == StargateIrisState.CLOSED) {
                        handleEntityVaporization();
                    }
                } else {
                    disconnect();
                }
                break;

            case CLOSING:
                fireComputerEvent("sgStargateStateChange", vortexState, StargateVortexState.IDLE);
                vortexState = StargateVortexState.IDLE;
                updateControllerBlockState();
                sync();
                break;

            default: break;
        }
    }

    public void startDialing(String targetAddress, ExtendedPos targetLocation, boolean isInitiator, double distFactor) {
        float vol = SGCraftRebornConfig.SOUND_VOLUME.get().floatValue();
        if (targetAddress == null || targetAddress.isEmpty() || getAddress().startsWith(targetAddress)) {
            world.playSound(null, pos, ModSounds.SG_ABORT, SoundCategory.BLOCKS, vol, 1.0F);
            disconnect();
            return;
        }

        StargateBaseTileEntity remoteGate = null;
        int requiredChevrons = targetAddress.length();

        if (isInitiator) {
            remoteGate = getRemoteGate(targetAddress);
            fireComputerEvent("sgDialOut", remoteGate);
            if (remoteGate == null || remoteGate.getVortexState() != StargateVortexState.IDLE) {
                disconnect();
                return;
            }

            if (world.getDimension().getType().getId() == remoteGate.getWorld().getDimension().getType().getId() && requiredChevrons > 7) {
                requiredChevrons = 7;
            }

            int maxChevrons = hasChevronUpgrade ? 9 : 7;
            if (requiredChevrons > maxChevrons) requiredChevrons = maxChevrons;
            targetAddress = targetAddress.substring(0, requiredChevrons);
        } else {
            remoteGate = getRemoteGate(targetAddress);
            fireComputerEvent("sgDialIn", remoteGate);
        }

        fireComputerEvent("sgStargateStateChange", vortexState, StargateVortexState.DIALLING);
        vortexState = StargateVortexState.DIALLING;
        dialledAddress = targetAddress;
        connectedLoc = targetLocation;
        this.isInitiator = isInitiator;
        this.distanceFactor = distFactor;
        numEngagedChevrons = 0;
        timeout = requiredChevrons * (DIALLING_TIME + INTER_DIALLING_TIME) + TRANSIENT_DURATION;

        updateControllerBlockState();
        sync();

        world.playSound(null, pos, ModSounds.SG_OPEN, SoundCategory.BLOCKS, vol, 1.0F);

        if (isInitiator && remoteGate != null) {
            String senderAddress = getAddress();
            if (senderAddress.length() > requiredChevrons) senderAddress = senderAddress.substring(0, requiredChevrons);
            remoteGate.startDialing(senderAddress, getExtendedPos(), false, distFactor);
        }
    }

    public void disconnect() {
        if (!isInitiator && !SGCraftRebornConfig.CLOSE_FROM_EITHER_END.get()) {
            return;
        }

        float vol = SGCraftRebornConfig.SOUND_VOLUME.get().floatValue();
        world.playSound(null, pos, ModSounds.SG_OPEN, SoundCategory.BLOCKS, vol, 1.0F);

        String targetAddress = dialledAddress;
        fireComputerEvent("sgStargateStateChange", vortexState, StargateVortexState.CLOSING);
        vortexState = StargateVortexState.CLOSING;
        connectedLoc = null;
        dialledAddress = "";
        isInitiator = false;
        numEngagedChevrons = 0;
        timeout = 0;

        updateControllerBlockState();
        sync();

        if (targetAddress != null && !targetAddress.isEmpty()) {
            StargateBaseTileEntity remoteGate = getRemoteGate(targetAddress);
            if (remoteGate != null && remoteGate.getVortexState() != StargateVortexState.CLOSING && remoteGate.getVortexState() != StargateVortexState.IDLE) {
                remoteGate.disconnect();
            }
        }
    }

    public void setIrisDeployed(boolean deploy) {
        float vol = SGCraftRebornConfig.SOUND_VOLUME.get().floatValue();
        if (deploy) {
            fireComputerEvent("sgIrisStateChange", irisState, StargateIrisState.CLOSING);
            irisState = StargateIrisState.CLOSING;
            world.playSound(null, pos, ModSounds.IRIS_CLOSE, SoundCategory.BLOCKS, vol, 1.0F);
            irisTimeout = 60;
            sync();
        } else if (!deploy) {
            fireComputerEvent("sgIrisStateChange", irisState, StargateIrisState.OPENING);
            irisState = StargateIrisState.OPENING;
            world.playSound(null, pos, ModSounds.IRIS_OPEN, SoundCategory.BLOCKS, vol, 1.0F);
            irisTimeout = 60;
            sync();
        }
    }

    public void setMerged(boolean merged) {
        if (isMerged != merged) {
            isMerged = merged;
            EnumFacing facing = getBlockState().get(StargateBaseBlock.FACING);

            if (!world.isRemote) {
                updateChunkLoading(merged);
                if (merged) {
                    StargateStructure.hideStructure(world, pos, facing);
                    notifyNearbyControllers();
                    notifyNearbyComputers();
                } else {
                    StargateStructure.showStructure(world, pos, facing);
                    if (controllerPos != null) {
                        TileEntity te = world.getTileEntity(controllerPos);
                        if (te instanceof StargateControllerTileEntity) ((StargateControllerTileEntity) te).unlink();
                        controllerPos = null;
                    }
                }
            }
            sync();
        }
    }

    private void handleEntityVaporization() {
        if (connectedLoc == null) return;
        AxisAlignedBB eventHorizonBox = getEventHorizonBoundingBox();

        float vol = SGCraftRebornConfig.SOUND_VOLUME.get().floatValue();
        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, eventHorizonBox)) {
            if (entity instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) entity;
                if (player.isCreative()) player.sendMessage(new TextComponentString("Iris closed!"));
                if (!SGCraftRebornConfig.PRESERVE_INVENTORY.get()) player.inventory.clear();
            }
            world.playSound(null, pos, ModSounds.IRIS_HIT, SoundCategory.BLOCKS, vol, 1.0F);
            entity.attackEntityFrom(DamageSource.FLY_INTO_WALL, Float.MAX_VALUE);
        }
    }

    private void handleEntityTeleportation() {
        if (connectedLoc == null) return;

        if (SGCraftRebornConfig.ONE_WAY_TRAVEL.get() && !isInitiator) {
            return;
        }

        StargateBaseTileEntity remoteGate = getRemoteGate(dialledAddress);

        if (remoteGate == null || remoteGate.getIrisState() != StargateIrisState.OPEN) {
            handleEntityVaporization();
            return;
        }

        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, getEventHorizonBoundingBox())) {
            if (entity.timeUntilPortal <= 0) teleportEntity(entity);
        }
    }

    private void teleportEntity(Entity entity) {
        if (connectedLoc == null || world.isRemote) return;
        MinecraftServer server = world.getServer();
        if (server == null) return;

        WorldServer targetWorld = server.getWorld(Objects.requireNonNull(DimensionType.getById(connectedLoc.getDimension())));
        if (targetWorld == null) return;

        BlockPos targetBasePos = new BlockPos(connectedLoc.getX(), connectedLoc.getY(), connectedLoc.getZ());
        double destX = targetBasePos.getX() + 0.5, destY = targetBasePos.getY() + 1.0, destZ = targetBasePos.getZ() + 0.5;
        float targetYaw = entity.rotationYaw;

        IBlockState targetState = targetWorld.getBlockState(targetBasePos);
        if (targetState.getBlock() instanceof StargateBaseBlock) {
            EnumFacing targetFacing = targetState.get(StargateBaseBlock.FACING);
            destX += targetFacing.getXOffset() * 1.5;
            destZ += targetFacing.getZOffset() * 1.5;
            targetYaw = targetFacing.getHorizontalAngle();
        }

        entity.timeUntilPortal = 40;
        boolean isCrossDimension = (world.getDimension().getType().getId() != connectedLoc.getDimension());
        DimensionType dimType = targetWorld.getDimension().getType();

        final double fX = destX, fY = destY, fZ = destZ;
        final float fYaw = targetYaw, fPitch = entity.rotationPitch;

        server.addScheduledTask(() -> {
            if (entity instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) entity;
                if (isCrossDimension) player.changeDimension(dimType, new StargateTeleporter(fX, fY, fZ, fYaw, fPitch));
                else player.connection.setPlayerLocation(fX, fY, fZ, fYaw, fPitch);
            } else {
                if (isCrossDimension) entity.changeDimension(dimType, new StargateTeleporter(fX, fY, fZ, fYaw, fPitch));
                else entity.setLocationAndAngles(fX, fY, fZ, fYaw, fPitch);
            }
        });
    }

    private AxisAlignedBB getEventHorizonBoundingBox() {
        EnumFacing facing = world.getBlockState(pos).get(StargateBaseBlock.FACING);
        double x = pos.getX(), y = pos.getY(), z = pos.getZ();
        double minY = y + 1.0, maxY = y + 4.0;

        return facing.getAxis() == EnumFacing.Axis.Z
                ? new AxisAlignedBB(x - 1.0, minY, z + 0.4, x + 2.0, maxY, z + 0.6)
                : new AxisAlignedBB(x + 0.4, minY, z - 1.0, x + 0.6, maxY, z + 2.0);
    }

    public StargateBaseTileEntity getRemoteGate(String targetAddress) {
        if (targetAddress == null || targetAddress.isEmpty() || world.getServer() == null) return null;
        ExtendedPos remotePos = StargateWorldData.findStargateUniversally(world.getServer(), targetAddress);

        if (remotePos != null) {
            WorldServer targetWorld = world.getServer().getWorld(DimensionType.getById(remotePos.getDimension()));
            if (targetWorld != null) {
                TileEntity te = targetWorld.getTileEntity(new BlockPos(remotePos.getX(), remotePos.getY(), remotePos.getZ()));
                if (te instanceof StargateBaseTileEntity) return (StargateBaseTileEntity) te;
            }
        }
        return null;
    }

    private void updateControllerBlockState() {
        if (controllerPos == null || world == null || world.isRemote) return;
        TileEntity te = world.getTileEntity(controllerPos);
        if (te instanceof StargateControllerTileEntity) ((StargateControllerTileEntity) te).onStargateStateChanged(vortexState);
    }

    private void notifyNearbyControllers() {
        double rX = SGCraftRebornConfig.LINK_RANGE_X.get() * 2.0;
        double rY = SGCraftRebornConfig.LINK_RANGE_Y.get() * 2.0;
        double rZ = SGCraftRebornConfig.LINK_RANGE_Z.get() * 2.0;

        for (TileEntity te : world.loadedTileEntityList) {
            if (te instanceof StargateControllerTileEntity && new AxisAlignedBB(pos).grow(rX, rY, rZ).contains(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ())) {
                ((StargateControllerTileEntity) te).getLinkedStargateTE(world);
            }
        }
    }

    private void notifyNearbyComputers() {
        if (world == null || world.isRemote) return;

        EnumFacing.Axis axis = world.getBlockState(pos).get(StargateBaseBlock.FACING).getAxis();

        BlockPos min1, max1, min2, max2;

        if (axis == EnumFacing.Axis.Z) {
            min1 = pos.add(-3, 0, -1);
            max1 = pos.add(3, 0, 1);
            min2 = pos.add(-2, -1, 0);
            max2 = pos.add(2, -1, 0);
        } else {
            min1 = pos.add(-1, 0, -3);
            max1 = pos.add(1, 0, 3);
            min2 = pos.add(0, -1, -2);
            max2 = pos.add(0, -1, 2);
        }

        for (BlockPos scanPos : BlockPos.getAllInBoxMutable(min1, max1)) {
            TileEntity te = world.getTileEntity(scanPos);
            if (te instanceof ComputerCraftInterfaceTileEntity) {
                ((ComputerCraftInterfaceTileEntity) te).notifyComputerCraft();
            }
        }

        for (BlockPos scanPos : BlockPos.getAllInBoxMutable(min2, max2)) {
            TileEntity te = world.getTileEntity(scanPos);
            if (te instanceof ComputerCraftInterfaceTileEntity) {
                ((ComputerCraftInterfaceTileEntity) te).notifyComputerCraft();
            }
        }
    }

    private void updateChunkLoading(boolean load) {
        if (world == null || world.isRemote || !(world instanceof WorldServer)) return;
        ChunkPos centerPos = new ChunkPos(pos);
        int range = SGCraftRebornConfig.CHUNK_LOADING_RANGE.get();

        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                world.setChunkForced(centerPos.x + x, centerPos.z + z, load);
            }
        }
    }

    @Override
    public void onLoad() {
        if (!world.isRemote) {
            StargateWorldData.get(world).register(getAddress(), new ExtendedPos(pos, world.getDimension().getType().getId()));
            if (isMerged()) updateChunkLoading(true);
        }
    }

    @Override
    public void remove() {
        if (world != null && !world.isRemote) {
            if (isMerged()) updateChunkLoading(false);
            if (vortexState != StargateVortexState.IDLE) disconnect();
            if (controllerPos != null) {
                TileEntity te = world.getTileEntity(controllerPos);
                if (te instanceof StargateControllerTileEntity) ((StargateControllerTileEntity) te).unlink();
            }
            StargateWorldData.get(world).unregister(getAddress());
        }
        super.remove();
    }

    public String getAddress() {
        if (address == null || address.trim().isEmpty()) {
            address = StargateAddressing.generateAddress();
            markDirty();
        }
        return address;
    }

    public boolean isMerged() {
        return isMerged;
    }

    public boolean isInitiator() {
        return isInitiator;
    }

    public ExtendedPos getConnectedLoc() {
        return connectedLoc;
    }

    public String getDialledAddress() {
        return dialledAddress;
    }

    public double getEnergyInBuffer() {
        return energyInBuffer;
    }

    public int getNumEngagedChevrons() {
        return numEngagedChevrons;
    }

    public boolean hasChevronUpgrade() {
        return hasChevronUpgrade;
    }

    public boolean hasIrisUpgrade() {
        return hasIrisUpgrade;
    }

    public StargateIrisState getIrisState() {
        return irisState;
    }

    public int getIrisPhase() {
        return irisPhase;
    }

    public int getLastIrisPhase() {
        return lastIrisPhase;
    }

    public StargateVortexState getVortexState() {
        return vortexState;
    }

    public double getRingAngle() {
        return ringAngle;
    }

    public double getLastRingAngle() {
        return lastRingAngle;
    }

    public ExtendedPos getExtendedPos() {
        return new ExtendedPos(getPos(), getWorld().getDimension().getType().getId());
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos = controllerPos; markDirty();
    }

    public void setHasChevronUpgrade(boolean hasChevronUpgrade) {
        this.hasChevronUpgrade = hasChevronUpgrade; sync();
    }

    public void setHasIrisUpgrade(boolean hasIrisUpgrade) {
        this.hasIrisUpgrade = hasIrisUpgrade; sync();
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new StargateBaseCamouflageContainer(playerInventory, pos);
    }

    @Override
    public String getGuiID() {
        return Constants.MOD_ID + ":base_camouflage";
    }

    @Override
    public ITextComponent getName() {
        return new TextComponentTranslation("container.sgcraftreborn.base");
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Nullable @Override public ITextComponent getCustomName() { return null; }

    @Nonnull @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return inventoryHolder.cast();
        return super.getCapability(cap, side);
    }

    @Override
    protected void invalidateCaps() {
        super.invalidateCaps();
        inventoryHolder.invalidate();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos).grow(3.0, 5.0, 8.0);
    }

    private void handleKawooshVaporization() {
        AxisAlignedBB box = getKawooshBoundingBox();
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, box);

        EnumFacing facing = world.getBlockState(pos).get(StargateBaseBlock.FACING);

        double gateX = pos.getX() + 0.5;
        double gateY = pos.getY() + 2.5;
        double gateZ = pos.getZ() + 0.5;

        double maxDepth = 4.0 * Math.sin(Math.PI * (double) (TRANSIENT_DURATION - timeout) / (double) TRANSIENT_DURATION);
        double maxRadius = 2.0;

        if (maxDepth <= 0) return;

        if (maxDepth <= 0) return;

        float vol = SGCraftRebornConfig.SOUND_VOLUME.get().floatValue();

        for (Entity entity : entities) {
            double eX = entity.posX;
            double eY = entity.posY + (entity.height / 2.0);
            double eZ = entity.posZ;

            double distanceAlongAxis = 0.0;
            double distanceFromAxis = 0.0;

            if (facing.getAxis() == EnumFacing.Axis.Z) {
                distanceAlongAxis = Math.abs(eZ - gateZ);
                distanceFromAxis = Math.sqrt(Math.pow(eX - gateX, 2) + Math.pow(eY - gateY, 2));
            } else if (facing.getAxis() == EnumFacing.Axis.X) {
                distanceAlongAxis = Math.abs(eX - gateX);
                distanceFromAxis = Math.sqrt(Math.pow(eZ - gateZ, 2) + Math.pow(eY - gateY, 2));
            } else {
                distanceAlongAxis = Math.abs(eY - gateY);
                distanceFromAxis = Math.sqrt(Math.pow(eX - gateX, 2) + Math.pow(eZ - gateZ, 2));
            }

            if (distanceAlongAxis <= maxDepth && distanceFromAxis <= maxRadius) {
                world.playSound(null, pos, ModSounds.IRIS_HIT, SoundCategory.BLOCKS, vol, 1.0F);
                entity.hurtResistantTime = 0;
                entity.attackEntityFrom(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
            }
        }
    }

    private AxisAlignedBB getKawooshBoundingBox() {
        EnumFacing facing = world.getBlockState(pos).get(StargateBaseBlock.FACING);

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 2.5;
        double z = pos.getZ() + 0.5;
        double r = 2.0;
        double depth = 4.5;

        double minX = x - r, maxX = x + r;
        double minY = y - r, maxY = y + r;
        double minZ = z - r, maxZ = z + r;

        if (facing.getAxis() == EnumFacing.Axis.Z) {
            minZ = z - 0.5; maxZ = z + 0.5;
        } else if (facing.getAxis() == EnumFacing.Axis.X) {
            minX = x - 0.5; maxX = x + 0.5;
        } else {
            minY = y - 0.5; maxY = y + 0.5;
        }

        AxisAlignedBB box = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        return box.expand(facing.getXOffset() * depth, facing.getYOffset() * depth, facing.getZOffset() * depth);
    }

    public double[][][] getEventHorizonGrid() {
        if (ehGrid == null) {
            ehGrid = new double[2][ehGridPolarSize + 2][ehGridRadialSize + 1];
            for (int i = 0; i < 2; i++) {
                ehGrid[i][0] = ehGrid[i][ehGridPolarSize];
                ehGrid[i][ehGridPolarSize + 1] = ehGrid[i][1];
            }
        }
        return ehGrid;
    }

    public void initiateOpeningTransient() {
        double[][][] grid = getEventHorizonGrid();
        double[][] u = grid[0];
        double[][] v = grid[1];
        for (int j = 0; j <= ehGridPolarSize + 1; j++) {
            for (int i = 0; i <= ehGridRadialSize; i++) {
                u[j][i] = 0;
                v[j][i] = 0;
            }
            v[j][ehGridRadialSize - 1] = 4.0;
            v[j][ehGridRadialSize - 2] = 4.0 + 0.25 * random.nextGaussian();
        }
    }

    public void initiateClosingTransient() {
        double[][] v = getEventHorizonGrid()[1];
        for (int i = 1; i < ehGridRadialSize; i++) {
            for (int j = 1; j <= ehGridPolarSize; j++) {
                v[j][i] += 0.25 * random.nextGaussian();
            }
        }
    }

    public void applyRandomImpulse() {
        double[][] v = getEventHorizonGrid()[1];
        int i = random.nextInt(ehGridRadialSize - 1) + 1;
        int j = random.nextInt(ehGridPolarSize) + 1;
        v[j][i] += 0.05 * random.nextGaussian();
    }

    public void updateEventHorizon() {
        double[][][] grid = getEventHorizonGrid();

        double[][] u = grid[0];
        double[][] v = grid[1];

        double dt = 1.0;
        double asq = 0.03;
        double d = 0.95;

        for (int i = 1; i < ehGridRadialSize; i++) {
            for (int j = 1; j <= ehGridPolarSize; j++) {
                double du_dr = 0.5 * (u[j][i + 1] - u[j][i - 1]);
                double d2u_drsq = u[j][i + 1] - 2 * u[j][i] + u[j][i - 1];
                double d2u_dthsq = u[j + 1][i] - 2 * u[j][i] + u[j - 1][i];

                v[j][i] = d * v[j][i] + (asq * dt) * (d2u_drsq + du_dr / i + d2u_dthsq / (i * i));
                if (Double.isNaN(v[j][i])) v[j][i] = 0;
            }
        }

        for (int i = 1; i < ehGridRadialSize; i++) {
            for (int j = 1; j <= ehGridPolarSize; j++) {
                u[j][i] += v[j][i] * dt;
            }
        }

        for (int layer = 0; layer < 2; layer++) {
            for (int r = 0; r <= ehGridRadialSize; r++) {
                grid[layer][0][r] = grid[layer][ehGridPolarSize][r];
                grid[layer][ehGridPolarSize + 1][r] = grid[layer][1][r];
            }
        }

        double u0 = 0, v0 = 0;
        for (int j = 1; j <= ehGridPolarSize; j++) {
            u0 += u[j][1];
            v0 += v[j][1];
        }
        u0 /= ehGridPolarSize;
        v0 /= ehGridPolarSize;

        for (int j = 0; j <= ehGridPolarSize + 1; j++) {
            u[j][0] = u0;
            v[j][0] = v0;
        }
    }

    public int getCamouflageLevel() {
        int level = 0;
        for (int i : new int[]{0, 4}) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Block block = Block.getBlockFromItem(stack.getItem());
                if (block instanceof BlockSlab) level = Math.max(level, 1);
                else if (block.getDefaultState().isFullCube()) level = Math.max(level, 2);
            }
        }
        return level;
    }

    public float getChevronAngle() {
        int type = hasChevronUpgrade() ? 1 : 0;
        int camouflageIdx = SGCraftRebornConfig.VARIABLE_CHEVRON_POSITIONS.get() ? getCamouflageLevel() : 0;
        return CHEVRON_ANGLES[type][camouflageIdx];
    }

    private List<RFPowerUnitTileEntity> getPowerUnits() {
        List<RFPowerUnitTileEntity> units = new ArrayList<>();

        for (EnumFacing facing : EnumFacing.values()) {
            TileEntity te = world.getTileEntity(pos.offset(facing));
            if (te instanceof RFPowerUnitTileEntity && !units.contains(te)) {
                units.add((RFPowerUnitTileEntity) te);
            }
        }

        if (world.getBlockState(pos).getBlock() instanceof StargateBaseBlock) {
            EnumFacing facing = world.getBlockState(pos).get(StargateBaseBlock.FACING);
            EnumFacing right = facing.rotateY();

            for (int i = -2; i <= 2; i++) {
                BlockPos underPos = pos.offset(right, i).down();
                TileEntity te = world.getTileEntity(underPos);
                if (te instanceof RFPowerUnitTileEntity && !units.contains(te)) {
                    units.add((RFPowerUnitTileEntity) te);
                }
            }
        }

        if (controllerPos != null) {
            for (EnumFacing facing : EnumFacing.values()) {
                TileEntity te = world.getTileEntity(controllerPos.offset(facing));
                if (te instanceof RFPowerUnitTileEntity && !units.contains(te)) {
                    units.add((RFPowerUnitTileEntity) te);
                }
            }
        }

        return units;
    }

    public void addComputerAdapter(StargateAbstractAPI adapter) {
        computerAdapters.add(adapter);
    }

    public void removeComputerAdapter(StargateAbstractAPI adapter) {
        computerAdapters.remove(adapter);
    }

    public void fireComputerEvent(String eventName, Object... args) {
        for (StargateAbstractAPI adapter : computerAdapters) {
            if (adapter != null) {
                adapter.queueEvent(eventName, args);
            }
        }
    }

    public void sendMessageAcrossVortex(Object... messageArgs) {
        if (vortexState == StargateVortexState.ACTIVE && connectedLoc != null) {
            StargateBaseTileEntity remoteGate = getRemoteGate(dialledAddress);
            if (remoteGate != null) {
                remoteGate.fireComputerEvent("sgMessageReceived", messageArgs);
            }
        }
    }

}