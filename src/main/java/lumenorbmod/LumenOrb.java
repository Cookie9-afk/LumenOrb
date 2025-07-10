package lumenorbmod;

import lumenorbmod.item.LumenOrbItemRegister;
import lumenorbmod.screenhandler.LumenOrbScreenHandler;
import net.fabricmc.api.ModInitializer;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LumenOrb implements ModInitializer {
	public static final String MOD_ID = "lumenorb";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final ScreenHandlerType<LumenOrbScreenHandler> LUMEN_ORB_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "lumen_orb_screen"), new ScreenHandlerType<>(LumenOrbScreenHandler::new, FeatureSet.empty()));

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("initializing lumenorb...");
		try{
			LumenOrbItemRegister.initialize();
		}catch (Exception e){
			System.out.println("Failed to correctly initialize: " + e.getMessage());
		}
	}
}