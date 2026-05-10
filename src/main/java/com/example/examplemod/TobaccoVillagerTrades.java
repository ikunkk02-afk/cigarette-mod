package com.example.examplemod;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

public class TobaccoVillagerTrades {
    @SubscribeEvent
    public void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != SmokingWarningMod.TOBACCO_VILLAGER.get()) {
            return;
        }

        event.getTrades().get(1).add(new BasicItemListing(3, new ItemStack(SmokingWarningMod.TOBACCO_SEEDS.get(), 4), 12, 2, 0.08F));
        event.getTrades().get(1).add(new BasicItemListing(new ItemStack(SmokingWarningMod.TOBACCO_LEAF.get(), 18), new ItemStack(Items.EMERALD), 12, 2, 0.05F));

        event.getTrades().get(2).add(new BasicItemListing(new ItemStack(SmokingWarningMod.DRIED_TOBACCO_LEAF.get(), 14), new ItemStack(Items.EMERALD), 10, 8, 0.05F));
        event.getTrades().get(2).add(new BasicItemListing(9, new ItemStack(SmokingWarningMod.TOBACCO_WORKBENCH_ITEM.get()), 6, 8, 0.08F));

        event.getTrades().get(3).add(new BasicItemListing(new ItemStack(SmokingWarningMod.TOBACCO_SHREDS.get(), 16), new ItemStack(Items.EMERALD), 10, 12, 0.05F));
        event.getTrades().get(3).add(new BasicItemListing(12, new ItemStack(SmokingWarningMod.LIGHTER.get()), 5, 12, 0.08F));

        event.getTrades().get(4).add(new BasicItemListing(10, new ItemStack(SmokingWarningMod.CIGARETTE.get()), 6, 16, 0.12F));

        event.getTrades().get(5).add(new BasicItemListing(16, new ItemStack(Items.FLOWER_POT), 4, 24, 0.12F));
        event.getTrades().get(5).add(new BasicItemListing(new ItemStack(Items.EMERALD, 8), new ItemStack(SmokingWarningMod.TOBACCO_LEAF.get(), 8), new ItemStack(Items.BONE_MEAL, 2), 4, 24, 0.12F));
        event.getTrades().get(5).add(new BasicItemListing(24, new ItemStack(SmokingWarningMod.LUNG_CANCER_TREATMENT_GUIDE.get()), 3, 30, 0.2F));
    }
}
