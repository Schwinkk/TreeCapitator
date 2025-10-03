package net.schwink.treecapitator;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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

        public static void InitializeLogSet (UUID uuid, BlockPos blockPos){
            Set<BlockPos> currentPos = new HashSet<>();
            currentPos.add(blockPos);
            logsPositions.put(uuid, currentPos);
        }

        public static void AddToLogSet (UUID uuid, BlockPos blockPos){
            Set<BlockPos> currentPos = logsPositions.get(uuid);
            currentPos.add(blockPos);
            logsPositions.put(uuid, currentPos);
        }

        public static Set<BlockPos> GetLogSet(UUID uuid){
            return logsPositions.get(uuid);
        }

        public static void ClearLogSet (UUID uuid){
            logsPositions.remove(uuid);
        }
    }

    @Mod.EventBusSubscriber
    public static class TreeCapitatorStarter {

        @SubscribeEvent
        public static void GetBlockCount(PlayerInteractEvent.LeftClickBlock event){

            BlockPos pos = event.getPos();
            Level level = event.getLevel();
            Player player = event.getEntity();

            BlockState state = level.getBlockState(pos);

            if (!isBlockLog(state)){
                return;
            }

            DataManager.ClearLogSet(player.getUUID());

            DataManager.InitializeLogSet(player.getUUID(), pos);
            getLogsHashSet(pos, level, player.getUUID());
        }

        @SubscribeEvent
        public static void OnBreakSpeed(PlayerEvent.BreakSpeed event){

            Player player = event.getEntity();

            float newDestroySpeed = event.getOriginalSpeed() / DataManager.GetLogSet(player.getUUID()).size();
            event.setNewSpeed(newDestroySpeed);
        }

        @SubscribeEvent
        public static void OnBlockBreak(BlockEvent.BreakEvent event) {

            var state = event.getState();
            if (!isBlockLog(state)){
                return;
            }

            ServerPlayer player = (ServerPlayer) event.getPlayer();
            Level level = (Level) event.getLevel(); // надеюсь приведение ничего не сломает))))
            BlockPos pos = event.getPos();

            destroyAndDrop(level, player);

            //DataManager.ClearLogSet(player.getUUID());

        }

        private static boolean isBlockLog(BlockState state) {
            return state.is(LOGS_TAG);
        }

        private static void getLogsHashSet(BlockPos pos, Level level, UUID uuid) {

            List<BlockPos> blocksToIterate = new ArrayList<>();
            blocksToIterate.add(pos);

            while (!blocksToIterate.isEmpty()) {
                List<BlockPos> currentBlocks = List.copyOf(blocksToIterate);

                blocksToIterate.clear();

                for (BlockPos blockPos : currentBlocks) {
                    blocksToIterate.addAll(checkNeighborsAndWrite(blockPos, level, uuid));
                }
            }
        }

        private static List<BlockPos> checkNeighborsAndWrite(BlockPos pos, Level level, UUID uuid) {
            Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP}; // добавить по диагоналям парсинг

            List<BlockPos> result = new ArrayList<>();

            for (Direction dir : directions) {
                if (level.getBlockState(pos.relative(dir)).is(LOGS_TAG)) {
                    if (DataManager.GetLogSet(uuid).contains(pos.relative(dir))){
                        continue;
                    }

                    DataManager.AddToLogSet(uuid, pos.relative((dir)));
                    System.out.println("NASHEL");

                    result.add(pos.relative(dir));
                }
            }

            return result;
        }

        private static void destroyAndDrop(Level level, ServerPlayer player){

            ItemStack tool = player.getMainHandItem();

            System.out.println("IDI NAHUI");

            for (BlockPos pos : DataManager.GetLogSet(player.getUUID())){
                Block.dropResources(level.getBlockState(pos), level, pos, level.getBlockEntity(pos), player, tool);
                level.destroyBlock(pos,false);
            }
        }
    }
}