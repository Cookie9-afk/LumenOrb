package lumenorbmod.item;

import lumenorbmod.utils.PlacementJob;
import lumenorbmod.utils.TorchPlacerQueue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import static lumenorbmod.utils.TorchPlacerQueue.hasDurability;
import static lumenorbmod.utils.TorchPlacerQueue.validate;

public class LumenOrbItem extends Item {
    public LumenOrbItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack usedItem = user.getStackInHand(hand);

        if (!hasDurability(usedItem)) return useFail(world, user);
        else if (world.isClient)  return ActionResult.SUCCESS;

        return useSuccess(world, user, usedItem);
    }

    // Denies the usage
    private ActionResult useFail(World world, PlayerEntity user){
        world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.1F, 0.8F);
        return ActionResult.FAIL;
    }

    // Confirms the usage
    private ActionResult useSuccess(World world, PlayerEntity user, ItemStack usedItem){
        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.3F, 1.0F);
        TorchPlacerQueue.add(new PlacementJob(world, user, usedItem, validate(user.getBlockPos())));
        return ActionResult.SUCCESS;
    }
}
