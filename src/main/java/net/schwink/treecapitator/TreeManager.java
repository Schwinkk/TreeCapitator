package net.schwink.treecapitator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class TreeManager {

    public static final TagKey<Block> LOGS_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("minecraft", "logs"));
    public static final TagKey<Block> LEAVES_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("minecraft", "leaves"));

    public static final List<BlockPos> upDirections = new ArrayList<>();

    static {
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 & z == 0) continue;
                    upDirections.add(new BlockPos(x, y, z));
                }
            }
        }
    }

    public static final List<BlockPos> allDirections = new ArrayList<>();

    static {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    allDirections.add(new BlockPos(x, y, z));
                }
            }
        }
    }

    public static int getTreeSize(BlockPos pos, Level level) {
        return getTreeList(pos, level, false).size();
    }

    public static void destroyAndDrop(Level level, BlockPos pos, ServerPlayer player) {

        Set<BlockPos> blocksToDestroy = getTreeList(pos, level, true);
        ItemStack tool = player.getMainHandItem();

        for (BlockPos destroyPos : blocksToDestroy) {

            if (level.getBlockState(destroyPos).is(LOGS_TAG)){
                tool.hurtAndBreak(1, player,InteractionHand.MAIN_HAND);
                LootboxManager.dropItemFromTable((ServerLevel) level, destroyPos, player);
            }

            Block.dropResources(level.getBlockState(destroyPos), level, destroyPos, level.getBlockEntity(destroyPos), player, tool);
            level.destroyBlock(destroyPos, false);
        }
    }

    public static Set<BlockPos> getTreeList(BlockPos pos, Level level, boolean checkLeaves){
        Queue<BlockPos> blocksQueue = new LinkedList<>();
        Set<BlockPos> iteratedBlocks = new HashSet<>();

        blocksQueue.add(pos);
        iteratedBlocks.add(pos);

        while (!blocksQueue.isEmpty()) {
            BlockPos blockPos = blocksQueue.poll();
            for (BlockPos dir : upDirections) {
                if (level.getBlockState(blockPos.offset(dir)).is(LOGS_TAG)){
                    if (iteratedBlocks.contains(blockPos.offset(dir))) {
                        continue;
                    }

                    iteratedBlocks.add(blockPos.offset(dir));
                    blocksQueue.add(blockPos.offset(dir));
                }
            }
        }

        if (!checkLeaves){
            return iteratedBlocks;
        }

        Set<BlockPos> restricdedLeavesBlocks = new HashSet<>();
        Set<BlockPos> leavesBlocks = new HashSet<>();
        blocksQueue.addAll(iteratedBlocks);

        while (!blocksQueue.isEmpty()) {
            BlockPos blockPos = blocksQueue.poll();
            for (BlockPos dir : allDirections) {
                if (level.getBlockState(blockPos.offset(dir)).is(LOGS_TAG) && !iteratedBlocks.contains(blockPos.offset(dir))){

                    // ищем расстояние от дерева до другого дерева
                    double lowestDist = Double.MAX_VALUE;
                    for (BlockPos log : iteratedBlocks){
                        double dist = blockPos.offset(dir).distSqr(log);
                        if (dist < lowestDist){
                            lowestDist = dist;
                        }
                    }
                    int radius = (int)Math.ceil(Math.sqrt(lowestDist) / 2.0);

                    // собираем список запрещенных блоков
                    for (int x = -radius; x < radius; x++){
                        for (int y = -radius; y < radius; y++) {
                            for (int z = -radius; z < radius; z++){
                                BlockPos offset = new BlockPos(x, y, z);
                                restricdedLeavesBlocks.add(blockPos.offset(dir).offset(offset));
                            }
                        }
                    }

                    // убираем блоки листвы, которые были записаны, но попали в черный список
                    for (BlockPos restrictedPos : restricdedLeavesBlocks){
                        leavesBlocks.remove(restrictedPos);
                    }
                }

                if (level.getBlockState(blockPos.offset(dir)).is(LEAVES_TAG)){
                    if (leavesBlocks.contains(blockPos.offset(dir)) || restricdedLeavesBlocks.contains(blockPos.offset(dir))) {
                        continue;
                    }

                    leavesBlocks.add(blockPos.offset(dir));
                    blocksQueue.add(blockPos.offset(dir));
                }
            }
        }

        iteratedBlocks.addAll(leavesBlocks);
        return iteratedBlocks;
    }
}
