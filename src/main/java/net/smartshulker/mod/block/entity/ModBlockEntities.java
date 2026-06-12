package net.smartshulker.mod.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.smartshulker.mod.SmartShulkerMod;
import net.smartshulker.mod.block.ModBlocks;

public class ModBlockEntities {

    public static final BlockEntityType<SmartShulkerBlockEntity> SMART_SHULKER_BLOCK_ENTITY =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    Identifier.of(SmartShulkerMod.MOD_ID, "smart_shulker_block_entity"),
                    FabricBlockEntityTypeBuilder
                            .create(SmartShulkerBlockEntity::new, ModBlocks.SMART_SHULKER_BOX)
                            .build()
            );

    public static void register() {
        SmartShulkerMod.LOGGER.info("[SmartShulker] Block entities registered.");
    }
}
