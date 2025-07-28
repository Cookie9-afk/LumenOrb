package lumenorbmod;

import lumenorbmod.event.EventRegistry;
import lumenorbmod.screens.LumenOrbScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

import static lumenorbmod.LumenOrb.LOGGER;


public class LumenOrbClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		HandledScreens.register(LumenOrb.LUMEN_ORB_SCREEN_HANDLER, LumenOrbScreen::new);

		LOGGER.info("initializing client lumenorb...");
		try{
			// Register the left click activation for the item
			EventRegistry.register();

		}catch (Exception e){
			LOGGER.info("Failed to correctly initialize client: " + e.getMessage());
		}
	}
}