package fr.azures04.sgcraftreborn.registries.world;

import java.util.UUID;
import java.util.regex.Pattern;

public class StargateAddressing {

    private static final String SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^[A-Z0-9]{7}$");
    private static final Pattern FORMAT_PATTERN = Pattern.compile("^([A-Z0-9]{3})([A-Z0-9]{4})$");

    public static String formatAddress(String address) {
        return FORMAT_PATTERN.matcher(address).replaceAll("$1-$2");
    }

    public static String normalizeAddress(String address) {
        return address.toUpperCase().replaceAll("[^A-Z0-9]", "");
    }

    public static boolean isValidAddress(String address) {
        return address != null && ADDRESS_PATTERN.matcher(address).matches();
    }

    public static String generateAddress() {
        UUID uuid = UUID.randomUUID();
        long bits = uuid.getMostSignificantBits() & Long.MAX_VALUE;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            sb.append(SYMBOLS.charAt((int)(bits % 36)));
            bits /= 36;
        }
        return sb.toString();
    }

    public static class StargateAddressingException extends RuntimeException {
        public static final StargateAddressingException INVALID_ADDRESS = new StargateAddressingException("Invalid address");
        public static final StargateAddressingException GATE_NOT_FOUND = new StargateAddressingException("No stargate at this address");
        public static final StargateAddressingException GATE_BUSY = new StargateAddressingException("Stargate is busy");

        public StargateAddressingException(String message) {
            super(message);
        }
    }

}
