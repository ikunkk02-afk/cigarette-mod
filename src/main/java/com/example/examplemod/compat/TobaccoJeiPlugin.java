package com.example.examplemod.compat;

import com.example.examplemod.DryingRackRecipe;
import com.example.examplemod.TobaccoGrinderRecipe;
import com.example.examplemod.TobaccoWorkbenchRecipe;
import com.example.examplemod.cigaretteMod;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

@JeiPlugin
public class TobaccoJeiPlugin implements IModPlugin {
    public static final mezz.jei.api.recipe.RecipeType<RecipeHolder<DryingRackRecipe>> DRYING_RACK =
            mezz.jei.api.recipe.RecipeType.createRecipeHolderType(cigaretteMod.id("drying_rack_recipe"));
    public static final mezz.jei.api.recipe.RecipeType<RecipeHolder<TobaccoGrinderRecipe>> TOBACCO_GRINDER =
            mezz.jei.api.recipe.RecipeType.createRecipeHolderType(cigaretteMod.id("tobacco_grinder_recipe"));
    public static final mezz.jei.api.recipe.RecipeType<RecipeHolder<TobaccoWorkbenchRecipe>> TOBACCO_WORKBENCH =
            mezz.jei.api.recipe.RecipeType.createRecipeHolderType(cigaretteMod.id("tobacco_workbench_recipe"));

    @Override
    public ResourceLocation getPluginUid() {
        return cigaretteMod.id("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new DryingRackCategory(gui),
                new TobaccoGrinderCategory(gui),
                new TobaccoWorkbenchCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        registration.addRecipes(DRYING_RACK, level.getRecipeManager().getAllRecipesFor(cigaretteMod.DRYING_RACK_RECIPE_TYPE.get()));
        registration.addRecipes(TOBACCO_GRINDER, level.getRecipeManager().getAllRecipesFor(cigaretteMod.TOBACCO_GRINDER_RECIPE_TYPE.get()));
        registration.addRecipes(TOBACCO_WORKBENCH, level.getRecipeManager().getAllRecipesFor(cigaretteMod.TOBACCO_WORKBENCH_RECIPE_TYPE.get()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(cigaretteMod.DRYING_RACK_ITEM.get(), DRYING_RACK);
        registration.addRecipeCatalyst(cigaretteMod.TOBACCO_GRINDER_ITEM.get(), TOBACCO_GRINDER);
        registration.addRecipeCatalyst(cigaretteMod.TOBACCO_WORKBENCH_ITEM.get(), TOBACCO_WORKBENCH);
    }

    private abstract static class BaseCategory<T extends Recipe<?>> implements mezz.jei.api.recipe.category.IRecipeCategory<RecipeHolder<T>> {
        private final mezz.jei.api.recipe.RecipeType<RecipeHolder<T>> recipeType;
        private final Component title;
        protected final Component description;
        private final IDrawable background;
        private final IDrawable icon;
        protected final IDrawable arrow;
        protected final IDrawable plus;

        BaseCategory(
                IGuiHelper gui,
                mezz.jei.api.recipe.RecipeType<RecipeHolder<T>> recipeType,
                String titleKey,
                String descriptionKey,
                ItemStack iconStack) {
            this.recipeType = recipeType;
            this.title = Component.translatable(titleKey);
            this.description = Component.translatable(descriptionKey);
            this.background = gui.createBlankDrawable(150, 70);
            this.icon = gui.createDrawableItemStack(iconStack);
            this.arrow = gui.getRecipeArrow();
            this.plus = gui.getRecipePlusSign();
        }

        @Override
        public mezz.jei.api.recipe.RecipeType<RecipeHolder<T>> getRecipeType() {
            return this.recipeType;
        }

        @Override
        public Component getTitle() {
            return this.title;
        }

        @Override
        public IDrawable getBackground() {
            return this.background;
        }

        @Override
        public IDrawable getIcon() {
            return this.icon;
        }

        @Override
        public void draw(RecipeHolder<T> recipe, mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
            this.arrow.draw(graphics, 62, 18);
            this.drawDescription(graphics);
        }

        protected void drawDescription(GuiGraphics graphics) {
            graphics.drawWordWrap(Minecraft.getInstance().font, this.description, 4, 44, 142, 0xFF404040);
        }
    }

    private static class DryingRackCategory extends BaseCategory<DryingRackRecipe> {
        DryingRackCategory(IGuiHelper gui) {
            super(gui, DRYING_RACK, "jei.category.cigarettemod.drying_rack", "jei.cigarettemod.drying_rack.description",
                    new ItemStack(cigaretteMod.DRYING_RACK_ITEM.get()));
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<DryingRackRecipe> holder, IFocusGroup focuses) {
            DryingRackRecipe recipe = holder.value();
            builder.addSlot(RecipeIngredientRole.INPUT, 26, 18).addIngredients(recipe.ingredient());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 18).addItemStack(recipe.result());
        }
    }

    private static class TobaccoGrinderCategory extends BaseCategory<TobaccoGrinderRecipe> {
        TobaccoGrinderCategory(IGuiHelper gui) {
            super(gui, TOBACCO_GRINDER, "jei.category.cigarettemod.tobacco_grinder", "jei.cigarettemod.tobacco_grinder.description",
                    new ItemStack(cigaretteMod.TOBACCO_GRINDER_ITEM.get()));
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<TobaccoGrinderRecipe> holder, IFocusGroup focuses) {
            TobaccoGrinderRecipe recipe = holder.value();
            builder.addSlot(RecipeIngredientRole.INPUT, 26, 18).addIngredients(recipe.ingredient());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 18).addItemStack(recipe.result());
        }
    }

    private static class TobaccoWorkbenchCategory extends BaseCategory<TobaccoWorkbenchRecipe> {
        TobaccoWorkbenchCategory(IGuiHelper gui) {
            super(gui, TOBACCO_WORKBENCH, "jei.category.cigarettemod.tobacco_workbench", "jei.cigarettemod.tobacco_workbench.description",
                    new ItemStack(cigaretteMod.TOBACCO_WORKBENCH_ITEM.get()));
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<TobaccoWorkbenchRecipe> holder, IFocusGroup focuses) {
            TobaccoWorkbenchRecipe recipe = holder.value();
            builder.addSlot(RecipeIngredientRole.INPUT, 12, 18).addIngredients(recipe.first());
            builder.addSlot(RecipeIngredientRole.INPUT, 38, 18).addIngredients(recipe.second());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 112, 18).addItemStack(recipe.result());
        }

        @Override
        public void draw(RecipeHolder<TobaccoWorkbenchRecipe> recipe, mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
            this.plus.draw(graphics, 30, 21);
            this.arrow.draw(graphics, 72, 18);
            this.drawDescription(graphics);
        }
    }
}
