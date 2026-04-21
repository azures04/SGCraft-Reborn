package fr.azures04.sgcraftreborn.client.screens;

import fr.azures04.sgcraftreborn.SGCraftReborn;
import fr.azures04.sgcraftreborn.common.network.StargateNetwork;
import fr.azures04.sgcraftreborn.common.network.packets.StargateCloseVortexPacket;
import fr.azures04.sgcraftreborn.common.network.packets.StargateDialPacket;
import fr.azures04.sgcraftreborn.common.registries.blocks.states.StargateControllerStatus;
import fr.azures04.sgcraftreborn.common.util.math.ExtendedPos;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.world.StargateAddressing;
import org.apache.logging.log4j.Level;

public class StargateControllerScreen extends GuiScreen {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/dhd_gui.png");
    private static final ResourceLocation CENTRE_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/dhd_centre.png");
    private static final ResourceLocation SYMBOL_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/symbols48.png");

    final static int dhdWidth = 320;
    final static int dhdHeight = 120;
    final static double dhdRadius1 = dhdWidth * 0.1;
    final static double dhdRadius2 = dhdWidth * 0.275;
    final static double dhdRadius3 = dhdWidth * 0.45;

    final static int symbolsPerRowInTexture = 10;
    final static int symbolWidthInTexture = 48;
    final static int symbolHeightInTexture = 48;
    final static int symbolTextureWidth = 512;
    final static int symbolTextureHeight = 256;
    final static int cellSize = 24;

    private int dhdCentreX, dhdCentreY, dhdTop;
    private String enteredAddress = "";
    private int addressLength = 7;
    private final ExtendedPos controllerPos;
    private final StargateControllerStatus status;

    public StargateControllerScreen(ExtendedPos controllerPos, StargateControllerStatus status, boolean hasChevronUpgrade) {
        super();
        this.controllerPos = controllerPos;
        this.status = status;
        if (status != StargateControllerStatus.LINKED) {
            addressLength = 0;
        } else if (hasChevronUpgrade) {
            addressLength = 9;
        }
    }

    @Override
    protected void initGui() {
        super.initGui();

        if (this.mc != null && this.mc.mouseHelper != null) {
            this.mc.mouseHelper.ungrabMouse();
        }

        this.dhdTop = this.height - dhdHeight;
        this.dhdCentreX = this.width / 2;
        this.dhdCentreY = this.dhdTop + (dhdHeight / 2);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableAlphaTest();

        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        drawModalRectWithCustomSizedTexture((this.width - dhdWidth) / 2, this.dhdTop, 0, 0, dhdWidth, dhdHeight, dhdWidth, dhdHeight);

        drawOrangeButton();

        drawEnteredSymbols();
        drawEnteredString();

        GlStateManager.enableAlphaTest();
        GlStateManager.disableBlend();

        super.render(mouseX, mouseY, partialTicks);
    }

