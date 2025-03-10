package fr.azures.mod.sgcraft_reborn.registry.objects.tiles;

import fr.azures.mod.sgcraft_reborn.registry.ModItems;
import fr.azures.mod.sgcraft_reborn.registry.ModTilesEntities;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class StargateBaseTile extends TileEntity implements ITickableTileEntity {

    private boolean hasChevronUpgrade = false;
    private boolean hasIrisUpgrade = false;
    
	public StargateBaseTile(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
	}
	
	public StargateBaseTile() {
		super(ModTilesEntities.STARGATE_CONTROLLER.get());
	}

	@Override
	public void tick() {
		
	}

    public boolean canAcceptUpgrade(ItemStack upgradeItem) {
        if (upgradeItem.getItem() == ModItems.SG_CHEVRON_UPGRADE.get()) {
            return !this.hasChevronUpgrade;
        } else if (upgradeItem.getItem() == ModItems.SG_IRIS_UPGRADE.get()) {
            return !this.hasIrisUpgrade;
        }
        return false;
    }
    
    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        this.hasChevronUpgrade = tag.getBoolean("HasChevronUpgrade");
        this.hasIrisUpgrade = tag.getBoolean("HasIrisUpgrade");
    }

    public boolean hasChevronUpgrade() {
        return hasChevronUpgrade;
    }

    public boolean hasIrisUpgrade() {
        return hasIrisUpgrade;
    }
    
    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        tag.putBoolean("HasChevronUpgrade", hasChevronUpgrade);
        tag.putBoolean("HasIrisUpgrade", hasIrisUpgrade);
		return tag;
    }
    
    public void addUpgrade(ItemStack upgradeItem) {
        if (canAcceptUpgrade(upgradeItem)) {
            if (upgradeItem.getItem() == ModItems.SG_CHEVRON_UPGRADE.get()) {
                this.hasChevronUpgrade = true;
            } else if (upgradeItem.getItem() == ModItems.SG_IRIS_UPGRADE.get()) {
                this.hasIrisUpgrade = true;
            }
            setChanged();
        }
    }
}
