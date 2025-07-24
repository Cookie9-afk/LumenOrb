package lumenorbmod.item;

import lumenorbmod.LumenOrb;
import lumenorbmod.screenhandler.LumenOrbScreenHandler;
import lumenorbmod.utils.PlacementJob;
import lumenorbmod.utils.TorchPlacerQueue;
import lumenorbmod.utils.components.LumenOrbComponents;
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
import net.minecraft.util.collection.DefaultedList;
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

        // while the item has less than 3\4 durability this will try to repair it, max is 288 durability, 1\4 * 288 = 72
        if(orb.getDamage() > 72){
            DefaultedList<ItemStack> items = orb.get(LumenOrbComponents.INVENTORY);
            while(orb.getDamage() > 72){
                // no fuel in the inventory? exit early
                if (items.size() == 0) break;

                // if we arrived here this means there's fuel in the inventory then we get the first ItemStack
                ItemStack fuel = items.getFirst();

                // rounding to allow items with 200+ burn time to be useful, planks for example
                int repairAmount = Math.round(LumenOrb.getFuelRegistry().getFuelTicks(fuel) / 400F);

                // once we get the amount we have to repair I call the repair method and decrement the ItemStack for the fuel
                repair(orb, repairAmount);
                items.getFirst().decrement(1);


                // Since decrementing the ItemStack can reduce the item to 0 amount then you would get air in the slot
                // instead I first clean the inventory then pass it back

                // After the job is done I update the orb inventory without the consumed item
                DefaultedList<ItemStack> sanitizedList = DefaultedList.copyOf(
                        ItemStack.EMPTY,
                        items.stream()
                                .filter(stack -> !stack.isEmpty())
                                .toArray(ItemStack[]::new)
                );

                items = sanitizedList;
                orb.set(LumenOrbComponents.INVENTORY, sanitizedList);
            }
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

    public void repair(ItemStack stack, int amount) {
        int currentDamage = stack.getDamage();
        stack.setDamage(currentDamage - amount);
    }
}
