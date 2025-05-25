package lumenorbmod.item;

import lumenorbmod.LumenOrb;
import lumenorbmod.utils.TorchPlacerQueue;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class LumenOrbItemRegister {
    public static final Item LUMEN_ORB = register("lumen_orb", LumenOrbItem::new, new Item.Settings().maxDamage(288));

    private LumenOrbItemRegister(){}

    public static Item register(String path, Function<Item.Settings, Item> factory, Item.Settings settings) {
        final RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(LumenOrb.MOD_ID, path));
        return Items.register(registryKey, factory, settings);
    }

    public static void initialize() {
        // adds the item in the creative functional tab
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                .register((itemGroup) -> itemGroup.add(LUMEN_ORB));

        TorchPlacerQueue.startQueue();
    }
}

