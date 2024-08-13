package net.maisiemarlowe.keirasaddons.entity.client;

import net.maisiemarlowe.keirasaddons.KeirasAddons;
import net.maisiemarlowe.keirasaddons.entity.custom.ErmineEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class ErmineModel extends GeoModel<ErmineEntity> {
    @Override
    public Identifier getModelResource(ErmineEntity animatable) {
        return new Identifier(KeirasAddons.MOD_ID, "geo/ermine.geo.json");
    }

    @Override
    public Identifier getTextureResource(ErmineEntity animatable) {
        return new Identifier(KeirasAddons.MOD_ID, "textures/entity/ermine_texture.png");
    }

    @Override
    public Identifier getAnimationResource(ErmineEntity animatable) {
        return new Identifier(KeirasAddons.MOD_ID, "animations/ermine.animation.json");
    }

    @Override
    public void setCustomAnimations(ErmineEntity animatable, long instanceId, AnimationState<ErmineEntity> animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");

        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * MathHelper.RADIANS_PER_DEGREE);
            head.setRotY(entityData.netHeadYaw() * MathHelper.RADIANS_PER_DEGREE);
        }


    }
}
