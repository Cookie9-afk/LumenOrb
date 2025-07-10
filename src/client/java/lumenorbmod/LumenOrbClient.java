package lumenorbmod;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import lumenorbmod.screens.LumenOrbScreen;


public class LumenOrbClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		HandledScreens.register(LumenOrb.LUMEN_ORB_SCREEN_HANDLER, LumenOrbScreen::new);
	}
}