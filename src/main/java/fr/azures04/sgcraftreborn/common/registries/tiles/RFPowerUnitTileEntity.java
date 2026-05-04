package fr.azures04.sgcraftreborn.common.registries.tiles;

import fr.azures04.sgcraftreborn.common.containers.RFPowerUnitContainer;
import fr.azures04.sgcraftreborn.common.registries.ModTilesEntities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RFPowerUnitTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {

    private final EnergyStorage energyStorage = new EnergyStorage(4000000, 4000000, 4000000);
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
    public void tick() {
        if (world != null && !world.isRemote) {
            int currentEnergy = energyStorage.getEnergyStored();
            if (currentEnergy != lastEnergy) {
                lastEnergy = currentEnergy;
                markDirty();

            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putInt("energy", energyStorage.getEnergyStored());
        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        if (compound.contains("energy", 3)) {
            energyStorage.extractEnergy(energyStorage.getEnergyStored(), false);
            energyStorage.receiveEnergy(compound.getInt("energy"), false);
            lastEnergy = energyStorage.getEnergyStored();
        }
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
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("container.sgcraftreborn.rf_power_unit");
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new RFPowerUnitContainer(i, playerInventory, pos);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY) {
            return energyHolder.cast();
        }
        return super.getCapability(cap, side);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbt = new CompoundNBT();
        this.write(nbt);
        return new SUpdateTileEntityPacket(this.pos, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(pkt.getNbtCompound());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void onLoad() {
        if (world != null && !world.isRemote) {
            world.notifyNeighborsOfStateChange(pos, getBlockState().getBlock());
        }
    }
}
