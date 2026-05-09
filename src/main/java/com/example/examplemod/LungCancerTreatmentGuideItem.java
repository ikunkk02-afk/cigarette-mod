package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class LungCancerTreatmentGuideItem extends Item {
    private static final int TOTAL_PAGES = 8;

    public LungCancerTreatmentGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            List<Component> pages = new ArrayList<>();
            for (int i = 0; i < TOTAL_PAGES; i++) {
                String key = "guide.cigarettemod.treatment.page." + (i + 1);
                pages.add(Component.translatable(key));
            }
            BookViewScreen.BookAccess bookAccess = new BookViewScreen.BookAccess(pages);
            Minecraft.getInstance().setScreen(new BookViewScreen(bookAccess));
        }
        return InteractionResultHolder.consume(stack);
    }
}
