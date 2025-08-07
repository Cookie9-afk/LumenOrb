package lumenorbmod.event;

import lumenorbmod.item.LumenOrbItem;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.util.ActionResult;

import static lumenorbmod.utils.LumenOrbBehavior.pickupTorch;

public final class EventRegistry {
    private EventRegistry() {
    }

    // listens for the player starting to break a block
    public static void register() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            // check if holding the orb
            if (!world.isClient && player.getStackInHand(hand).getItem() instanceof LumenOrbItem) {
                return pickupTorch(player, world, hand, pos);
            }
            return ActionResult.PASS;
        });
    }
}
