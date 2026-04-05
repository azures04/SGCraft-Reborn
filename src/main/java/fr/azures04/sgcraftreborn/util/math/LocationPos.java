package fr.azures04.sgcraftreborn.util.math;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class LocationPos extends BlockPos {

    private final int d;

    public LocationPos(int x, int y, int z, int d) {
        super(x, y, z);
        this.d = d;
    }

    public LocationPos(double x, double y, double z, int d) {
        super(x, y, z);
        this.d = d;
    }

    public LocationPos(Entity source, int d) {
        super(source);
        this.d = d;
    }

    public LocationPos(Vec3d vec, int d) {
        super(vec);
        this.d = d;
    }

    public LocationPos(Vec3i source, int d) {
        super(source);
        this.d = d;
    }

    public int getDimension() {
        return d;
    }
}
