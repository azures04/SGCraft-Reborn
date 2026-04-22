package fr.azures04.sgcraftreborn.common.registries;

import fr.azures04.sgcraftreborn.common.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import java.util.ArrayList;
import java.util.List;

public class ModSounds {

    public static final List<SoundEvent> SOUNDS_TO_REGISTER = new ArrayList<>();

    public static final SoundEvent SG_OPEN;
    public static final SoundEvent SG_CLOSE;
    public static final SoundEvent SG_DIAL7;
    public static final SoundEvent SG_DIAL9;
    public static final SoundEvent SG_ABORT;

    public static final SoundEvent IRIS_OPEN;
    public static final SoundEvent IRIS_CLOSE;
    public static final SoundEvent IRIS_HIT;

    public static final SoundEvent DHD_PRESS;
    public static final SoundEvent DHD_DIAL;

    static {
        SG_OPEN = register("sg_open");
        SG_CLOSE = register("sg_close");
        SG_DIAL7 = register("sg_dial7");
        SG_DIAL9 = register("sg_dial9");
        SG_ABORT = register("sg_abort");

        IRIS_OPEN = register("iris_open");
        IRIS_CLOSE = register("iris_close");
        IRIS_HIT = register("iris_hit");

        DHD_PRESS = register("dhd_press");
        DHD_DIAL = register("dhd_dial");
    }

    private static SoundEvent register(String name) {
        ResourceLocation location = new ResourceLocation(Constants.MOD_ID, name);
        SoundEvent sound = new SoundEvent(location);
        sound.setRegistryName(location);

        SOUNDS_TO_REGISTER.add(sound);
        return sound;
    }
}