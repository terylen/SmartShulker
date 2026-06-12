package net.smartshulker.mod.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.smartshulker.mod.block.entity.SmartShulkerBlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen layout (176 × 222 px GUI texture):
 *
 *   Row 0-2  (y=18..71):   27 storage slots (3×9)
 *   Divider + "Zielitems:" label (y=82)
 *   Row goal (y=90):        5 goal slots  (1×5)
 *   Row 0-2  (y=112..166): player inventory (3×9)
 *   Row hot  (y=170):       hotbar (1×9)
 */
public class SmartShulkerScreenHandler extends ScreenHandler {

    public static final int GOAL_SLOTS = 5;
    private static final int STORAGE_SIZE = 27;

    private final SmartShulkerBlockEntity blockEntity;
    private final SimpleInventory goalInventory;

    // ── Server-side constructor ───────────────────────────────────────────────
    public SmartShulkerScreenHandler(int syncId, PlayerInventory playerInv,
                                      SmartShulkerBlockEntity be) {
        super(ModScreenHandlers.SMART_SHULKER_SCREEN_HANDLER, syncId);
        this.blockEntity = be;

        // Build goal inventory from stored goals
        this.goalInventory = new SimpleInventory(GOAL_SLOTS);
        List<ItemStack> stored = be.getGoalItems();
        for (int i = 0; i < Math.min(stored.size(), GOAL_SLOTS); i++) {
            goalInventory.setStack(i, stored.get(i).copy());
        }

        be.onOpen(playerInv.player);
        checkSize(be, STORAGE_SIZE);

        // ── Storage slots ─────────────────────────────────────────────────────
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(be, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // ── Goal slots (green-tinted, 1 × 5) ─────────────────────────────────
        for (int i = 0; i < GOAL_SLOTS; i++) {
            addSlot(new GoalSlot(goalInventory, i, 8 + i * 18, 90));
        }

        // ── Player inventory ──────────────────────────────────────────────────
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 112 + row * 18));
            }
        }

        // ── Hotbar ────────────────────────────────────────────────────────────
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 170));
        }
    }

    // ── Client-side constructor ───────────────────────────────────────────────
    public SmartShulkerScreenHandler(int syncId, PlayerInventory playerInv,
                                      SmartShulkerBlockEntity.ScreenData data) {
        // On the client we don't have the real block entity; placeholders are fine
        // because the server saves goals on close via onClosed()
        this(syncId, playerInv, createClientDummy(data));
    }

    /** Creates a temporary client-side stand-in block entity. */
    private static SmartShulkerBlockEntity createClientDummy(
            SmartShulkerBlockEntity.ScreenData data) {
        // The dummy is only used to satisfy the constructor; the server is authoritative
        return new SmartShulkerBlockEntity(data.pos(), null) {
            @Override public boolean canPlayerUse(net.minecraft.entity.player.PlayerEntity p) { return true; }
        };
    }

    // ── Quick-move (Shift+click) ──────────────────────────────────────────────
    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasStack()) return ItemStack.EMPTY;

        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();

        // Goal slots: no quick-move
        if (index >= STORAGE_SIZE && index < STORAGE_SIZE + GOAL_SLOTS) {
            return ItemStack.EMPTY;
        }

        if (index < STORAGE_SIZE) {
            // Storage → player
            if (!insertItem(stack, STORAGE_SIZE + GOAL_SLOTS,
                    STORAGE_SIZE + GOAL_SLOTS + 36, true)) return ItemStack.EMPTY;
        } else {
            // Player → storage
            if (!insertItem(stack, 0, STORAGE_SIZE, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();

        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTakeItem(player, stack);
        return original;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return blockEntity == null || blockEntity.canPlayerUse(player);
    }

    /** Saves goal items back to the block entity when the screen is closed. */
    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (blockEntity != null && !player.getWorld().isClient) {
            List<ItemStack> goals = new ArrayList<>();
            for (int i = 0; i < GOAL_SLOTS; i++) {
                ItemStack s = goalInventory.getStack(i);
                if (!s.isEmpty()) goals.add(s.copy());
            }
            blockEntity.setGoalItems(goals);
            blockEntity.onClose(player);
        }
    }

    public SmartShulkerBlockEntity getBlockEntity() { return blockEntity; }
    public SimpleInventory getGoalInventory() { return goalInventory; }

    // ── Inner class: goal slot ─────────────────────────────────────────────────
    /** Accepts any item; count = how many of that item are required. */
    public static class GoalSlot extends Slot {
        public GoalSlot(net.minecraft.inventory.Inventory inv, int index, int x, int y) {
            super(inv, index, x, y);
        }
        @Override public int getMaxItemCount() { return 64; }
        @Override public boolean canInsert(ItemStack stack) { return true; }
    }
}
