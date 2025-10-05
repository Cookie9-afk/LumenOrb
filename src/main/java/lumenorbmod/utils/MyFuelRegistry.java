package lumenorbmod.utils;

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;

import static lumenorbmod.LumenOrb.LOGGER;

public final class MyFuelRegistry {
    private static net.minecraft.item.FuelRegistry FUEL_REGISTRY;

    private MyFuelRegistry(){}

    public static void initialize(MinecraftServer server) {
        LOGGER.info("initializing custom FuelRegistry...");
        // stores the fuelRegistry reference

        RegistryWrapper.WrapperLookup registries = server.getRegistryManager();
        FeatureSet featureSet = server.getSaveProperties().getEnabledFeatures();

        FUEL_REGISTRY = net.minecraft.item.FuelRegistry.createDefault(registries, featureSet);
        LOGGER.info("FuelRegistry initialized");
    }

    public static net.minecraft.item.FuelRegistry getFuelRegistry(){
        return FUEL_REGISTRY;
    }
}
