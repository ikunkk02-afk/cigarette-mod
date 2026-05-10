package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class TobaccoGrinderRecipe implements Recipe<SingleRecipeInput> {
    public static final MapCodec<TobaccoGrinderRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(TobaccoGrinderRecipe::ingredient),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(TobaccoGrinderRecipe::result)
    ).apply(instance, TobaccoGrinderRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TobaccoGrinderRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            TobaccoGrinderRecipe::ingredient,
            ItemStack.STREAM_CODEC,
            TobaccoGrinderRecipe::result,
            TobaccoGrinderRecipe::new);

    private final Ingredient ingredient;
    private final ItemStack result;

    public TobaccoGrinderRecipe(Ingredient ingredient, ItemStack result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    public Ingredient ingredient() {
        return this.ingredient;
    }

    public ItemStack result() {
        return this.result;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, this.ingredient);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SmokingWarningMod.TOBACCO_GRINDER_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return SmokingWarningMod.TOBACCO_GRINDER_RECIPE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<TobaccoGrinderRecipe> {
        @Override
        public MapCodec<TobaccoGrinderRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TobaccoGrinderRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
