package net.maisiemarlowe.keirasaddons.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.maisiemarlowe.keirasaddons.KeirasAddons;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public static final TagKey<Block> ERMINE_SPAWNABLE_ON = TagKey.of(Registries.BLOCK.getKey(), new Identifier(KeirasAddons.MOD_ID, "ermine_spawnable_on"));

    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)



        ;
        getOrCreateTagBuilder(ModBlockTagProvider.ERMINE_SPAWNABLE_ON)
                .add(Blocks.GRASS_BLOCK)
                .add(Blocks.STONE)
                .add(Blocks.MYCELIUM)
                .add(Blocks.DIRT)
                .add(Blocks.COARSE_DIRT)
                .add(Blocks.ROOTED_DIRT)
                .add(Blocks.MUD)
                .add(Blocks.SNOW_BLOCK)
                .add(Blocks.PACKED_ICE)
                .add(Blocks.PACKED_MUD)


        ;



    }
}
