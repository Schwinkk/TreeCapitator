package net.schwink.treecapitator;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class LootboxManager {
    public static void dropItemFromTable(ServerLevel level, BlockPos pos, ServerPlayer player){
        ResourceLocation lootTableLocation = ResourceLocation.parse("treecapitator:gameplay/log_drop");
        ResourceKey<LootTable> tableKey = ResourceKey.create(Registries.LOOT_TABLE, lootTableLocation);
        LootTable table = level.getServer().reloadableRegistries().getLootTable(tableKey);

        if (table == LootTable.EMPTY) {
            System.out.println("[Treecapitator] Loot table not found: " + lootTableLocation);
        }

        ItemStack itemStack = player.getMainHandItem();
        BlockState state = level.getBlockState(pos);

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, itemStack)
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .create(LootContextParamSets.BLOCK);
        ObjectArrayList<ItemStack> items = table.getRandomItems(params);

        for (ItemStack stack : items)
        {
            if(!stack.isEmpty()){
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }
}
