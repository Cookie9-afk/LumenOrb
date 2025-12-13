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

    // This method is going to check every slot of the orb and if that slot can repair the orb by at least 1 durability it will try to
    public static void repair(ItemStack orb) {
        DefaultedList<ItemStack> orbInventory = orb.get(LumenOrbComponents.INVENTORY);

        for(ItemStack slot : orbInventory){
            // if that slot is empty will skip it
            //if (slot.isEmpty()) continue;

            // since the slot it's not empty it will check if it can consume the material to repair
            int fuelTicks = MyFuelRegistry.getFuelRegistry().getFuelTicks(slot);

            while (orb.getDamage() > 72){
                // is burn time a multiple of 400?
                if(fuelTicks % 400 == 0){
                    // since 400 burn ticks equals to 1 durability
                    slot.decrement(1);
                    addDurability(orb, 1);
                    if(slot.getCount() <= 0) break;
                }else{
                    // if it's not
                    int itemsNeededForRepair = (int) Math.ceil(400f / fuelTicks);
                    if(slot.getCount() >= itemsNeededForRepair){
                        slot.decrement(itemsNeededForRepair);
                        addDurability(orb, itemsNeededForRepair - 1);
                    }
                    if(slot.getCount() < itemsNeededForRepair) break;
                }
            }

            // After the job is done I update the orb inventory without the consumed item
            DefaultedList<ItemStack> sanitizedList = DefaultedList.copyOf(
                    ItemStack.EMPTY,
                    orbInventory.stream()
                            .filter(stack -> !stack.isEmpty())
                            .toArray(ItemStack[]::new)
            );

            orb.set(LumenOrbComponents.INVENTORY, sanitizedList);

        }
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
            // opens screen handler for the player from the server side
            NamedScreenHandlerFactory screenHandlerFactory = new NamedScreenHandlerFactory() {
                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                    return new LumenOrbScreenHandler(syncId, playerInventory);
                }

                @Override
                public Text getDisplayName() {
                    // title of the GUI window
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
