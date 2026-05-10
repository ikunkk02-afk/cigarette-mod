package com.example.examplemod.compat;

import com.example.examplemod.DryingRackRecipe;
import com.example.examplemod.TobaccoGrinderRecipe;
import com.example.examplemod.TobaccoWorkbenchRecipe;
import com.example.examplemod.SmokingWarningMod;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@REIPluginClient
public class TobaccoReiPlugin implements REIClientPlugin {
    public static final CategoryIdentifier<BasicDisplay> TOBACCO_WORKBENCH =
            CategoryIdentifier.of("smokingwarningmod", "tobacco_workbench");
    public static final CategoryIdentifier<BasicDisplay> DRYING_RACK =
            CategoryIdentifier.of("smokingwarningmod", "drying_rack");
    public static final CategoryIdentifier<BasicDisplay> TOBACCO_GRINDER =
            CategoryIdentifier.of("smokingwarningmod", "tobacco_grinder");

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new TobaccoWorkbenchCategory());
        registry.add(new DryingRackCategory());
        registry.add(new TobaccoGrinderCategory());
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        var rm = net.minecraft.client.Minecraft.getInstance().level.getRecipeManager();
        for (RecipeHolder<?> holder : rm.getAllRecipesFor(SmokingWarningMod.TOBACCO_WORKBENCH_RECIPE_TYPE.get())) {
            var recipe = (TobaccoWorkbenchRecipe) holder.value();
            registry.add(new BasicDisplay(
                    List.of(toIngredient(recipe.first().getItems()), toIngredient(recipe.second().getItems())),
                    List.of(toIngredient(recipe.getResultItem(null)))) {
                @Override public CategoryIdentifier<?> getCategoryIdentifier() { return TOBACCO_WORKBENCH; }
            });
        }
        for (RecipeHolder<?> holder : rm.getAllRecipesFor(SmokingWarningMod.DRYING_RACK_RECIPE_TYPE.get())) {
            var recipe = (DryingRackRecipe) holder.value();
            registry.add(new BasicDisplay(
                    List.of(toIngredient(recipe.ingredient().getItems())),
                    List.of(toIngredient(recipe.getResultItem(null)))) {
                @Override public CategoryIdentifier<?> getCategoryIdentifier() { return DRYING_RACK; }
            });
        }
        for (RecipeHolder<?> holder : rm.getAllRecipesFor(SmokingWarningMod.TOBACCO_GRINDER_RECIPE_TYPE.get())) {
            var recipe = (TobaccoGrinderRecipe) holder.value();
            registry.add(new BasicDisplay(
                    List.of(toIngredient(recipe.ingredient().getItems())),
                    List.of(toIngredient(recipe.getResultItem(null)))) {
                @Override public CategoryIdentifier<?> getCategoryIdentifier() { return TOBACCO_GRINDER; }
            });
        }
    }

    private static EntryIngredient toIngredient(ItemStack... stacks) {
        return EntryIngredient.of(Arrays.stream(stacks).map(EntryStacks::of).toList());
    }

    private static EntryIngredient toIngredient(ItemStack stack) {
        return EntryIngredient.of(EntryStacks.of(stack));
    }

    static class TobaccoWorkbenchCategory implements DisplayCategory<BasicDisplay> {
        @Override public CategoryIdentifier<BasicDisplay> getCategoryIdentifier() { return TOBACCO_WORKBENCH; }
        @Override public Component getTitle() { return Component.translatable("rei.category.SmokingWarningMod.tobacco_workbench"); }
        @Override public Renderer getIcon() { return EntryStacks.of(SmokingWarningMod.TOBACCO_WORKBENCH_ITEM); }
        @Override public int getDisplayHeight() { return 36; }
        @Override public List<Widget> setupDisplay(BasicDisplay display, Rectangle bounds) {
            var widgets = new ArrayList<Widget>();
            widgets.add(Widgets.createRecipeBase(bounds));
            var inputs = display.getInputEntries();
            if (inputs.size() >= 1) widgets.add(Widgets.createSlot(new Point(bounds.x + 1, bounds.y + 1)).entries(inputs.get(0)).markInput());
            if (inputs.size() >= 2) widgets.add(Widgets.createSlot(new Point(bounds.x + 19, bounds.y + 1)).entries(inputs.get(1)).markInput());
            var outputs = display.getOutputEntries();
            if (!outputs.isEmpty()) widgets.add(Widgets.createSlot(new Point(bounds.x + 95, bounds.y + 1)).entries(outputs.get(0)).markOutput());
            return widgets;
        }
    }

    static class DryingRackCategory implements DisplayCategory<BasicDisplay> {
        @Override public CategoryIdentifier<BasicDisplay> getCategoryIdentifier() { return DRYING_RACK; }
        @Override public Component getTitle() { return Component.translatable("rei.category.SmokingWarningMod.drying_rack"); }
        @Override public Renderer getIcon() { return EntryStacks.of(SmokingWarningMod.DRYING_RACK_ITEM); }
        @Override public int getDisplayHeight() { return 36; }
        @Override public List<Widget> setupDisplay(BasicDisplay display, Rectangle bounds) {
            var widgets = new ArrayList<Widget>();
            widgets.add(Widgets.createRecipeBase(bounds));
            var inputs = display.getInputEntries();
            if (!inputs.isEmpty()) widgets.add(Widgets.createSlot(new Point(bounds.x + 1, bounds.y + 1)).entries(inputs.get(0)).markInput());
            var outputs = display.getOutputEntries();
            if (!outputs.isEmpty()) widgets.add(Widgets.createSlot(new Point(bounds.x + 95, bounds.y + 1)).entries(outputs.get(0)).markOutput());
            return widgets;
        }
    }

    static class TobaccoGrinderCategory implements DisplayCategory<BasicDisplay> {
        @Override public CategoryIdentifier<BasicDisplay> getCategoryIdentifier() { return TOBACCO_GRINDER; }
        @Override public Component getTitle() { return Component.translatable("rei.category.SmokingWarningMod.tobacco_grinder"); }
        @Override public Renderer getIcon() { return EntryStacks.of(SmokingWarningMod.TOBACCO_GRINDER_ITEM); }
        @Override public int getDisplayHeight() { return 36; }
        @Override public List<Widget> setupDisplay(BasicDisplay display, Rectangle bounds) {
            var widgets = new ArrayList<Widget>();
            widgets.add(Widgets.createRecipeBase(bounds));
            var inputs = display.getInputEntries();
            if (!inputs.isEmpty()) widgets.add(Widgets.createSlot(new Point(bounds.x + 1, bounds.y + 1)).entries(inputs.get(0)).markInput());
            var outputs = display.getOutputEntries();
            if (!outputs.isEmpty()) widgets.add(Widgets.createSlot(new Point(bounds.x + 95, bounds.y + 1)).entries(outputs.get(0)).markOutput());
            return widgets;
        }
    }
}
