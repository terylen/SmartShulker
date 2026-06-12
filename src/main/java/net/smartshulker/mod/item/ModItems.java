package net.smartshulker.mod.item;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.smartshulker.mod.SmartShulkerMod;
import net.smartshulker.mod.block.ModBlocks;

public class ModItems {

    public static final BlockItem SMART_SHULKER_BOX_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of(SmartShulkerMod.MOD_ID, "smart_shulker_box"),
            new SmartShulkerBoxItem(ModBlocks.SMART_SHULKER_BOX, new Item.Settings().maxCount(1))
    );

    public static void register() {
        SmartShulkerMod.LOGGER.info("[SmartShulker] Items registered.");
    }
}