    private void drawOrangeButton() {
        this.mc.getTextureManager().bindTexture(CENTRE_TEXTURE);

        switch (status) {
            case LINKED:
                GlStateManager.color4f(0.5F, 0.25F, 0.0F, 1.0F);
                break;
            case ACTIVATED:
                GlStateManager.color4f(1.0F, 0.5F, 0.0F, 1.0F);
                break;
            case UNLINKED:
                GlStateManager.color4f(0.2F, 0.2F, 0.2F, 1.0F);
                break;
        }

        double rx = dhdWidth * 48 / 512.0;
        double ry = dhdHeight * 48 / 256.0;

        Gui.drawScaledCustomSizeModalRect(dhdCentreX - (int)rx, dhdCentreY - (int)ry - 6, 64, 0, 64, 48, (int)(2 * rx), (int)(1.5 * ry), 128, 64);

        if (status == StargateControllerStatus.ACTIVATED) {
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F); // Reset couleur pour la texture de lueur

            double d = 5;
            Gui.drawScaledCustomSizeModalRect(dhdCentreX - (int)(rx + d), dhdCentreY - (int)(ry + d) - 6, 0, 0, 64, 32, (int)(2 * (rx + d)), (int)(ry + d), 128, 64);
            Gui.drawScaledCustomSizeModalRect(dhdCentreX - (int)(rx + d), dhdCentreY - 6, 0, 32, 64, 32, (int)(2 * (rx + d)), (int)(0.5 * ry + d), 128, 64);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F); // Reset final
    }
    private void drawEnteredSymbols() {
        if (enteredAddress.isEmpty()) return;

        this.mc.getTextureManager().bindTexture(SYMBOL_TEXTURE);

        int x = this.width / 2;
        int y = this.dhdTop - 80;

        int n = enteredAddress.length();
        int x0 = x - (n * cellSize / 2);
        int y0 = y + 22 - (cellSize / 2);

        for (int i = 0; i < n; i++) {
            char c = enteredAddress.charAt(i);
            int s = SGCraftCharToSymbol(c);

            int row = s / symbolsPerRowInTexture;
            int col = s % symbolsPerRowInTexture;

            Gui.drawScaledCustomSizeModalRect(x0 + (i * cellSize), y0, col * symbolWidthInTexture, row * symbolHeightInTexture, symbolWidthInTexture, symbolHeightInTexture, cellSize, cellSize, symbolTextureWidth, symbolTextureHeight);
        }
    }

    private void drawEnteredString() {
        String formatted = padAddress(enteredAddress);
        int textWidth = this.fontRenderer.getStringWidth(formatted);
        this.fontRenderer.drawStringWithShadow(formatted, (this.width - textWidth) / 2.0F, this.dhdTop - 20, 0xFFFFFF);
    }

    private String padAddress(String address) {
        StringBuilder sb = new StringBuilder(address);
        while (sb.length() < addressLength) sb.append("-");
        return StargateAddressing.formatAddress(sb.toString());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0) {
            int buttonId = findDHDButton(mouseX, mouseY);
            if (buttonId >= 0) {
                dhdButtonPressed(buttonId);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private int findDHDButton(double mx, double my) {
        double x = -(mx - dhdCentreX);
        double y = -(my - dhdCentreY);

        if (y > 0 && Math.hypot(x, y) <= dhdRadius1) return 0;

        y = y * dhdWidth / dhdHeight;
        double r = Math.hypot(x, y);

        if (r > dhdRadius3 || r <= dhdRadius1) return -1;

        double a = Math.toDegrees(Math.atan2(y, x));
        if (a < 0) a += 360;

        int i0, nb;
        if (r > dhdRadius2) {
            i0 = 1; nb = 26;
        } else {
            i0 = 27; nb = 11;
        }

        return i0 + (int) Math.floor(a * nb / 360);
    }

    private void dhdButtonPressed(int id) {
        buttonSound();
        SGCraftReborn.LOGGER.log(Level.INFO, "buttonId: " + id);
        if (id == 0) {
            switch (status) {
                case LINKED:
                    if (enteredAddress.length() == 7 || enteredAddress.length() == 9) {
                        StargateNetwork.INSTANCE.sendToServer(new StargateDialPacket(this.controllerPos, this.enteredAddress));
                    }
                    break;
                case ACTIVATED:
                    StargateNetwork.INSTANCE.sendToServer(new StargateCloseVortexPacket(this.controllerPos));
                    break;
            }
            this.close();
        } else if (id >= 37) {
            backspace();
        } else {
            enterCharacter(SGCraftSymbolToChar(id - 1));
        }
    }

    private void buttonSound() {
        //this.mc.getSoundHandler().play(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        String c = String.valueOf(codePoint).toUpperCase();
        if (Character.isLetterOrDigit(codePoint)) {
            enterCharacter(c.charAt(0));
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.close();
            return true;
        }
        if (this.status == StargateControllerStatus.LINKED) {

        }
        if (keyCode == 259 || keyCode == 261) {
            backspace();
            return true;
        } else if (keyCode == 257 || keyCode == 335) {
            dhdButtonPressed(0);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void backspace() {
        if (!enteredAddress.isEmpty()) {
            buttonSound();
            enteredAddress = enteredAddress.substring(0, enteredAddress.length() - 1);
        }
    }

    private void enterCharacter(char c) {
        if (enteredAddress.length() < addressLength && c != '?') {
            buttonSound();
            enteredAddress += c;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    private char SGCraftSymbolToChar(int symbolIndex) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        if (symbolIndex >= 0 && symbolIndex < chars.length()) {
            return chars.charAt(symbolIndex);
        }
        return '?';
    }

    private int SGCraftCharToSymbol(char c) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        return chars.indexOf(Character.toUpperCase(c));
    }
}