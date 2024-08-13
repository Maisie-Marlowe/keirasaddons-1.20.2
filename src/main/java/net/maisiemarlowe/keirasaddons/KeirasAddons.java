package net.maisiemarlowe.keirasaddons;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.maisiemarlowe.keirasaddons.entity.ModEntities;
import net.maisiemarlowe.keirasaddons.entity.custom.ErmineEntity;
import net.maisiemarlowe.keirasaddons.item.ModItemGroup;
import net.maisiemarlowe.keirasaddons.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeirasAddons implements ModInitializer {
	public static final String MOD_ID = "keirasaddons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		FabricDefaultAttributeRegistry.register(ModEntities.ERMINE, ErmineEntity.setAttributes());

		ModItems.registerModItems();
		ModItemGroup.registerItemGroups();







	}
}