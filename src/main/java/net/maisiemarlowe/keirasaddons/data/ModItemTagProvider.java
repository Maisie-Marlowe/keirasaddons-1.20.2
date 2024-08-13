package net.maisiemarlowe.keirasaddons.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.maisiemarlowe.keirasaddons.KeirasAddons;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public static final TagKey<Item> ERMINE_FOOD = TagKey.of(Registries.ITEM.getKey(), new Identifier(KeirasAddons.MOD_ID, "ermine_food"));

    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(ModItemTagProvider.ERMINE_FOOD)
                .add(Items.CHICKEN)
                .add(Items.COOKED_CHICKEN)
                .add(Items.COD)
                .add(Items.COOKED_COD)
                .add(Items.SALMON)
                .add(Items.COOKED_SALMON)
                .add(Items.RABBIT)
                .add(Items.COOKED_RABBIT)
                .add(Items.PORKCHOP)
                .add(Items.COOKED_PORKCHOP)
                .add(Items.MUTTON)
                .add(Items.COOKED_MUTTON)
                .add(Items.BEEF)
                .add(Items.COOKED_BEEF)
                .add(Items.EGG)
                .add(Items.TURTLE_EGG)


        ;

    }
}