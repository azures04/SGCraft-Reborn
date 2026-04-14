package fr.azures04.sgcraftreborn.common.registries.blocks.states;

import net.minecraft.util.IStringSerializable;

public enum StargateControllerStatus implements IStringSerializable {

    UNLINKED("unlinked"),
    LINKED("linked"),
    ACTIVATED("activated");

    private final String name;

    StargateControllerStatus(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
