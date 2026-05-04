package fr.azures04.sgcraftreborn.common.network.packets;

import fr.azures04.sgcraftreborn.SGCraftReborn;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateControllerTileEntity;
import fr.azures04.sgcraftreborn.common.util.math.ExtendedPos;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.Level;

import java.util.function.Supplier;

public class StargateDialPacket {

    private final BlockPos dhdPos;
    private final String address;
    private final int dimension;

    public StargateDialPacket(ExtendedPos dhdPos, String address) {
        this.dhdPos = dhdPos.getPos();
        this.address = address;
        this.dimension = dhdPos.getDimension();
    }

    public StargateDialPacket(BlockPos dhdPos, int dimension, String address) {
        this.dhdPos = dhdPos;
        this.address = address;
        this.dimension = dimension;
    }

    public static void encode(StargateDialPacket msg, PacketBuffer buf) {
        buf.writeBlockPos(msg.dhdPos);
        buf.writeInt(msg.dimension);
        buf.writeString(msg.address);
    }

    public static StargateDialPacket decode(PacketBuffer buf) {
        return new StargateDialPacket(buf.readBlockPos(), buf.readInt(), buf.readString(9));
    }

    public static void handle(StargateDialPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity sender = ctx.get().getSender();
            if (sender == null) return;

            World world = sender.world;
            BlockPos targetPos = msg.dhdPos;

            if (world.dimension.getType().getId() != msg.dimension) {
                SGCraftReborn.LOGGER.log(Level.WARN, "Player " + ctx.get().getSender().getName() + " tried to use a DHD from a different dimension to that of the DHD.");
                return;
            }

            double distanceSq = sender.getDistanceSq(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);

            if (distanceSq > 64.0D) {
                SGCraftReborn.LOGGER.log(Level.WARN, "Player " + ctx.get().getSender().getName() + " tried to use a DHD that was out of range!");
                return;
            }

            if (!world.isBlockLoaded(targetPos)) {
                return;
            }
            TileEntity te = world.getTileEntity(targetPos);

            if (te instanceof StargateControllerTileEntity) {
                StargateControllerTileEntity dhd = (StargateControllerTileEntity) te;
                try {
                    String result = dhd.dial(msg.address);
                    if (result != null) {
                        ctx.get().getSender().sendStatusMessage(new StringTextComponent(TextFormatting.RED + new TranslationTextComponent(result).getString()), true);
                    }
                } catch (RuntimeException e) {
                    SGCraftReborn.LOGGER.log(Level.ERROR, e.toString());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}