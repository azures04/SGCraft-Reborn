package fr.azures04.sgcraftreborn.common.registries.tiles;

import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.containers.RFPowerUnitContainer;
import fr.azures04.sgcraftreborn.common.registries.ModTilesEntities;
import fr.azures04.sgcraftreborn.common.util.math.Stargates;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RFPowerUnitTileEntity extends TileEntity implements IInteractionObject {

    private final EnergyStorage energyStorage = new EnergyStorage(4000000, 4000000, 4000000) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0 && !simulate) {
                markDirty();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (extracted > 0 && !simulate) {
                markDirty();
            }
            return extracted;
        }
    };

    private final LazyOptional<EnergyStorage> energyHolder = LazyOptional.of(() -> energyStorage);
    private int lastEnergy = 0;
    private static final double FE_PER_SGPU = 80.0;

    public RFPowerUnitTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public RFPowerUnitTileEntity() {
        super(ModTilesEntities.RF_POWER_UNIT_BLOCK);
    }

    @Override
    public NBTTagCompound write(NBTTagCompound compound) {
        super.write(compound);
        compound.putInt("energy", energyStorage.getEnergyStored());
        return compound;
    }

    @Override
    public void read(NBTTagCompound compound) {
        super.read(compound);
        if (compound.contains("energy", 3)) {
            energyStorage.extractEnergy(energyStorage.getEnergyStored(), false);
            energyStorage.receiveEnergy(compound.getInt("energy"), false);
            lastEnergy = energyStorage.getEnergyStored();
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        if (cap == CapabilityEnergy.ENERGY) {
            return energyHolder.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void invalidateCaps() {
        super.invalidateCaps();
        energyHolder.invalidate();
    }

    public double getAvailableSGEnergy() {
        return energyStorage.getEnergyStored() / FE_PER_SGPU;
    }

    public double extractSGEnergy(double requestedSGU) {
        int requestedFE = (int) Math.ceil(requestedSGU * FE_PER_SGPU);
        int extractedFE = energyStorage.extractEnergy(requestedFE, false);
        return extractedFE / FE_PER_SGPU;
    }

    public int getStoredForgeEnergy() {
        return energyStorage.getEnergyStored();
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new RFPowerUnitContainer(playerInventory, pos);
    }

    @Override
    public String getGuiID() {
        return Constants.MOD_ID + ":rf_power_unit";
    }

    @Override
    public ITextComponent getName() {
        return new TextComponentTranslation("container.sgcraftreborn.rf_power_unit");
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

    @Override
    public void onLoad() {
        super.onLoad();
        if (world != null && !world.isRemote) {
            StargateBaseTileEntity gate = Stargates.searchNearbyStargate(world, pos);
            if (gate != null) gate.registerPowerUnit(this);
        }
    }

    @Override
    public void remove() {
        if (world != null && !world.isRemote) {
            StargateBaseTileEntity gate = Stargates.searchNearbyStargate(world, pos);
            if (gate != null) gate.unregisterPowerUnit(this);
        }
        super.remove();
    }
}
