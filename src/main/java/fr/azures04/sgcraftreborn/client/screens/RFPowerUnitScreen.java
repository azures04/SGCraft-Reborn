package fr.azures04.sgcraftreborn.client.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.containers.RFPowerUnitContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class RFPowerUnitScreen extends ContainerScreen<RFPowerUnitContainer> {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/power_gui.png");

    private static final int GUI_WIDTH = 128;
    private static final int GUI_HEIGHT = 64;

    private final RFPowerUnitContainer container;

    public RFPowerUnitScreen(RFPowerUnitContainer container, PlayerInventory playerInventory, ITextComponent title) {
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
        font.drawString(titleStr, (xSize - titleWidth) / 2.0F, 8, 0x404040);
        
        double ratio = container.getEnergyScaled();
        double maxEnergy = 4000000.0;
        double currentEnergy = maxEnergy * ratio;

        String unitName = "FE";
        font.drawString(unitName, 72 - font.getStringWidth(unitName), 28, 0x404040);

        String maxLabel = "Max";
        font.drawString(maxLabel, 72 - font.getStringWidth(maxLabel), 42, 0x404040);

        String currentStr = String.format("%.0f", currentEnergy);
        font.drawString(currentStr, 121 - font.getStringWidth(currentStr), 28, 0x404040);

        String maxStr = String.format("%.0f", maxEnergy);
        font.drawString(maxStr, 121 - font.getStringWidth(maxStr), 42, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(GUI_TEXTURE);

        int relX = (width - xSize) / 2;
        int relY = (height - ySize) / 2;

        blit(relX, relY, 0, 0, xSize, ySize, 128, 64);

        double energyLevel = container.getEnergyScaled();
        int gaugeWidth = (int)(25 * energyLevel);

        if (gaugeWidth > 0) {
            int left = relX + 19;
            int top = relY + 27;
            int right = left + gaugeWidth;
            int bottom = top + 10;

            fill(left, top, right, bottom, 0xFFFF0000);
        }
    }
}