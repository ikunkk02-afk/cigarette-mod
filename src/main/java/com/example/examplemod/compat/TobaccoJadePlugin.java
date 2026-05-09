package com.example.examplemod.compat;

import com.example.examplemod.DryingRackBlock;
import com.example.examplemod.DryingRackBlockEntity;
import com.example.examplemod.TobaccoGrinderBlock;
import com.example.examplemod.TobaccoWorkbenchBlock;
import com.example.examplemod.cigaretteMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class TobaccoJadePlugin implements IWailaPlugin {
    private static final ResourceLocation DRYING_RACK = cigaretteMod.id("drying_rack_state");
    private static final ResourceLocation TOBACCO_GRINDER = cigaretteMod.id("tobacco_grinder_info");
    private static final ResourceLocation TOBACCO_WORKBENCH = cigaretteMod.id("tobacco_workbench_info");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(DryingRackProvider.INSTANCE, DryingRackBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(DryingRackProvider.INSTANCE, DryingRackBlock.class);
        registration.registerBlockComponent(TobaccoGrinderProvider.INSTANCE, TobaccoGrinderBlock.class);
        registration.registerBlockComponent(TobaccoWorkbenchProvider.INSTANCE, TobaccoWorkbenchBlock.class);
    }

    private enum DryingRackProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public ResourceLocation getUid() {
            return DRYING_RACK;
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof DryingRackBlockEntity dryingRack) {
                data.putInt("LeafCount", dryingRack.leafCount());
                data.putInt("Progress", dryingRack.dryingProgressPercent());
                data.putInt("RemainingSeconds", dryingRack.remainingDryingSeconds());
                data.putBoolean("Dried", dryingRack.isDried());
            }
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            int leafCount = data.getInt("LeafCount");
            if (leafCount <= 0) {
                tooltip.add(Component.translatable("jade.cigarettemod.status", Component.translatable("jade.cigarettemod.status.empty")));
                return;
            }

            tooltip.add(Component.translatable("jade.cigarettemod.leaf_count", leafCount));
            if (data.getBoolean("Dried")) {
                tooltip.add(Component.translatable("jade.cigarettemod.status", Component.translatable("jade.cigarettemod.status.dried")));
                return;
            }

            tooltip.add(Component.translatable("jade.cigarettemod.progress", data.getInt("Progress")));
            tooltip.add(Component.translatable("jade.cigarettemod.remaining_seconds", data.getInt("RemainingSeconds")));
            tooltip.add(Component.translatable("jade.cigarettemod.status", Component.translatable("jade.cigarettemod.status.drying")));
        }
    }

    private enum TobaccoGrinderProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public ResourceLocation getUid() {
            return TOBACCO_GRINDER;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            tooltip.add(Component.translatable("block.cigarettemod.tobacco_grinder"));
            tooltip.add(Component.translatable("jade.cigarettemod.usage.grinder"));
        }
    }

    private enum TobaccoWorkbenchProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public ResourceLocation getUid() {
            return TOBACCO_WORKBENCH;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            tooltip.add(Component.translatable("block.cigarettemod.tobacco_workbench"));
            tooltip.add(Component.translatable("jade.cigarettemod.usage.workbench"));
        }
    }
}
