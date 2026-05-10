package com.example.examplemod.compat;

import com.example.examplemod.DryingRackRecipe;
import com.example.examplemod.TobaccoGrinderRecipe;
import com.example.examplemod.TobaccoWorkbenchRecipe;
import com.example.examplemod.SmokingWarningMod;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

@JeiPlugin
public class TobaccoJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = SmokingWarningMod.id("jei");

    public static final RecipeType<TobaccoWorkbenchRecipe> TOBACCO_WORKBENCH =
            RecipeType.create("smokingwarningmod", "tobacco_workbench", TobaccoWorkbenchRecipe.class);
    public static final RecipeType<DryingRackRecipe> DRYING_RACK =
            RecipeType.create("smokingwarningmod", "drying_rack", DryingRackRecipe.class);
    public static final RecipeType<TobaccoGrinderRecipe> TOBACCO_GRINDER =
            RecipeType.create("smokingwarningmod", "tobacco_grinder", TobaccoGrinderRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(
                new TobaccoWorkbenchCategory(),
                new DryingRackCategory(),
                new TobaccoGrinderCategory());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var rm = Minecraft.getInstance().level.getRecipeManager();
        registration.addRecipes(TOBACCO_WORKBENCH,
                rm.getAllRecipesFor(SmokingWarningMod.TOBACCO_WORKBENCH_RECIPE_TYPE.get()).stream()
                        .map(r -> r.value()).toList());
        registration.addRecipes(DRYING_RACK,
                rm.getAllRecipesFor(SmokingWarningMod.DRYING_RACK_RECIPE_TYPE.get()).stream()
                        .map(r -> r.value()).toList());
        registration.addRecipes(TOBACCO_GRINDER,
                rm.getAllRecipesFor(SmokingWarningMod.TOBACCO_GRINDER_RECIPE_TYPE.get()).stream()
                        .map(r -> r.value()).toList());
    }

    static class TobaccoWorkbenchCategory implements IRecipeCategory<TobaccoWorkbenchRecipe> {
        @Override
        public RecipeType<TobaccoWorkbenchRecipe> getRecipeType() {
            return TOBACCO_WORKBENCH;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("jei.category.smokingwarningmod.tobacco_workbench");
        }

        @Override
        public IDrawable getIcon() {
            return new ItemDrawable(SmokingWarningMod.TOBACCO_WORKBENCH_ITEM.toStack());
        }

        @Override
        public int getWidth() { return 116; }

        @Override
        public int getHeight() { return 54; }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, TobaccoWorkbenchRecipe recipe, IFocusGroup focuses) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 19).addIngredients(recipe.first());
            builder.addSlot(RecipeIngredientRole.INPUT, 37, 19).addIngredients(recipe.second());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19).addItemStack(recipe.getResultItem(null));
        }

        @Override
        public void draw(TobaccoWorkbenchRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
            Font font = Minecraft.getInstance().font;
            g.drawString(font, Component.translatable("jei.smokingwarningmod.tobacco_workbench.description"),
                    1, 42, 0xFF808080, false);
        }
    }

    static class DryingRackCategory implements IRecipeCategory<DryingRackRecipe> {
        @Override
        public RecipeType<DryingRackRecipe> getRecipeType() {
            return DRYING_RACK;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("jei.category.smokingwarningmod.drying_rack");
        }

        @Override
        public IDrawable getIcon() {
            return new ItemDrawable(SmokingWarningMod.DRYING_RACK_ITEM.toStack());
        }

        @Override
        public int getWidth() { return 116; }

        @Override
        public int getHeight() { return 54; }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, DryingRackRecipe recipe, IFocusGroup focuses) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 19).addIngredients(recipe.ingredient());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19).addItemStack(recipe.getResultItem(null));
        }

        @Override
        public void draw(DryingRackRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
            Font font = Minecraft.getInstance().font;
            int seconds = recipe.dryingTime() / 20;
            g.drawString(font, Component.translatable("jei.smokingwarningmod.drying_rack.description"),
                    1, 42, 0xFF808080, false);
            g.drawString(font, seconds + "s", 75, 5, 0xFF555555, false);
        }
    }

    static class TobaccoGrinderCategory implements IRecipeCategory<TobaccoGrinderRecipe> {
        @Override
        public RecipeType<TobaccoGrinderRecipe> getRecipeType() {
            return TOBACCO_GRINDER;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("jei.category.smokingwarningmod.tobacco_grinder");
        }

        @Override
        public IDrawable getIcon() {
            return new ItemDrawable(SmokingWarningMod.TOBACCO_GRINDER_ITEM.toStack());
        }

        @Override
        public int getWidth() { return 116; }

        @Override
        public int getHeight() { return 54; }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, TobaccoGrinderRecipe recipe, IFocusGroup focuses) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 19).addIngredients(recipe.ingredient());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19).addItemStack(recipe.getResultItem(null));
        }

        @Override
        public void draw(TobaccoGrinderRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
            Font font = Minecraft.getInstance().font;
            g.drawString(font, Component.translatable("jei.smokingwarningmod.tobacco_grinder.description"),
                    1, 42, 0xFF808080, false);
        }
    }

    private record ItemDrawable(ItemStack stack) implements IDrawable {
        @Override public int getWidth() { return 16; }
        @Override public int getHeight() { return 16; }
        @Override
        public void draw(GuiGraphics g, int x, int y) {
            g.renderFakeItem(stack, x, y);
        }
    }
}
