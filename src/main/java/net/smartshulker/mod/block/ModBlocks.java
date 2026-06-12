package net.smartshulker.mod.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.smartshulker.mod.SmartShulkerMod;
import net.smartshulker.mod.item.ModItems;

public class ModBlocks {

    public static final SmartShulkerBlock SMART_SHULKER_BOX = Registry.register(
            Registries.BLOCK,
            Identifier.of(SmartShulkerMod.MOD_ID, "smart_shulker_box"),
            new SmartShulkerBlock(
                    AbstractBlock.Settings.create()
                            .strength(2.0f)
                            .dynamicBounds()
                            .nonOpaque()
                            .suffocates((s, w, p) -> false)
                            .blockVision((s, w, p) -> false)
            )
    );

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries ->
                entries.add(ModItems.SMART_SHULKER_BOX_ITEM));
    }
}
