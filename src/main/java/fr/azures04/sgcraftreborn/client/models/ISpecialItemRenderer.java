package fr.azures04.sgcraftreborn.client.models;

import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;

import java.util.concurrent.Callable;

public interface ISpecialItemRenderer {
    Callable<ItemStackTileEntityRenderer> getISTER();
}