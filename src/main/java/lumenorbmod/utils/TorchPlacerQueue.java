package lumenorbmod.utils;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.*;

import static lumenorbmod.utils.InventoryManager.decrementCharges;
import static lumenorbmod.utils.components.LumenOrbComponents.TORCH_CHARGES;
import static net.minecraft.block.Blocks.TORCH;

public class TorchPlacerQueue {
    private static final Queue<PlacementJob> jobQ = new LinkedList<>();

    /**
     * adds a job in the queue
     * @param job is the job called when right-clicking the item
     */
    public static void add(PlacementJob job) {
        if(!hasDurability(job.getItemUsed())) return;

        BlockPos playerPos = job.getUser().getBlockPos();

        List<BlockPos> validPositions = validate(playerPos).stream()
                .filter(position -> canPlaceTorch(job.getWorld(), position))
                .toList();

        if (!validPositions.isEmpty()) {
            jobQ.add(new PlacementJob(job.getWorld(), job.getUser(), job.getItemUsed(), validPositions));
        }
    }

    /**
     * Checks if the item has durability left
     * @param itemUsed the item checked
     * @return true when the item has durability
     */
    public static boolean hasDurability(ItemStack itemUsed){
        int durabilityLeft = itemUsed.getMaxDamage() - itemUsed.getDamage();
        return durabilityLeft >= 1;
    }

    /**
     * returns a cube around the player of radius 8 height radius 3
     * @param playerPosition is the player position at the moment of the call
     * @return all the cube coordinates
     */
    public static List<BlockPos> validate(BlockPos playerPosition) {
        List<BlockPos> spots = new ArrayList<>();

        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                for (int dy = -3; dy <= 3; dy++) {
                    spots.add(playerPosition.add(dx, dy, dz));
                }
            }
        }

        return spots;
    }

    /**
     * starts the queue having it compute a job each tick
     */
    public static void startQueue() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!jobQ.isEmpty()) {
                PlacementJob job = jobQ.peek();
                if (tryPlaceNext(job.getWorld(), job.getUser(), job.getItemUsed(), job.getPositions())) {
                    jobQ.poll(); // remove completed job
                }
            }
        });
    }

    /**
     * Attempts to place a torch at the next validate position.
     * @param world the world where we are going to place torches
     * @param user who used the item, also used to reach for the item to damage
     * @param itemUsed the item
     * @param positions the iterator of positions where a torch can be placed
     * @return true it's considered completed and will have the job removed from the queue.
     */
    private static boolean tryPlaceNext(World world, PlayerEntity user, ItemStack itemUsed, Iterator<BlockPos> positions) {
        if(!hasDurability(itemUsed)) return true;

        while (positions.hasNext()) {
            BlockPos position = positions.next();

            // Check light level before placing torch
            if (world.getLightLevel(LightType.BLOCK, position) <= 7) {
                world.setBlockState(position, TORCH.getDefaultState(), Block.NOTIFY_ALL);

                spawnParticleLine(
                        (ServerWorld) world,
                        Vec3d.of(position).add(0.5 , 0, 0.5),
                        Vec3d.of(position).add(0.5 , 0, 0.5),
                        Arrays.asList(ParticleTypes.WITCH, ParticleTypes.PORTAL),
                        25
                );

                // if the orb has light charges then first consumes them
                if(itemUsed.get(TORCH_CHARGES) > 0) decrementCharges(itemUsed);
                else itemUsed.damage(1, user);

                return false; // placed one torch this tick, stop here
            }
            // else: skip this position and try next
        }
        return true; // no more positions left
    }

    /**
     * checks for spots where a torch can be placed, doesn't check light level
     */
    private static boolean canPlaceTorch(World world, BlockPos position) {
        BlockPos below = position.down();
        BlockState belowState = world.getBlockState(below);
        BlockState targetState = world.getBlockState(position);

        if ((!targetState.isAir() && !targetState.isOf(Blocks.SNOW)) || !targetState.getFluidState().isEmpty()) return false;

        return belowState.isSideSolidFullSquare(world, below, Direction.UP);
    }

    public static void spawnParticleLine(ServerWorld world, Vec3d start, Vec3d end, List<ParticleEffect> particles, int steps) {
        Vec3d mid = start.add(end).multiply(0.5);
        Vec3d control = mid.add(0, 2.0, 0);

        int particleCount = particles.size();

        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            double x = (1 - t) * (1 - t) * start.x + 2 * (1 - t) * t * control.x + t * t * end.x;
            double y = (1 - t) * (1 - t) * start.y + 2 * (1 - t) * t * control.y + t * t * end.y;
            double z = (1 - t) * (1 - t) * start.z + 2 * (1 - t) * t * control.z + t * t * end.z;

            Vec3d point = new Vec3d(x, y, z);

            // Cycle through particles list with modulo
            ParticleEffect particle = particles.get(i % particleCount);

            world.spawnParticles(particle, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
    }
}