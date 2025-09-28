package net.schwink.treecapitator;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import java.io.Console;
import java.lang.String;
import java.util.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TreeCapitator.MODID)
public final class TreeCapitator {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "treecapitator";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public TreeCapitator(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();

        // Register the commonSetup method for modloading
        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::commonSetup);

        // Register the item to a creative tab
        BuildCreativeModeTabContentsEvent.getBus(modBusGroup).addListener(TreeCapitator::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // Add the example block item to the building blocks tab
    private static void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }

    public static final TagKey<Block> LOGS_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("minecraft", "logs"));

    @Mod.EventBusSubscriber
    public static class TreeCapitatorStarter {

        public static Set<BlockPos> logsPositions = new HashSet<>();

        @SubscribeEvent
        public static void OnBlockBreak(BlockEvent.BreakEvent event) {

            var player = event.getPlayer();
            Level level = (Level) event.getLevel(); // надеюсь приведение ничего не сломает))))
            BlockPos pos = event.getPos();
            var state = event.getState();

            if (isBlockLog(state)) {
                player.displayClientMessage(Component.literal(state.getBlock().getName().getString()), false);

                getLogsHashMap(pos, level);

                destroyAndDrop(level, player);

                logsPositions.clear();
            }
        }

        private static boolean isBlockLog(BlockState state) {
            return state.is(LOGS_TAG);
        }

        private static void getLogsHashMap(BlockPos pos, Level level) {

            boolean isTreeEnded = false;
            logsPositions.add(pos);

            while (!isTreeEnded) {
                int setLength = logsPositions.size();

                for (BlockPos blockPos : new ArrayList<>(logsPositions.stream().toList())) {
                    checkNeighborsAndWrite(blockPos, level);
                }

                if (logsPositions.size() == setLength) {
                    isTreeEnded = true;
                }
            }

            logsPositions.remove(pos);
        }

        private static void checkNeighborsAndWrite(BlockPos pos, Level level) {
            Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP};

            for (Direction dir : directions) {
                if (level.getBlockState(pos.relative(dir)).is(LOGS_TAG)) {
                    logsPositions.add(pos.relative(dir));
                    System.out.println("NASHEL");
                }
            }
        }

        private static void destroyAndDrop(Level level, Player player){
            if (level.isClientSide) return;

            ItemStack tool = player.getMainHandItem();
            for (BlockPos pos : logsPositions){
                Block.dropResources(level.getBlockState(pos), level, pos, level.getBlockEntity(pos), player, tool);
                level.destroyBlock(pos,false);
            }
        }
    }
}