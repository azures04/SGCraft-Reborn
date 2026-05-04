package fr.azures04.sgcraftreborn.client.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.containers.StargateBaseCamouflageContainer;
import fr.azures04.sgcraftreborn.common.world.StargateAddressing;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class StargateBaseCamouflageScreen extends ContainerScreen<StargateBaseCamouflageContainer> {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/sg_gui.png");
    private static final ResourceLocation SYMBOL_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/symbols48.png");

    private final StargateBaseCamouflageContainer container;
    private String rawAddress = "";
    private String formattedAddress = "";

    public StargateBaseCamouflageScreen(StargateBaseCamouflageContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.container = container;
        xSize = 256;
        ySize = 208;

        String address = container.base.getAddress();
        if (address != null && !address.isEmpty()) {
            rawAddress = address;
            formattedAddress = StargateAddressing.formatAddress(address).replaceAll(" ", "-");
        } else {
            formattedAddress = "";
        }
    }

    private int charToSymbol(char c) {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".indexOf(Character.toUpperCase(c));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int centerX = xSize / 2;

        String title = "Stargate Address";
        font.drawString(title, centerX - (font.getStringWidth(title) / 2), 8, 0x004c66);
        font.drawString(formattedAddress, centerX - (font.getStringWidth(formattedAddress) / 2), 72, 0x004c66);

        String camoText = "Base Camouflage";
        font.drawString(camoText, 92 - (font.getStringWidth(camoText) / 2), 92, 0x004c66);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        int relX = (width - xSize) / 2;
        int relY = (height - ySize) / 2;

        minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
        blit(relX, relY, 0, 0, xSize, ySize);

        if (!rawAddress.isEmpty()) {
            drawAddressSymbols(relX + (xSize / 2), relY + 22, rawAddress);
        }
    }

    private void drawAddressSymbols(int cx, int y, String address) {
        int cellSize = 24;
        int frameHeight = 44;
        int symbolsPerRow = 10;
        int symbolSizeOnTexture = 48;

        int x0 = cx - (address.length() * cellSize) / 2;
        int y0 = y + (frameHeight / 2) - (cellSize / 2);

        minecraft.getTextureManager().bindTexture(SYMBOL_TEXTURE);

        GlStateManager.enableBlend();

        for (int i = 0; i < address.length(); i++) {
            int s = charToSymbol(address.charAt(i));
            if (s >= 0) {
                int row = s / symbolsPerRow;
                int col = s % symbolsPerRow;

                int u = col * symbolSizeOnTexture;
                int v = row * symbolSizeOnTexture;

                blit(x0 + (i * cellSize), y0, cellSize, cellSize, u, v, symbolSizeOnTexture, symbolSizeOnTexture, 512, 256);
            }
        }

        GlStateManager.disableBlend();
    }
}