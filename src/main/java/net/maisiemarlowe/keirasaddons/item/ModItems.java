package net.maisiemarlowe.keirasaddons.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.maisiemarlowe.keirasaddons.KeirasAddons;
import net.maisiemarlowe.keirasaddons.entity.ModEntities;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item ERMINE_SPAWN_EGG = registerItem("ermine_spawn_egg", new SpawnEggItem(ModEntities.ERMINE, 0x9d2f18,
            0xfeffda, new FabricItemSettings()));



    private static Item registerItem(String name, Item item){
        return Registry.register(Registries.ITEM, new Identifier(KeirasAddons.MOD_ID, name), item);
    }

    public static void registerModItems() {
        KeirasAddons.LOGGER.info("Registering Mod Items for " + KeirasAddons.MOD_ID);
    }
}


