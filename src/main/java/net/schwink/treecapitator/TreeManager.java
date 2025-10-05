package net.schwink.treecapitator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class TreeManager {

    public static final TagKey<Block> LOGS_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("minecraft", "logs"));
    public static final TagKey<Block> LEAVES_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("minecraft", "leaves"));

    private static boolean isBlockLog(BlockState state) {
        return state.is(LOGS_TAG);
    }

    public static int getTreeSize(BlockPos pos, Level level) {

        Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP};
        Queue<BlockPos> blocksQueue = new LinkedList<>();
        List<BlockPos> iteratedBlocks = new ArrayList<>();

        blocksQueue.add(pos);
        iteratedBlocks.add(pos);

        while (!blocksQueue.isEmpty()) {
            for (BlockPos blockPos : blocksQueue) {
                blockPos = blocksQueue.poll();

                for (Direction dir : directions) {
                    if (level.getBlockState(blockPos.relative(dir)).is(LOGS_TAG)){
                        if (iteratedBlocks.contains(blockPos.relative(dir))) {
                            continue;
                        }

                        iteratedBlocks.add(blockPos.relative(dir));
                        blocksQueue.add(blockPos.relative(dir));
                    }

                }
            }
        }

        return iteratedBlocks.size();
    }

    public static void destroyAndDrop(Level level, BlockPos pos, Player player) {

        Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP};
        Queue<BlockPos> blocksQueue = new LinkedList<>();
        List<BlockPos> iteratedBlocks = new ArrayList<>();

        blocksQueue.add(pos);
        iteratedBlocks.add(pos);

        while (!blocksQueue.isEmpty()) {
            for (BlockPos blockPos : blocksQueue) {
                blockPos = blocksQueue.poll();

                for (Direction dir : directions) {
                    if (level.getBlockState(blockPos.relative(dir)).is(LOGS_TAG)){
                        if (iteratedBlocks.contains(blockPos.relative(dir))) {
                            continue;
                        }

                        iteratedBlocks.add(blockPos.relative(dir));
                        blocksQueue.add(blockPos.relative(dir));
                    }

                }
            }
        }

        ItemStack tool = player.getMainHandItem();

        for (BlockPos destroyPos : iteratedBlocks) {
            System.out.println("BABA");
            Block.dropResources(level.getBlockState(destroyPos), level, destroyPos, level.getBlockEntity(destroyPos), player, tool);
            level.destroyBlock(destroyPos, false);
        }
    }
}
