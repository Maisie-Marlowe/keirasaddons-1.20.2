package net.maisiemarlowe.keirasaddons.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.maisiemarlowe.keirasaddons.KeirasAddons;
import net.maisiemarlowe.keirasaddons.entity.custom.ErmineEntity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {

public static final EntityType<ErmineEntity> ERMINE = Registry.register(Registries.ENTITY_TYPE, new Identifier(KeirasAddons.MOD_ID, "ermine"),
        FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, ErmineEntity::new)
                .dimensions(EntityDimensions.fixed(1.4f, .5f)).build());


}
