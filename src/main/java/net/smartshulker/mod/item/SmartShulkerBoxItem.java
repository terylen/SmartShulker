package net.smartshulker.mod.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

public class SmartShulkerBoxItem extends BlockItem {

    public SmartShulkerBoxItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context,
                               List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.translatable("item.smartshulker.smart_shulker_box.tooltip.line1")
                .formatted(net.minecraft.util.Formatting.GRAY));
        tooltip.add(Text.translatable("item.smartshulker.smart_shulker_box.tooltip.line2")
                .formatted(net.minecraft.util.Formatting.DARK_GRAY));
    }
}
