package lumenorbmod.item;

import lumenorbmod.LumenOrb;
import lumenorbmod.screenhandler.LumenOrbScreenHandler;
import lumenorbmod.utils.PlacementJob;
import lumenorbmod.utils.TorchPlacerQueue;
import lumenorbmod.utils.components.LumenOrbComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import static lumenorbmod.utils.TorchPlacerQueue.validate;

public final class LumenOrbBehavior {
    private LumenOrbBehavior(){}

    // Denies the usage
    public static ActionResult useFail(World world, PlayerEntity user){
        world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.1F, 0.8F);
        return ActionResult.FAIL;
    }

    // Confirms the usage
    public static ActionResult useSuccess(World world, PlayerEntity user, ItemStack usedItem){
        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.3F, 1.0F);
        TorchPlacerQueue.add(new PlacementJob(world, user, usedItem, validate(user.getBlockPos())));
        return ActionResult.SUCCESS;
    }

    public static void repair(ItemStack orb) {
        // the fuel inventory
        DefaultedList<ItemStack> items = orb.get(LumenOrbComponents.INVENTORY);

        // I'm sure I can call it at least once because if we entered the method with the same condition we'll repeat it
        do{
            // no fuel in the inventory? exit early
            if (items.size() == 0) break;

            // if we arrived here this means there's fuel in the inventory then we get the first ItemStack
            ItemStack fuel = items.getFirst();

            // rounding so items with 200+ burn time can be used for fuel, planks for example
            int repairAmount = Math.round(LumenOrb.getFuelRegistry().getFuelTicks(fuel) / 400F);

            // once we get the amount we have to repair I repair and decrement the ItemStack for the fuel
            orb.setDamage(orb.getDamage() - repairAmount);
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
        } while(orb.getDamage() > 72);
    }

    public static ActionResult.Success openInventory(World world, PlayerEntity user){
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
}
