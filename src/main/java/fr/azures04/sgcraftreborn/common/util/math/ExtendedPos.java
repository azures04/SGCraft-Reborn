package fr.azures04.sgcraftreborn.common.util.math;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class ExtendedPos extends BlockPos {

    private final int d;

    public ExtendedPos(int x, int y, int z, int d) {
        super(x, y, z);
        this.d = d;
    }

    public ExtendedPos(double x, double y, double z, int d) {
        super(x, y, z);
        this.d = d;
    }

    public ExtendedPos(Entity source, int d) {
        super(source);
        this.d = d;
    }

    public ExtendedPos(Vec3d vec, int d) {
        super(vec);
        this.d = d;
    }

    public ExtendedPos(Vec3i source, int d) {
        super(source);
        this.d = d;
    }

    public int getDimension() {
        return d;
    }

    public BlockPos getPos() {
        return new BlockPos(getX(), getY(), getZ());
    }
}
