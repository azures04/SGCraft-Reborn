package fr.azures04.sgcraftreborn.client.models;

import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import java.util.concurrent.Callable;

public interface ISpecialItemRenderer {
    Callable<TileEntityItemStackRenderer> getISTER();
}