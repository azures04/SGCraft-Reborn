package fr.azures04.sgcraftreborn.client.screens;

import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.containers.RFPowerUnitContainer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

public class RFPowerUnitScreen extends GuiContainer {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/power_gui.png");

    private static final int GUI_WIDTH = 128;
    private static final int GUI_HEIGHT = 64;

    private final RFPowerUnitContainer container;

    public RFPowerUnitScreen(RFPowerUnitContainer container) {
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
        // Titre
        String title = new TextComponentTranslation("container.sgcraftreborn.power").getFormattedText();
        int titleWidth = this.fontRenderer.getStringWidth(title);
        this.fontRenderer.drawString(title, (this.xSize - titleWidth) / 2, 8, 0x404040); // Couleur sombre classique

        double ratio = this.container.getEnergyScaled();
        double maxEnergy = 4000000.0;
        double currentEnergy = maxEnergy * ratio;

        String unitName = "FE";
        this.fontRenderer.drawString(unitName, 72 - this.fontRenderer.getStringWidth(unitName), 28, 0x404040);

        String maxLabel = "Max";
        this.fontRenderer.drawString(maxLabel, 72 - this.fontRenderer.getStringWidth(maxLabel), 42, 0x404040);

        String currentStr = String.format("%.0f", currentEnergy);
        this.fontRenderer.drawString(currentStr, 121 - this.fontRenderer.getStringWidth(currentStr), 28, 0x404040);

        String maxStr = String.format("%.0f", maxEnergy);
        this.fontRenderer.drawString(maxStr, 121 - this.fontRenderer.getStringWidth(maxStr), 42, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);

        int relX = (this.width - this.xSize) / 2;
        int relY = (this.height - this.ySize) / 2;

        Gui.drawModalRectWithCustomSizedTexture(relX, relY, 0, 0, this.xSize, this.ySize, 128, 64);

        double energyLevel = this.container.getEnergyScaled();
        int gaugeWidth = (int)(25 * energyLevel);

        if (gaugeWidth > 0) {
            int left = relX + 19;
            int top = relY + 27;
            int right = left + gaugeWidth;
            int bottom = top + 10;

            Gui.drawRect(left, top, right, bottom, 0xFFFF0000);
        }
    }
}