package net.maisiemarlowe.keirasaddons.world.gen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.maisiemarlowe.keirasaddons.entity.ModEntities;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BiomeKeys;

public class ModEntityGeneration {

    public static void addSpawns() {
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(
                BiomeKeys.SNOWY_SLOPES,
                BiomeKeys.SNOWY_PLAINS,
                BiomeKeys.SNOWY_TAIGA,
                BiomeKeys.FROZEN_PEAKS,
                BiomeKeys.PLAINS,
                BiomeKeys.CHERRY_GROVE,
                BiomeKeys.FLOWER_FOREST,
                BiomeKeys.FOREST,
                BiomeKeys.MEADOW,
                BiomeKeys.TAIGA
                ), SpawnGroup.CREATURE, ModEntities.ERMINE, 20, 1, 12);

        SpawnRestriction.register(ModEntities.ERMINE, SpawnRestriction.Location.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
    }

}
