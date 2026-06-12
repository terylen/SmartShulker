package net.smartshulker.mod.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.smartshulker.mod.screen.SmartShulkerScreenHandler;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SmartShulkerBlockEntity extends LootableContainerBlockEntity
        implements ExtendedScreenHandlerFactory<SmartShulkerBlockEntity.ScreenData> {

    // ── Particle colors ──────────────────────────────────────────────────────
    /** Bright red for "goal not yet met" */
    private static final DustParticleEffect PARTICLE_RED =
            new DustParticleEffect(new Vector3f(1.0f, 0.15f, 0.15f), 1.2f);
    /** Bright green for "goal met" */
    private static final DustParticleEffect PARTICLE_GREEN =
            new DustParticleEffect(new Vector3f(0.15f, 1.0f, 0.25f), 1.2f);

    /** How many ticks between each particle burst (20 ticks = 1 second) */
    private static final int PARTICLE_INTERVAL = 20;

    // ── Storage ───────────────────────────────────────────────────────────────
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);

    // ── Goal configuration ────────────────────────────────────────────────────
    /** Each ItemStack: item = the required item, count = required amount */
    private List<ItemStack> goalItems = new ArrayList<>();

    // ── State ─────────────────────────────────────────────────────────────────
    private boolean filled = false;
    private int viewerCount = 0;
    private int particleTick = 0;

    // ── Animation ─────────────────────────────────────────────────────────────
    private float openProgress = 0f;
    private float lastOpenProgress = 0f;

    public SmartShulkerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SMART_SHULKER_BLOCK_ENTITY, pos, state);
    }

    // ── Inventory ─────────────────────────────────────────────────────────────

    @Override public int size() { return 27; }
    @Override protected DefaultedList<ItemStack> getHeldStacks() { return inventory; }
    @Override protected void setHeldStacks(DefaultedList<ItemStack> list) { inventory = list; }

    @Override
    public Text getContainerName() {
        return Text.translatable("block.smartshulker.smart_shulker_box");
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new SmartShulkerScreenHandler(syncId, playerInventory, this);
    }

    // ── Goal logic ────────────────────────────────────────────────────────────

    public void setGoalItems(List<ItemStack> goals) {
        this.goalItems = new ArrayList<>(goals);
        checkFillState();
        markDirty();
    }

    public List<ItemStack> getGoalItems() { return goalItems; }

    /**
     * Checks whether all goal items are present in the required amounts.
     * Updates the {@code filled} flag and syncs to clients if it changed.
     */
    public void checkFillState() {
        boolean newFilled;
        if (goalItems.isEmpty()) {
            newFilled = false;
        } else {
            newFilled = true;
            for (ItemStack goal : goalItems) {
                if (goal.isEmpty()) continue;
                int found = 0;
                for (ItemStack slot : inventory) {
                    if (ItemStack.areItemsEqual(slot, goal)) found += slot.getCount();
                }
                if (found < goal.getCount()) { newFilled = false; break; }
            }
        }

        if (newFilled != filled) {
            filled = newFilled;
            // Reset particle tick so a burst fires immediately on state change
            particleTick = PARTICLE_INTERVAL - 1;
            sync();
        }
    }

    public boolean isFilled() { return filled; }

    // ── Server tick ──────────────────────────────────────────────────────────

    public static void tick(net.minecraft.world.World world, BlockPos pos,
                             BlockState state, SmartShulkerBlockEntity be) {
        // Lid animation
        be.lastOpenProgress = be.openProgress;
        if (be.viewerCount > 0) {
            be.openProgress = Math.min(be.openProgress + 0.1f, 1.0f);
        } else {
            be.openProgress = Math.max(be.openProgress - 0.1f, 0.0f);
        }

        // Particle logic — only on server, only when goals are configured
        if (!world.isClient && !be.goalItems.isEmpty()) {
            be.particleTick++;
            if (be.particleTick >= PARTICLE_INTERVAL) {
                be.particleTick = 0;
                be.spawnParticleBurst((ServerWorld) world, pos);
            }
        }
    }

    /**
     * Spawns a small burst of red or green dust particles rising from the top
     * of the box. Particles are spawned server-side and broadcast to clients.
     *
     * @param serverWorld the server world
     * @param pos         position of this block entity
     */
    private void spawnParticleBurst(ServerWorld serverWorld, BlockPos pos) {
        DustParticleEffect effect = filled ? PARTICLE_GREEN : PARTICLE_RED;

        // Spawn 5 particles in a small horizontal spread above the block
        for (int i = 0; i < 5; i++) {
            double x = pos.getX() + 0.2 + serverWorld.random.nextDouble() * 0.6;
            double y = pos.getY() + 1.05; // just above the top face
            double z = pos.getZ() + 0.2 + serverWorld.random.nextDouble() * 0.6;

            // Gentle upward drift with slight random horizontal wobble
            double vx = (serverWorld.random.nextDouble() - 0.5) * 0.05;
            double vy = 0.08 + serverWorld.random.nextDouble() * 0.06;
            double vz = (serverWorld.random.nextDouble() - 0.5) * 0.05;

            serverWorld.spawnParticles(effect, x, y, z,
                    1,   // count (1 so velocity is applied per-particle)
                    vx, vy, vz,
                    0.0  // speed (0 = use exact velocity above)
            );
        }
    }

    // ── Lid animation accessor (used by renderer) ─────────────────────────────

    public float getOpenProgress(float delta) {
        return lastOpenProgress + (openProgress - lastOpenProgress) * delta;
    }

    // ── Viewer tracking ───────────────────────────────────────────────────────

    @Override
    public void onOpen(PlayerEntity player) {
        if (!player.isSpectator()) { viewerCount++; markDirty(); }
    }

    @Override
    public void onClose(PlayerEntity player) {
        if (!player.isSpectator()) {
            if (--viewerCount < 0) viewerCount = 0;
            checkFillState();
            markDirty();
        }
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        Inventories.writeNbt(nbt, inventory, registries);
        nbt.putBoolean("Filled", filled);

        NbtList goalList = new NbtList();
        for (ItemStack goal : goalItems) {
            NbtCompound tag = new NbtCompound();
            goal.encode(registries, tag);
            goalList.add(tag);
        }
        nbt.put("GoalItems", goalList);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
        Inventories.readNbt(nbt, inventory, registries);
        filled = nbt.getBoolean("Filled");

        goalItems = new ArrayList<>();
        NbtList list = nbt.getList("GoalItems", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            ItemStack.fromNbt(registries, list.getCompound(i)).ifPresent(goalItems::add);
        }
    }

    // ── Network sync ──────────────────────────────────────────────────────────

    public void sync() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }

    // ── Extended screen factory ───────────────────────────────────────────────

    @Override
    public ScreenData getScreenOpeningData(ServerPlayerEntity player) {
        return new ScreenData(pos);
    }

    @Override
    public PacketCodec<RegistryByteBuf, ScreenData> getScreenOpeningDataCodec() {
        return ScreenData.CODEC;
    }

    public record ScreenData(BlockPos pos) {
        public static final PacketCodec<RegistryByteBuf, ScreenData> CODEC =
                PacketCodec.tuple(BlockPos.PACKET_CODEC, ScreenData::pos, ScreenData::new);
    }

    // ── ItemStack NBT helper (for pick-block) ─────────────────────────────────

    public void setStackNbt(ItemStack stack, RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = new NbtCompound();
        writeNbt(nbt, registries);
        if (!nbt.isEmpty()) {
            stack.set(net.minecraft.component.DataComponentTypes.BLOCK_ENTITY_DATA,
                    net.minecraft.component.type.NbtComponent.of(nbt));
        }
    }
}
