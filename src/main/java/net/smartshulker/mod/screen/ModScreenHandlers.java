package net.smartshulker.mod.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.smartshulker.mod.SmartShulkerMod;
import net.smartshulker.mod.block.entity.SmartShulkerBlockEntity;

public class ModScreenHandlers {

    public static final ScreenHandlerType<SmartShulkerScreenHandler> SMART_SHULKER_SCREEN_HANDLER =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of(SmartShulkerMod.MOD_ID, "smart_shulker_screen"),
                    new ExtendedScreenHandlerType<>(
                            SmartShulkerScreenHandler::new,
                            SmartShulkerBlockEntity.ScreenData.CODEC
                    )
            );

    public static void register() {
        SmartShulkerMod.LOGGER.info("[SmartShulker] Screen handlers registered.");
    }
}
