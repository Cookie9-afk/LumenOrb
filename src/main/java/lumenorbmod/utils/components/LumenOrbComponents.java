package lumenorbmod.utils.components;

import com.mojang.serialization.Codec;
import lumenorbmod.LumenOrb;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;

public class LumenOrbComponents {
    public static void initialize() {
        LumenOrb.LOGGER.info("Registering {} components", LumenOrb.MOD_ID);
    }

    public static final Codec<DefaultedList<ItemStack>> INVENTORY_CODEC =
            ItemStack.CODEC.listOf().xmap(
                    list -> DefaultedList.copyOf(ItemStack.EMPTY, list.toArray(new ItemStack[0])),
                    ArrayList::new
            );

    public static final ComponentType<DefaultedList<ItemStack>> INVENTORY = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(LumenOrb.MOD_ID, "inventory"),
            ComponentType.<DefaultedList<ItemStack>>builder()
                    .codec(INVENTORY_CODEC)
                    .build()
    );

    public static final ComponentType<Integer> TORCH_CHARGES = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(LumenOrb.MOD_ID, "torch_charges"),
            ComponentType.<Integer>builder()
                    .codec(Codec.INT)
                    .build()
    );

    public static final ComponentType<Boolean> IS_LOCKED = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(LumenOrb.MOD_ID, "is_locked"),
            ComponentType.<Boolean>builder()
                    .codec(Codec.BOOL)
                    .build()
    );
}
