package lumenorbmod.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;

public class PlacementJob {
    private final World world;
    private final PlayerEntity user;
    private final ItemStack itemUsed;
    private final Iterator<BlockPos> positions;

    public PlacementJob(World world, PlayerEntity user, ItemStack itemUsed, List<BlockPos> positions) {
        this.world = world;
        this.user = user;
        this.itemUsed = itemUsed;
        this.positions = positions.iterator();
    }

    public World getWorld() {
        return world;
    }

    public PlayerEntity getUser() {
        return user;
    }

    public ItemStack getItemUsed() {
        return itemUsed;
    }

    public Iterator<BlockPos> getPositions() {
        return positions;
    }
}

