package net.maisiemarlowe.keirasaddons;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.maisiemarlowe.keirasaddons.data.*;

public class KeirasAddonsDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		//pack.addProvider(ModBlockTagProvider::new);
		//pack.addProvider(ModItemTagProvider::new);
		//pack.addProvider(ModLootTableProvider::new);
		pack.addProvider(ModModelProvider::new);
		//pack.addProvider(ModRecipeProvider::new);
		//pack.addProvider(ModWorldGenerator::new);
	}
}
