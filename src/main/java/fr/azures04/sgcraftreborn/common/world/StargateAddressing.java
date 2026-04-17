package fr.azures04.sgcraftreborn.common.world;

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

    public static class StargateAddressingException extends RuntimeException {
        public static final StargateAddressingException INVALID_ADDRESS = new StargateAddressingException("Invalid address");
        public static final StargateAddressingException GATE_NOT_FOUND = new StargateAddressingException("No stargate at this address");
        public static final StargateAddressingException GATE_BUSY = new StargateAddressingException("Stargate is busy");
        public static final StargateAddressingException NOT_LINKED = new StargateAddressingException("Stargate not linked");
        public static final StargateAddressingException NOT_MERGED = new StargateAddressingException("Stargate not merged");
        public static final StargateAddressingException NOT_AT_THIS_ADDRESS = new StargateAddressingException("No stargate at this address");
        public static final StargateAddressingException MISSING_IRIS_UPGRADE = new StargateAddressingException("You need an iris upgrade to do that.");
        public static final StargateAddressingException MISSING_CHEVRON_UPGRADE = new StargateAddressingException("You need an chevron upgrade to do that.");
        public static final StargateAddressingException CANT_DIAL_SAME_GATE = new StargateAddressingException("A gate cannot call itfself.");

        public StargateAddressingException(String message) {
            super(message);
        }
    }

}
