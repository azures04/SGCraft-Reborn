package fr.azures04.sgcraftreborn.common.world;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

public class StargateTeleporter implements ITeleporter {

    private final double x, y, z;
    private final float yaw, pitch;

    public StargateTeleporter(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public void placeEntity(World world, Entity entity, float yaw) {
        entity.motionX = 0;
        entity.motionY = 0;
        entity.motionZ = 0;
        entity.setLocationAndAngles(this.x, this.y, this.z, this.yaw, this.pitch);
    }

    @Override
    public boolean isVanilla() {
        return false;
    }
}
