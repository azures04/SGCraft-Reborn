package fr.azures04.sgcraftreborn.client.screens;

import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.inventories.StargateBaseCamouflageContainer;
import fr.azures04.sgcraftreborn.common.world.StargateAddressing;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class StargateBaseCamouflageScreen extends GuiContainer {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/sg_gui.png");
    private static final ResourceLocation SYMBOL_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/symbols48.png");

    private final StargateBaseCamouflageContainer container;
    private String rawAddress = "";
    private String formattedAddress = "";

    public StargateBaseCamouflageScreen(StargateBaseCamouflageContainer container) {
        super(container);
        this.container = container;
        this.xSize = 256;
        this.ySize = 208;

        String address = container.te.getAddress();
        if (address != null && !address.isEmpty()) {
            this.rawAddress = address;
            this.formattedAddress = StargateAddressing.formatAddress(address).replaceAll(" ", "-");
        } else {
            this.formattedAddress = "";
        }
    }

    private int charToSymbol(char c) {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".indexOf(Character.toUpperCase(c));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int centerX = this.xSize / 2;

        String title = "Stargate Address";
        this.fontRenderer.drawString(title, centerX - (this.fontRenderer.getStringWidth(title) / 2), 8, 0x004c66);
        this.fontRenderer.drawString(formattedAddress, centerX - (this.fontRenderer.getStringWidth(formattedAddress) / 2), 72, 0x004c66);

        String camoText = "Base Camouflage";
        this.fontRenderer.drawString(camoText, 92 - (this.fontRenderer.getStringWidth(camoText) / 2), 92, 0x004c66);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        int relX = (this.width - this.xSize) / 2;
        int relY = (this.height - this.ySize) / 2;

        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        this.drawTexturedModalRect(relX, relY, 0, 0, this.xSize, this.ySize);

        if (!rawAddress.isEmpty()) {
            drawAddressSymbols(relX + (this.xSize / 2), relY + 22, rawAddress);
        }
    }

    private void drawAddressSymbols(int cx, int y, String address) {
        int cellSize = 24;
        int frameHeight = 44;
        int symbolsPerRow = 10;
        int symbolSizeOnTexture = 48;

        int x0 = cx - (address.length() * cellSize) / 2;
        int y0 = y + (frameHeight / 2) - (cellSize / 2);

        this.mc.getTextureManager().bindTexture(SYMBOL_TEXTURE);

        GlStateManager.enableBlend();

        for (int i = 0; i < address.length(); i++) {
            int s = charToSymbol(address.charAt(i));
            if (s >= 0) {
                int row = s / symbolsPerRow;
                int col = s % symbolsPerRow;

                int u = col * symbolSizeOnTexture;
                int v = row * symbolSizeOnTexture;

                Gui.drawScaledCustomSizeModalRect(x0 + (i * cellSize), y0, u, v,symbolSizeOnTexture, symbolSizeOnTexture,cellSize, cellSize, 512, 256                );
            }
        }

        GlStateManager.disableBlend();
    }
}