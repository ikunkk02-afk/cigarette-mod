package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class TobaccoWorkbenchRecipe implements Recipe<CraftingInput> {
    public static final MapCodec<TobaccoWorkbenchRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("first").forGetter(TobaccoWorkbenchRecipe::first),
            Ingredient.CODEC_NONEMPTY.fieldOf("second").forGetter(TobaccoWorkbenchRecipe::second),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(TobaccoWorkbenchRecipe::result)
    ).apply(instance, TobaccoWorkbenchRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TobaccoWorkbenchRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            TobaccoWorkbenchRecipe::first,
            Ingredient.CONTENTS_STREAM_CODEC,
            TobaccoWorkbenchRecipe::second,
            ItemStack.STREAM_CODEC,
            TobaccoWorkbenchRecipe::result,
            TobaccoWorkbenchRecipe::new);

    private final Ingredient first;
    private final Ingredient second;
    private final ItemStack result;

    public TobaccoWorkbenchRecipe(Ingredient first, Ingredient second, ItemStack result) {
        this.first = first;
        this.second = second;
        this.result = result;
    }

    public Ingredient first() {
        return this.first;
    }

    public Ingredient second() {
        return this.second;
    }

    public ItemStack result() {
        return this.result;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != 2) {
            return false;
        }

        ItemStack firstStack = ItemStack.EMPTY;
        ItemStack secondStack = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (firstStack.isEmpty()) {
                firstStack = stack;
            } else if (secondStack.isEmpty()) {
                secondStack = stack;
            } else {
                return false;
            }
        }

        return this.matchesPair(firstStack, secondStack);
    }

    public boolean matchesPair(ItemStack firstStack, ItemStack secondStack) {
        return this.first.test(firstStack) && this.second.test(secondStack)
                || this.first.test(secondStack) && this.second.test(firstStack);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, this.first, this.second);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SmokingWarningMod.TOBACCO_WORKBENCH_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return SmokingWarningMod.TOBACCO_WORKBENCH_RECIPE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<TobaccoWorkbenchRecipe> {
        @Override
        public MapCodec<TobaccoWorkbenchRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TobaccoWorkbenchRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
