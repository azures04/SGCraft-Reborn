package fr.azures04.sgcraftreborn.client.screens;

import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.containers.StargateControllerFuelContainer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

public class StargateControllerFuelScreen extends GuiContainer {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/dhd_fuel_gui.png");

    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 208;

    private static final int FUEL_GAUGE_WIDTH = 16;
    private static final int FUEL_GAUGE_HEIGHT = 34;
    private static final int FUEL_LEFT_X = 214;
    private static final int FUEL_LEFT_Y = 84;
    private static final int FUEL_LEFT_U = 0;
    private static final int FUEL_FUEL_V = 208;

    public final StargateControllerFuelContainer container;

    public StargateControllerFuelScreen(StargateControllerFuelContainer container) {
        super(container);
        this.container = container;
        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = new TextComponentTranslation("container.sgcraftreborn.dhd").getFormattedText();
        int titleWidth = this.fontRenderer.getStringWidth(title);
        this.fontRenderer.drawString(title, (this.xSize - titleWidth) / 2, 8, 0x004c66);

        this.fontRenderer.drawString("Fuel", 150, 96, 0x004c66);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);

        int relX = (this.width - this.xSize) / 2;
        int relY = (this.height - this.ySize) / 2;

        this.drawTexturedModalRect(relX, relY, 0, 0, this.xSize, this.ySize);

        drawFuelGauge(relX, relY);
    }

    private void drawFuelGauge(int relX, int relY) {
        // On récupère le ratio (0.0 à 1.0) calculé par le container
        double energyLevel = this.container.getEnergyScaled();

        // On calcule la hauteur en pixels (max 34)
        int levelHeight = (int)(FUEL_GAUGE_HEIGHT * energyLevel);

        if (levelHeight > 0) {
            GlStateManager.enableBlend();

            // On dessine la partie colorée de la jauge.
            // La texture de la jauge commence à V=208
            this.drawTexturedModalRect(
                    relX + FUEL_LEFT_X,
                    relY + FUEL_LEFT_Y + FUEL_GAUGE_HEIGHT - levelHeight, // Positionne le bas
                    FUEL_LEFT_U,
                    FUEL_FUEL_V + FUEL_GAUGE_HEIGHT - levelHeight, // Découpe le bas de la texture
                    FUEL_GAUGE_WIDTH,
                    levelHeight
            );

            GlStateManager.disableBlend();
        }
    }
}