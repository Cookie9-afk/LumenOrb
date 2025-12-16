package lumenorbmod.event;

import lumenorbmod.item.LumenOrbItem;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import static lumenorbmod.utils.LumenOrbBehavior.pickupTorch;

public final class EventRegistry {
    private EventRegistry() {
    }

    // listens for the player starting to break a block
    public static void register() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            // check if holding the orb from the server
            if (!world.isClient()) {
                if (player.getOffHandStack().getItem() instanceof LumenOrbItem) {
                    return pickupTorch(player, world, Hand.OFF_HAND, pos);
                } else if (player.getStackInHand(hand).getItem() instanceof LumenOrbItem) {
                    return pickupTorch(player, world, hand, pos);
                }
            }
            return ActionResult.PASS;
        });
    }
}
