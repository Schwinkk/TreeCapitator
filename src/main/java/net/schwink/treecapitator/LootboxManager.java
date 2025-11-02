package net.schwink.treecapitator;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class LootboxManager {

    public static LootTable getLootTable(){
        LootTable.Builder tableBuilder = LootTable.lootTable();

        LootPool.Builder tablePool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(Items.REDSTONE)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1,3)))
                        .setWeight(5))
                .add(LootItem.lootTableItem(Items.DIAMOND)
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                        .setWeight(5))
                .add(EmptyLootItem.emptyItem()
                        .setWeight(10));

        tableBuilder.withPool(tablePool);

        return tableBuilder.build();
    }


    public static void dropItemFromTable(ServerLevel level, BlockPos pos, ServerPlayer player){
        LootTable table = getLootTable();

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
