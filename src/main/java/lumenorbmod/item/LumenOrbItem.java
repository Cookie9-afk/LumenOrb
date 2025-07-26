package lumenorbmod.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;

import static lumenorbmod.item.LumenOrbBehavior.*;
import static lumenorbmod.utils.TorchPlacerQueue.hasDurability;

public class LumenOrbItem extends Item {
    public LumenOrbItem(Settings settings) {
        super(settings);
    }

    // Normal use, places the torches
    // Sneaking plus use, opens the orb's inventory
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack orb = user.getStackInHand(hand);

        // When opening the inventory the use methods stops early
        if(user.isSneaking()) return openInventory(world, user);

        // if the item has less than 3\4 durability this will try to repair it, max is 288 durability, 1\4 * 288 = 72
        if(orb.getDamage() > 72) repair(orb);

        // if the item has no durability to be used it just fails to be used
        if (!hasDurability(orb)) return useFail(world, user);

        // here I close for the client early because I want to do other stuff from server only
        // like managing the torch placing order, particle spawn etc
        else if (world.isClient)  return ActionResult.SUCCESS;

        // start of server managed stuff
        return useSuccess(world, user, orb);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("itemTooltip.lumenorbmod.lumen_orb").formatted(Formatting.GOLD));
    }
}
