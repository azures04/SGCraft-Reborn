package fr.azures04.sgcraftreborn.common.network;

import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.network.packets.StargateCloseVortexPacket;
import fr.azures04.sgcraftreborn.common.network.packets.StargateDialPacket;
import fr.azures04.sgcraftreborn.common.network.packets.StargateUpdateBufferPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class StargateNetwork {

    private static final String PROTOCOL_VESION = "1";
    private static final ResourceLocation networkId = new ResourceLocation(Constants.MOD_ID, "main");
    private static int packetId = 0;

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(networkId, () -> PROTOCOL_VESION, PROTOCOL_VESION::equals, PROTOCOL_VESION::equals);

    public static void registerPackets() {
        INSTANCE.registerMessage(packetId++, StargateDialPacket.class, StargateDialPacket::encode, StargateDialPacket::decode, StargateDialPacket::handle);
        INSTANCE.registerMessage(packetId++, StargateCloseVortexPacket.class, StargateCloseVortexPacket::encode, StargateCloseVortexPacket::decode, StargateCloseVortexPacket::handle);
        INSTANCE.registerMessage(packetId++, StargateUpdateBufferPacket.class, StargateUpdateBufferPacket::encode, StargateUpdateBufferPacket::decode, StargateUpdateBufferPacket::handle);
    }

}
