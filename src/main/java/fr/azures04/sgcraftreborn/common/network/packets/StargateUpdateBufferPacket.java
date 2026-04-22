package fr.azures04.sgcraftreborn.common.network.packets;

import fr.azures04.sgcraftreborn.common.registries.tiles.StargateControllerTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class StargateUpdateBufferPacket {
    private final BlockPos pos;
    private final String buffer;

    public StargateUpdateBufferPacket(BlockPos pos, String buffer) {
        this.pos = pos;
        this.buffer = buffer;
    }

    public static void encode(StargateUpdateBufferPacket msg, PacketBuffer buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeString(msg.buffer);
    }

    public static StargateUpdateBufferPacket decode(PacketBuffer buf) {
        return new StargateUpdateBufferPacket(buf.readBlockPos(), buf.readString(9));
    }

    public static void handle(StargateUpdateBufferPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World world = ctx.get().getSender().world;
            TileEntity te = world.getTileEntity(msg.pos);
            if (te instanceof StargateControllerTileEntity) {
                ((StargateControllerTileEntity) te).setDialingBuffer(msg.buffer);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}