package lumenorbmod.utils;

import lumenorbmod.screenhandler.LumenOrbScreenHandler;
import lumenorbmod.utils.components.LumenOrbComponents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static lumenorbmod.utils.TorchPlacerQueue.validate;

public final class LumenOrbBehavior {
    private static final int COOLDOWN = 10;

    private LumenOrbBehavior(){}

    public static int getCooldown(){
        return COOLDOWN;
    }

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
        // I'm sure I can call it at least once because we entered the method with the same condition we'll repeat
        do{
            DefaultedList<ItemStack> orbInventory = orb.get(LumenOrbComponents.INVENTORY);

            // no fuel in the inventory? exit early
            if (orbInventory.size() == 0) break;

            // if we arrived here this means there's fuel in the inventory then we get the first ItemStack
            ItemStack itemToBurn = orbInventory.getFirst();

            // I first check the amount of burn ticks the item has
            int fuelTicks = MyFuelRegistry.getFuelRegistry().getFuelTicks(itemToBurn);

            // Since items rarely have exactly 400 fuel ticks, decrementing won't be perfect
            // I chose to make the item very useful despite some resource waste
            if (fuelTicks % 400 > 0) {

                int QuantityToBurn = (400 / fuelTicks) + 1;

                if (QuantityToBurn < itemToBurn.getCount()) addDurability(orb, 1);
                itemToBurn.decrement(QuantityToBurn);
            } else {
                addDurability(orb, fuelTicks / 400);
                itemToBurn.decrement(1);
            }

            // Since decrementing the ItemStack can reduce the item to 0 amount then you would get air in the slot
            // instead I first clean the inventory then pass it back

            // After the job is done I update the orb inventory without the consumed item
            DefaultedList<ItemStack> sanitizedList = DefaultedList.copyOf(
                    ItemStack.EMPTY,
                    orbInventory.stream()
                            .filter(stack -> !stack.isEmpty())
                            .toArray(ItemStack[]::new)
            );

            orb.set(LumenOrbComponents.INVENTORY, sanitizedList);
        } while(orb.getDamage() > 72);
    }

    // custom method to manage durability, avoiding exceeding max durability or reducing to less, 0-288 durability range
    private static void addDurability(ItemStack orb, int amount){
        int current = orb.getDamage();
        int max = orb.getMaxDamage();
        int newDamage = Math.min(max, Math.max(0, current - amount));
        orb.setDamage(newDamage);
    }

    public static ActionResult.Success openInventory(World world, PlayerEntity user, ItemStack orb){
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
                    return orb.getName();
                }
            };

            user.openHandledScreen(screenHandlerFactory);
        }
        return ActionResult.SUCCESS;
    }

    // picks up the broken torch
    public static ActionResult pickupTorch(PlayerEntity player, World world, Hand hand, BlockPos pos) {
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

            // After breaking, the torch gets absorbed by the orb item
            ItemStack orb = player.getStackInHand(hand);
            InventoryManager.incrementCharges(orb);

            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

}
