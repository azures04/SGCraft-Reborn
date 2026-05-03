package fr.azures04.sgcraftreborn.common.world;

import net.minecraft.util.text.TextComponentTranslation;

import java.util.UUID;
import java.util.regex.Pattern;

public class StargateAddressing {

    private static final String SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^[A-Z0-9]{7}([A-Z0-9]{2})?$");
    private static final Pattern FORMAT_PATTERN = Pattern.compile("^([A-Z0-9]{4})([A-Z0-9]{3})([A-Z0-9]{2})?$");

    public static boolean isValidAddress(String address) {
        return address != null && ADDRESS_PATTERN.matcher(address).matches();
    }

    public static String generateAddress() {
        UUID uuid = UUID.randomUUID();
        long bits = uuid.getMostSignificantBits() & Long.MAX_VALUE;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            sb.append(SYMBOLS.charAt((int)(bits % 36)));
            bits /= 36;
        }
        return sb.toString();
    }

    public static String formatAddress(String address) {
        if (address == null || address.isEmpty()) return "";

        if (address.length() > 7) {
            return String.format("%s %s %s",
                    address.substring(0, 4),
                    address.substring(4, 7),
                    address.substring(7, 9));
        } else if (address.length() > 4) {
            return String.format("%s %s",
                    address.substring(0, 4),
                    address.substring(4, address.length()));
        }
        return address;
    }

    public static class StargateAddressingException {
        public static final String INVALID_ADDRESS = "stargate.error.invalid_address";
        public static final String GATE_NOT_FOUND = "stargate.error.gate_not_found";
        public static final String GATE_BUSY = "stargate.error.gate_busy";
        public static final String NOT_LINKED = "stargate.error.not_linked";
        public static final String NOT_MERGED = "stargate.error.not_merged";
        public static final String NOT_AT_THIS_ADDRESS = "stargate.error.not_at_this_address";
        public static final String MISSING_IRIS_UPGRADE = "stargate.error.missing_iris_upgrade";
        public static final String MISSING_CHEVRON_UPGRADE = "stargate.error.missing_chevron_upgrade";
        public static final String CANT_DIAL_SAME_GATE = "stargate.error.cant_dial_same_gate";
        public static final String INSUFFICIENT_POWER = "stargate.error.insufficient_power";
    }

}
