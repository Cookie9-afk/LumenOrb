package lumenorbmod.utils;

import lumenorbmod.LumenOrb;
import lumenorbmod.item.LumenOrbItem;
import lumenorbmod.item.LumenOrbItemRegister;
import lumenorbmod.utils.components.LumenOrbComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public final class InventoryManager extends SimpleInventory {
    public InventoryManager(){
        super(9);
    }

    @Override
    public void onOpen(PlayerEntity player){
        ItemStack orb = player.getMainHandStack();
        if(orb.getItem() != LumenOrbItemRegister.LUMEN_ORB) orb = player.getOffHandStack();

        loadInventory(orb);
    }

    private void loadInventory(ItemStack orb) {
        DefaultedList<ItemStack> items = orb.get(LumenOrbComponents.INVENTORY);
        if (items != null) {
            heldStacks.clear();
            items.forEach(this::addStack);
        }
    }

    @Override
    public void onClose(PlayerEntity player){
        ItemStack orb = player.getMainHandStack();

        if(orb.getItem() != LumenOrbItemRegister.LUMEN_ORB) orb = player.getOffHandStack();

        saveInventory(orb);
    }

    private void saveInventory(ItemStack orb) {
        DefaultedList<ItemStack> sanitizedList = DefaultedList.copyOf(
                ItemStack.EMPTY,
                heldStacks.stream()
                        .filter(stack -> !stack.isEmpty())
                        .toArray(ItemStack[]::new)
        );

        // Adds a component to the item with the inventory parsed as string
        orb.set(LumenOrbComponents.INVENTORY, sanitizedList);
    }

    public static void incrementCharges(ItemStack orb){
        int charges = orb.getOrDefault(LumenOrbComponents.TORCH_CHARGES, 0);

        orb.set(LumenOrbComponents.TORCH_CHARGES, ++charges);
    }

    public static void decrementCharges(ItemStack orb){
        int charges = orb.getOrDefault(LumenOrbComponents.TORCH_CHARGES, 0);

        if(charges > 0) orb.set(LumenOrbComponents.TORCH_CHARGES, --charges);
    }

}