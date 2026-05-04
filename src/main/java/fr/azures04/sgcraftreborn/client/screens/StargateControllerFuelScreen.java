package fr.azures04.sgcraftreborn.client.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.containers.StargateControllerFuelContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class StargateControllerFuelScreen extends ContainerScreen<StargateControllerFuelContainer> {

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

    public StargateControllerFuelScreen(StargateControllerFuelContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.container = container;
        xSize = GUI_WIDTH;
        ySize = GUI_HEIGHT;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String titleStr = title.getFormattedText();
        int titleWidth = font.getStringWidth(titleStr);
        font.drawString(titleStr, (xSize - titleWidth) / 2, 8, 0x004c66);

        font.drawString("Fuel", 150, 96, 0x004c66);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(GUI_TEXTURE);

        int relX = (width - xSize) / 2;
        int relY = (height - ySize) / 2;

        blit(relX, relY, 0, 0, xSize, ySize);

        drawFuelGauge(relX, relY);
    }

    private void drawFuelGauge(int relX, int relY) {
        double energyLevel = container.getEnergyScaled();

        int levelHeight = (int)(FUEL_GAUGE_HEIGHT * energyLevel);

        if (levelHeight > 0) {
            GlStateManager.enableBlend();

            fillGradient(
                relX + FUEL_LEFT_X,
                relY + FUEL_LEFT_Y + FUEL_GAUGE_HEIGHT - levelHeight,
                FUEL_LEFT_U,
                FUEL_FUEL_V + FUEL_GAUGE_HEIGHT - levelHeight,
                FUEL_GAUGE_WIDTH,
                levelHeight
            );

            GlStateManager.disableBlend();
        }
    }
}