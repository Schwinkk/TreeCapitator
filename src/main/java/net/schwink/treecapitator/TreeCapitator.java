package net.schwink.treecapitator;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
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

    @Mod.EventBusSubscriber
    public static class TreeCapitatorStarter {

        @SubscribeEvent
        public static void OnBreakSpeed(PlayerEvent.BreakSpeed event) {

            var state = event.getState();

            if (!isBlockLog(state)) {
                return;
            }

            Level level = event.getEntity().level();
            Optional<BlockPos> optionalPos = event.getPosition();
            BlockPos pos;
            Player player = event.getEntity();


            if (optionalPos.isPresent()){
                pos = optionalPos.get();
            }
            else{
                return;
            }

            int x = TreeManager.getTreeSize(pos, level);

            if (player.getMainHandItem().getItem() instanceof AxeItem & x > 1){
                float breakSpeedModifier = (float) Math.sqrt((double) 1 / (x * 2));
                event.setNewSpeed(event.getOriginalSpeed() * breakSpeedModifier);
            }

        }

        @SubscribeEvent
        public static void OnBlockBreak(BlockEvent.BreakEvent event) {

            var state = event.getState();
            if (!isBlockLog(state)) {
                return;
            }

            ServerPlayer player = (ServerPlayer) event.getPlayer();
            Level level = (Level) event.getLevel(); // надеюсь приведение ничего не сломает))))
            BlockPos pos = event.getPos();

            if (player.getMainHandItem().getItem() instanceof AxeItem){
                TreeManager.destroyAndDrop(level, pos, player);

            }
        }

        private static boolean isBlockLog(BlockState state) {
            return state.is(LOGS_TAG);
        }
    }
}