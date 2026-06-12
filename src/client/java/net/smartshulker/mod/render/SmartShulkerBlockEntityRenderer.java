package net.smartshulker.mod.render;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.smartshulker.mod.SmartShulkerMod;
import net.smartshulker.mod.block.SmartShulkerBlock;
import net.smartshulker.mod.block.entity.SmartShulkerBlockEntity;

public class SmartShulkerBlockEntityRenderer
        implements BlockEntityRenderer<SmartShulkerBlockEntity> {

    /** Single neutral texture — particles carry the color signal, not the block */
    private static final Identifier TEXTURE =
            Identifier.of(SmartShulkerMod.MOD_ID, "textures/block/smart_shulker.png");

    private final ModelPart base;
    private final ModelPart lid;

    public SmartShulkerBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();

        root.addChild("base",
                ModelPartBuilder.create().uv(0, 28)
                        .cuboid(-8f, -8f, -8f, 16f, 12f, 16f),
                ModelTransform.pivot(8f, 8f, 8f));

        root.addChild("lid",
                ModelPartBuilder.create().uv(0, 0)
                        .cuboid(-8f, -4f, -8f, 16f, 4f, 16f),
                ModelTransform.pivot(8f, 4f, 8f));

        ModelPart built = TexturedModelData.of(data, 64, 64).createModel();
        this.base = built.getChild("base");
        this.lid  = built.getChild("lid");
    }

    @Override
    public void render(SmartShulkerBlockEntity entity, float tickDelta,
                        MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                        int light, int overlay) {

        Direction facing = entity.getCachedState().get(SmartShulkerBlock.FACING);

        float open = entity.getOpenProgress(tickDelta);
        // Ease-in-out curve so the lid doesn't snap open
        open = 0.5f * (1.0f - MathHelper.cos((float) Math.PI * open));

        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);

        // Rotate model to match block facing direction
        switch (facing) {
            case DOWN  -> { matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
                            matrices.translate(0, -1, 0); }
            case SOUTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
            case WEST  -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
            case EAST  -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
            case NORTH, UP -> { /* default orientation */ }
        }

        matrices.translate(-0.5, -0.5, -0.5);

        // Animate lid (pivot Y moves up as box opens)
        lid.pivotY = 4.0f + open * -8.0f;

        VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));
        base.render(matrices, vc, light, overlay);
        lid.render(matrices, vc, light, overlay);

        matrices.pop();
    }
}
