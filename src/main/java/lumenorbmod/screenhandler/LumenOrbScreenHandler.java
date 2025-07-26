package lumenorbmod.screenhandler;

import lumenorbmod.LumenOrb;
import lumenorbmod.utils.InventoryManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class LumenOrbScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    // This constructor gets called on the client when the server wants it to open the screenHandler,
    // The client will call the other constructor with an empty Inventory and the screenHandler will automatically
    // sync this empty inventory with the inventory on the server.
    public LumenOrbScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new InventoryManager());
    }

    // This constructor gets called from the item on the server without calling the other constructor first, the server knows the inventory of the container
    // and can therefore directly provide it as an argument. This inventory will then be synced to the client.
    public LumenOrbScreenHandler(int syncId, PlayerInventory playerInventory, InventoryManager inventory) {
        super(LumenOrb.LUMEN_ORB_SCREEN_HANDLER, syncId);

        checkSize(inventory, 9);
        this.inventory = inventory;

        // I do some stuff when opening the inventory
        inventory.onOpen(playerInventory.player);

        // I do this to lock the player from moving the orb or that would cause a wrong managing of the orb's inventory
        int selectedHotbarSlot = playerInventory.selectedSlot; // player's current main hand slot

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
                        return LumenOrb.getFuelRegistry().isFuel(stack);
                    }
                });
            }
        }

        // The player inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }
        // The player Hotbar
        for (m = 0; m < 9; ++m) {

            // this makes the player unable to modify the ItemStack where the orb is located
            if (m != selectedHotbarSlot) this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
            else {
                this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean canTakeItems(PlayerEntity playerEntity) {
                        return false;
                    }
                });
            }
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
}
