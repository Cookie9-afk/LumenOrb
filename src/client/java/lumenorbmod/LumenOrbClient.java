package lumenorbmod;

import lumenorbmod.event.EventRegistry;
import lumenorbmod.item.LumenOrbItem;
import lumenorbmod.item.LumenOrbItemRegister;
import lumenorbmod.screens.LumenOrbScreen;
import lumenorbmod.utils.components.LumenOrbComponents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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

			// adds the orb's tooltip
			ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipType, list) -> {
				if (!itemStack.isOf(LumenOrbItemRegister.LUMEN_ORB)) {
					return;
				}

				list.add(
						Text.translatable("itemTooltip.lumenorbmod.lumen_orb")
								.formatted(Formatting.GOLD)
				);

				int charges = itemStack.get(LumenOrbComponents.TORCH_CHARGES);
				if(charges > 0){
					list.add(Text.translatable("itemTooltip.lumenorbmod.charges", charges)
							.formatted(Formatting.GRAY));
				}
			});

		}catch (Exception e){
			LOGGER.info("Failed to correctly initialize client: " + e.getMessage());
		}
	}
}