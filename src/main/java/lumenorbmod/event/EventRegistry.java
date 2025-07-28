package lumenorbmod.event;

import lumenorbmod.item.LumenOrbItem;
import lumenorbmod.utils.InventoryManager;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static lumenorbmod.utils.LumenOrbBehavior.getCooldown;
import static lumenorbmod.utils.LumenOrbBehavior.retrieveTorches;

public final class EventRegistry {
    private EventRegistry() {
    }

    // swing -> places a torch to the position the player is looking
    // sneaking plus swing -> checks the same area it uses to place torches but picks them up instead
    public static void register() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!world.isClient && player.getStackInHand(hand).getItem() instanceof LumenOrbItem) {
                if (player.isSneaking()) return pickupTorchArea(player, world, hand, pos);
                else return pickupTorch(player, world, hand, pos);
            }
            return ActionResult.PASS;
        });
    }

    // places a torch to the position the player is looking
    private static ActionResult pickupTorchArea(PlayerEntity player, World world, Hand hand, BlockPos pos) {
        if (player.getItemCooldownManager().isCoolingDown(player.getStackInHand(hand)))
            return ActionResult.PASS;

        // picks up the torches and puts them in the item's nbt
        retrieveTorches(world, player, pos, player.getStackInHand(hand));

        // Add cooldown for the item
        player.getItemCooldownManager().set(player.getStackInHand(hand), getCooldown());
        return ActionResult.SUCCESS;
    }

    // checks the same area it uses to place torches but picks them up instead
    private static ActionResult pickupTorch(PlayerEntity player, World world, Hand hand, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (state.isOf(Blocks.TORCH) || state.isOf(Blocks.WALL_TORCH)) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());

            // play a sound to confirm the item interacted with the torch
            world.playSound(
                    null,
                    pos,
                    SoundEvents.ENTITY_ITEM_PICKUP,
                    SoundCategory.PLAYERS,
                    0.3f,
                    1.0f
            );

            // After breaking the torch is gets absorbed by orb item
            ItemStack orb = player.getStackInHand(hand);
            InventoryManager.incrementCharges(orb);

            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
