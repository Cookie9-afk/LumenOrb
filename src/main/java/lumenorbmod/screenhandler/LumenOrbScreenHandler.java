package lumenorbmod.screenhandler;

import lumenorbmod.LumenOrb;
import lumenorbmod.item.LumenOrbItemRegister;
import lumenorbmod.utils.InventoryManager;
import lumenorbmod.utils.MyFuelRegistry;
import lumenorbmod.utils.components.LumenOrbComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class LumenOrbScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    // This constructor gets called on the client when the server wants it to open
    // the screenHandler,
    // The client will call the other constructor with an empty Inventory and the
    // screenHandler will automatically
    // sync this empty inventory with the inventory on the server.
    public LumenOrbScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new InventoryManager());
    }

    // This constructor gets called from the item on the server without calling the
    // other constructor first, the server knows the inventory of the container
    // and can therefore directly provide it as an argument. This inventory will
    // then be synced to the client.
    public LumenOrbScreenHandler(int syncId, PlayerInventory playerInventory, InventoryManager inventory) {
        super(LumenOrb.LUMEN_ORB_SCREEN_HANDLER, syncId);

        checkSize(inventory, 9);
        this.inventory = inventory;

        // I do some stuff when opening the inventory
        inventory.onOpen(playerInventory.player);

        // 3x3 Grid
        int m;
        int l;

        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 3; ++l) {
                int index = l + m * 3;
                int x = 62 + l * 18;
                int y = 17 + m * 18;

                this.addSlot(new Slot(inventory, index, x, y) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        // Only allow items that have a burn time (i.e. are fuel)
                        return MyFuelRegistry.getFuelRegistry().isFuel(stack);
                    }
                });
            }
        }

        // The player inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18) {
                    @Override
                    public boolean canTakeItems(PlayerEntity PlayerEntity) {
                        ItemStack stack = this.getStack();
                        boolean locked = stack.getOrDefault(LumenOrbComponents.IS_LOCKED,false);
                        return !locked;
                    }
                });

            }
        }
        // The player Hotbar
        for (m = 0; m < 9; ++m)

        {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142) {
                @Override
                public boolean canTakeItems(PlayerEntity PlayerEntity) {
                    ItemStack stack = this.getStack();
                    boolean locked = stack.getOrDefault(LumenOrbComponents.IS_LOCKED,false);
                    return !locked;
                }
            });
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    // Shift + Player Inv Slot
    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);

        if (inventory instanceof InventoryManager manager) {
            manager.onClose(player);
        }

    }
/*
    // locks the orb in the inventory
    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        System.out.println(slotIndex + " " + button + " " + actionType);

        // -1 and -999 handles clicks outside slots
        if (slotIndex == -1 || slotIndex == -999)
            super.onSlotClick(slotIndex, button, actionType, player);
        else {
            // item stack at selected slot, ends early if clicked an empty slot
            ItemStack clickedStack = getSlot(slotIndex).getStack();

            // button pressed, 40 is offhand, default 'F' keybind, 1 for left click, 2 for
            // right click
            ItemStack buttonStack = (button == 40) ? player.getOffHandStack() : player.getInventory().main.get(button);

            // the item stack held in hand
            ItemStack heldStack = player.getInventory().getMainHandStack();

            boolean isSwapping = actionType == SlotActionType.SWAP;

            // is the held stack the same as the clicked stack
            boolean isHeldOrb = heldStack == clickedStack;

            // Prevent swapping the held orb
            if (isSwapping) {

                // Case 1: Empty clicked slot & buttonStack is not the held orb
                if (clickedStack.isEmpty() && buttonStack != heldStack) {
                    super.onSlotClick(slotIndex, button, actionType, player);
                    return;
                }

                // Case 2: Trying to swap the orb itself
                if (isOrb(clickedStack) && isHeldOrb) {
                    System.out.println("Cannot swap Lumen Orb!");
                    return;
                }

                // Case 3: Normal item swap (not the orb)
                if (!isOrb(buttonStack)) {
                    super.onSlotClick(slotIndex, button, actionType, player);
                }

            } else {
                // Not swapping & held item is not orb
                if (!isHeldOrb) {
                    super.onSlotClick(slotIndex, button, actionType, player);
                    net.minecraft.item.BundleItem.getBundles();
                }
            }
        }
    }
*/
    private boolean isOrb(ItemStack item) {
        return item.isOf(LumenOrbItemRegister.LUMEN_ORB);
    }
}
