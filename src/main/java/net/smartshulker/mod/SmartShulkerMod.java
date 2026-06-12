package net.smartshulker.mod;

import net.fabricmc.api.ModInitializer;
import net.smartshulker.mod.block.ModBlocks;
import net.smartshulker.mod.block.entity.ModBlockEntities;
import net.smartshulker.mod.item.ModItems;
import net.smartshulker.mod.network.ModPackets;
import net.smartshulker.mod.screen.ModScreenHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartShulkerMod implements ModInitializer {

    public static final String MOD_ID = "smartshulker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModScreenHandlers.register();
        ModPackets.registerServerPackets();
        LOGGER.info("[SmartShulker] Initialized for MC 1.21.11");
    }
}
