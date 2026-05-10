package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TobaccoGrinderBlock extends Block {
    public static final MapCodec<TobaccoGrinderBlock> CODEC = simpleCodec(TobaccoGrinderBlock::new);
    private static final VoxelShape SHAPE = box(1.0D, 0.0D, 1.0D, 15.0D, 13.0D, 15.0D);

    public TobaccoGrinderBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends TobaccoGrinderBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        SingleRecipeInput input = new SingleRecipeInput(stack);
        java.util.Optional<RecipeHolder<TobaccoGrinderRecipe>> recipe = level.getRecipeManager().getRecipeFor(
                SmokingWarningMod.TOBACCO_GRINDER_RECIPE_TYPE.get(),
                input,
                level);
        if (recipe.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        giveOrDrop(player, recipe.get().value().assemble(input, level.registryAccess()));
        level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.8F, 1.0F);
        player.displayClientMessage(Component.translatable("message.SmokingWarningMod.tobacco_grinder_use"), true);
        return ItemInteractionResult.CONSUME;
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
