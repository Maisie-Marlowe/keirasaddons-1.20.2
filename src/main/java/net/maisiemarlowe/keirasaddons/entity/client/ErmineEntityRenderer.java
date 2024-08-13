package net.maisiemarlowe.keirasaddons.entity.client;

import net.maisiemarlowe.keirasaddons.entity.custom.ErmineEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ErmineEntityRenderer extends GeoEntityRenderer<ErmineEntity> {


    public ErmineEntityRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new ErmineModel());
    }

//    @Override
//    public Identifier getTextureLocation(ErmineEntity animatable) {
//        return new Identifier(KeirasAddons.MOD_ID, "textures/entity/ermine_texture.png");
//    }

    @Override
    public void render(ErmineEntity entity, float entityYaw, float partialTick, MatrixStack poseStack,
                       VertexConsumerProvider bufferSource, int packedLight) {
        if (entity.isAlive()) {
            poseStack.scale(0.7f, 0.7f, 0.7f);

        }
        if (entity.isDead()) {
            poseStack.scale(0.7f, 0.7f, 0.7f);
        }

        if (entity.isBaby()) {
            poseStack.scale(0.6f, 0.6f, 0.6f);
        }
//        if (entity.isBaby() && entity.isDead()) {
//            poseStack.scale(0.45f, 0.45f, 0.45f);
//        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}