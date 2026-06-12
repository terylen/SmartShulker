package net.smartshulker.mod.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.smartshulker.mod.block.entity.SmartShulkerBlockEntity;

public class ModPackets {

    public static void registerServerPackets() {
        // Register the payload type for C2S traffic
        PayloadTypeRegistry.playC2S().register(GoalUpdatePayload.ID, GoalUpdatePayload.CODEC);

        // Handle goal updates sent from the client when the screen closes
        ServerPlayNetworking.registerGlobalReceiver(GoalUpdatePayload.ID, (payload, ctx) ->
                ctx.server().execute(() -> {
                    var world = ctx.player().getWorld();
                    if (world.getBlockEntity(payload.pos()) instanceof SmartShulkerBlockEntity be) {
                        be.setGoalItems(payload.goals());
                    }
                })
        );
    }

    public static void registerClientPackets() {
        // Payload type must also be registered on the client side for the codec to work
        PayloadTypeRegistry.playC2S().register(GoalUpdatePayload.ID, GoalUpdatePayload.CODEC);
    }
}
