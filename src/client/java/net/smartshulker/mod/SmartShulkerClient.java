package net.smartshulker.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.smartshulker.mod.block.entity.ModBlockEntities;
import net.smartshulker.mod.render.SmartShulkerBlockEntityRenderer;
import net.smartshulker.mod.screen.ModScreenHandlers;
import net.smartshulker.mod.screen.SmartShulkerScreen;

public class SmartShulkerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.SMART_SHULKER_SCREEN_HANDLER,
                SmartShulkerScreen::new);

        BlockEntityRendererRegistry.register(
                ModBlockEntities.SMART_SHULKER_BLOCK_ENTITY,
                SmartShulkerBlockEntityRenderer::new
        );

        SmartShulkerMod.LOGGER.info("[SmartShulker] Client initialized.");
    }
}
