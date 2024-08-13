package net.maisiemarlowe.keirasaddons.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.maisiemarlowe.keirasaddons.KeirasAddons;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroup {

    public static final ItemGroup KEIRAS_ADDONS_GROUP = Registry.register(Registries.ITEM_GROUP, new Identifier(KeirasAddons.MOD_ID, "keirasaddons"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.keirasaddons"))
                    .icon(() -> new ItemStack(Items.BUCKET)).entries(((displayContext, entries) -> {

                        entries.add(ModItems.ERMINE_SPAWN_EGG);




                    })).build());


    public static void registerItemGroups() {
        KeirasAddons.LOGGER.info("Registering Item Groups for " + KeirasAddons.MOD_ID);
    }
}
