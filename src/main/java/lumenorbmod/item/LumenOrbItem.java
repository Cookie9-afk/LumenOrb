package lumenorbmod.item;

import lumenorbmod.screenhandler.LumenOrbScreenHandler;
import lumenorbmod.utils.PlacementJob;
import lumenorbmod.utils.TorchPlacerQueue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;

import static lumenorbmod.utils.TorchPlacerQueue.hasDurability;
import static lumenorbmod.utils.TorchPlacerQueue.validate;

public class LumenOrbItem extends Item {
    public LumenOrbItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack orb = user.getStackInHand(hand);

        if(user.isSneaking()){
            if (!world.isClient) {
                // Open your screen handler for the player on the server side
                NamedScreenHandlerFactory screenHandlerFactory = new NamedScreenHandlerFactory() {
                    @Override
                    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                        return new LumenOrbScreenHandler(syncId, playerInventory);
                    }

                    @Override
                    public Text getDisplayName() {
                        // This is the title of the GUI window
                        return Text.literal("Lumen Orb Inventory");
                    }
                };

                user.openHandledScreen(screenHandlerFactory);
            }
            return ActionResult.SUCCESS;
        }

        if (!hasDurability(orb)) return useFail(world, user);
        else if (world.isClient)  return ActionResult.SUCCESS;

        return useSuccess(world, user, orb);
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

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("itemTooltip.lumenorbmod.lumen_orb").formatted(Formatting.GOLD));
    }
}
