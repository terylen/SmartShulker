package net.smartshulker.mod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.smartshulker.mod.SmartShulkerMod;
import net.smartshulker.mod.network.GoalUpdatePayload;

import java.util.ArrayList;
import java.util.List;

public class SmartShulkerScreen extends HandledScreen<SmartShulkerScreenHandler> {

    private static final Identifier TEXTURE =
            Identifier.of(SmartShulkerMod.MOD_ID, "textures/gui/smart_shulker_box.png");

    // Goal slot highlight color (semi-transparent green tint)
    private static final int GOAL_SLOT_BG = 0x4400CC44;

    public SmartShulkerScreen(SmartShulkerScreenHandler handler,
                               PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth  = 176;
        this.backgroundHeight = 192;   // taller than default to fit goal row
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        int x = (width  - backgroundWidth)  / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        // Draw green highlight behind each goal slot
        for (int i = 0; i < SmartShulkerScreenHandler.GOAL_SLOTS; i++) {
            int sx = x + 8 + i * 18;
            int sy = y + 90;
            context.fill(sx, sy, sx + 16, sy + 16, GOAL_SLOT_BG);
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Title
        context.drawText(textRenderer, title, titleX, titleY, 0x404040, false);
        // Player inventory label
        context.drawText(textRenderer, playerInventoryTitle,
                playerInventoryTitleX, playerInventoryTitleY, 0x404040, false);
        // "Goal Items:" label above the goal slots
        context.drawText(textRenderer,
                Text.translatable("gui.smartshulker.goal_label")
                        .formatted(Formatting.DARK_GREEN),
                8, 80, 0x404040, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    /**
     * When the screen is closed, send the goal configuration to the server.
     */
    @Override
    public void removed() {
        super.removed();
        var be = handler.getBlockEntity();
        if (be != null && be.getPos() != null) {
            List<ItemStack> goals = new ArrayList<>();
            for (int i = 0; i < SmartShulkerScreenHandler.GOAL_SLOTS; i++) {
                ItemStack s = handler.getGoalInventory().getStack(i);
                if (!s.isEmpty()) goals.add(s.copy());
            }
            ClientPlayNetworking.send(new GoalUpdatePayload(be.getPos(), goals));
        }
    }
}
