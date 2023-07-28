package fr.azures.mod.sgcraft_reborn.utils;


public class Logging {
    public static void info(String s) {
    	System.out.println("[INFO] SGCraft-Reborn : " + s);
    }

    public static void error(String s) {
    	System.out.println("[ERROR] SGCraft-Reborn : " + s);
    }

    public static void warn(String s) {
    	System.out.println("[WARNING] SGCraft-Reborn : " + s);
    }

    public static void debug(String s) {
    	System.out.println("[DEBUG] SGCraft-Reborn : " + s);
    }
}