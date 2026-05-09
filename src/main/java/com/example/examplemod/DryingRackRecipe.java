package com.example.examplemod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class DryingRackRecipe implements Recipe<SingleRecipeInput> {
    public static final MapCodec<DryingRackRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(DryingRackRecipe::ingredient),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(DryingRackRecipe::result),
            Codec.INT.optionalFieldOf("drying_time", DryingRackBlockEntity.DEFAULT_DRYING_TIME_TICKS).forGetter(DryingRackRecipe::dryingTime)
    ).apply(instance, DryingRackRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DryingRackRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            DryingRackRecipe::ingredient,
            ItemStack.STREAM_CODEC,
            DryingRackRecipe::result,
            ByteBufCodecs.VAR_INT,
            DryingRackRecipe::dryingTime,
            DryingRackRecipe::new);

    private final Ingredient ingredient;
    private final ItemStack result;
    private final int dryingTime;

    public DryingRackRecipe(Ingredient ingredient, ItemStack result, int dryingTime) {
        this.ingredient = ingredient;
        this.result = result;
        this.dryingTime = Math.max(1, dryingTime);
    }

    public Ingredient ingredient() {
        return this.ingredient;
    }

    public ItemStack result() {
        return this.result;
    }

    public int dryingTime() {
        return this.dryingTime;
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
        return cigaretteMod.DRYING_RACK_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return cigaretteMod.DRYING_RACK_RECIPE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<DryingRackRecipe> {
        @Override
        public MapCodec<DryingRackRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DryingRackRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
