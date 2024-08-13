package net.maisiemarlowe.keirasaddons;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.maisiemarlowe.keirasaddons.entity.ModEntities;
import net.maisiemarlowe.keirasaddons.entity.client.ErmineEntityRenderer;

public class KeirasAddonsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {


        EntityRendererRegistry.register(ModEntities.ERMINE, ErmineEntityRenderer::new);
    }
}
