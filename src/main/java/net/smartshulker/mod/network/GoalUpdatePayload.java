package net.smartshulker.mod.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.smartshulker.mod.SmartShulkerMod;

import java.util.List;

/**
 * Sent client → server when the screen closes, carrying the new goal list.
 */
public record GoalUpdatePayload(BlockPos pos, List<ItemStack> goals)
        implements CustomPayload {

    public static final Id<GoalUpdatePayload> ID =
            new Id<>(Identifier.of(SmartShulkerMod.MOD_ID, "goal_update"));

    public static final PacketCodec<RegistryByteBuf, GoalUpdatePayload> CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC, GoalUpdatePayload::pos,
                    ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()),
                    GoalUpdatePayload::goals,
                    GoalUpdatePayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
