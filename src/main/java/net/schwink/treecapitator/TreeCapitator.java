package net.schwink.treecapitator;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import java.lang.String;
import java.util.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TreeCapitator.MODID)
public final class TreeCapitator {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "treecapitator";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final TagKey<Block> LOGS_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("minecraft", "logs"));

    public static class DataManager {
        private static final Map<UUID, Set<BlockPos>> logsPositions = new HashMap<>();

        public static void InitializeLogSet (ServerPlayer player, BlockPos blockPos){
            Set<BlockPos> currentPos = new HashSet<>();
            currentPos.add(blockPos);
            logsPositions.put(player.getUUID(), currentPos);
        }

        public static void AddToLogSet (ServerPlayer player, BlockPos blockPos){
            Set<BlockPos> currentPos = logsPositions.get(player.getUUID());
            currentPos.add(blockPos);
            logsPositions.put(player.getUUID(), currentPos);
        }

        public static Set<BlockPos> GetLogSet(ServerPlayer player){
            return logsPositions.get(player.getUUID());
        }

        public static void ClearLogSet (ServerPlayer player){
            logsPositions.remove(player.getUUID());
        }
    }

    @Mod.EventBusSubscriber
    public static class TreeCapitatorStarter {

/*        @SubscribeEvent
        public static void OnBreakSpeed(PlayerEvent.BreakSpeed event){
            BlockState state = event.getState();

            if (!isBlockLog(state)){
                return;
            }

            ServerPlayer player = (ServerPlayer) event.getEntity();
            BlockPos pos = event.getEntity().getOnPos();
            Level level = event.getEntity().level();

            double destroySpeedModifier = 1 + Math.pow(Math.log(Math.pow(DataManager.GetLogSet(player).size(), 2)), 2);

            event.setNewSpeed((float) destroySpeedModifier);
        }*/

        @SubscribeEvent
        public static void OnBlockBreak(BlockEvent.BreakEvent event) {

            var state = event.getState();
            if (!isBlockLog(state)){
                return;
            }

            ServerPlayer player = (ServerPlayer) event.getPlayer();
            Level level = (Level) event.getLevel(); // надеюсь приведение ничего не сломает))))
            BlockPos pos = event.getPos();

            DataManager.InitializeLogSet(player, pos);

            getLogsHashSet(pos, level, player);
            destroyAndDrop(level, player);

            DataManager.ClearLogSet(player);

        }

        private static boolean isBlockLog(BlockState state) {
            return state.is(LOGS_TAG);
        }

        private static void getLogsHashSet(BlockPos pos, Level level, ServerPlayer player) {

            List<BlockPos> blocksToIterate = new ArrayList<>();
            blocksToIterate.add(pos);

            while (!blocksToIterate.isEmpty()) {
                List<BlockPos> currentBlocks = List.copyOf(blocksToIterate);

                blocksToIterate.clear();

                for (BlockPos blockPos : currentBlocks) {
                    blocksToIterate.addAll(checkNeighborsAndWrite(blockPos, level, player));
                }
            }
        }

        private static List<BlockPos> checkNeighborsAndWrite(BlockPos pos, Level level, ServerPlayer player) {
            Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP}; // добавить по диагоналям парсинг

            List<BlockPos> result = new ArrayList<>();

            for (Direction dir : directions) {
                if (level.getBlockState(pos.relative(dir)).is(LOGS_TAG)) {
                    if (DataManager.GetLogSet(player).contains(pos.relative(dir))){
                        continue;
                    }

                    DataManager.AddToLogSet(player, pos.relative((dir)));
                    System.out.println("NASHEL");

                    result.add(pos.relative(dir));
                }
            }

            return result;
        }

        private static void destroyAndDrop(Level level, ServerPlayer player){

            ItemStack tool = player.getMainHandItem();
            for (BlockPos pos : DataManager.GetLogSet(player)){
                Block.dropResources(level.getBlockState(pos), level, pos, level.getBlockEntity(pos), player, tool);
                level.destroyBlock(pos,false);
            }
        }
    }
}