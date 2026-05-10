package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DryingRackBlock extends BaseEntityBlock {
    public static final MapCodec<DryingRackBlock> CODEC = simpleCodec(DryingRackBlock::new);
    public static final EnumProperty<DryingRackState> LEGACY_STATE = EnumProperty.create("state", DryingRackState.class);
    public static final IntegerProperty LEAF_COUNT = IntegerProperty.create("leaf_count", 0, DryingRackBlockEntity.MAX_LEAF_COUNT);
    public static final BooleanProperty DRIED = BooleanProperty.create("dried");
    private static final VoxelShape SHAPE = box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public DryingRackBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LEGACY_STATE, DryingRackState.EMPTY)
                .setValue(LEAF_COUNT, 0)
                .setValue(DRIED, false));
    }

    @Override
    public MapCodec<? extends DryingRackBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(LEGACY_STATE, LEAF_COUNT, DRIED);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DryingRackBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(blockEntityType, SmokingWarningMod.DRYING_RACK_BLOCK_ENTITY.get(), DryingRackBlockEntity::serverTick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        boolean hasRecipe = level.getRecipeManager().getRecipeFor(
                SmokingWarningMod.DRYING_RACK_RECIPE_TYPE.get(),
                new SingleRecipeInput(stack),
                level).isPresent();
        if (!hasRecipe) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide()) {
            if (state.getValue(DRIED)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            return ItemInteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof DryingRackBlockEntity dryingRack)) {
            return ItemInteractionResult.FAIL;
        }

        if (dryingRack.isDried()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (dryingRack.isFull()) {
            player.displayClientMessage(Component.translatable("message.smokingwarningmod.drying_rack_full"), true);
            return ItemInteractionResult.CONSUME;
        }

        if (dryingRack.insertLeaf(stack)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 0.7F, 0.9F);
            player.displayClientMessage(Component.translatable("message.smokingwarningmod.drying_rack_place_leaf", dryingRack.leafCount(), DryingRackBlockEntity.MAX_LEAF_COUNT), true);
            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.FAIL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (state.getValue(LEAF_COUNT) == 0) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof DryingRackBlockEntity dryingRack)) {
            return InteractionResult.PASS;
        }

        if (!dryingRack.isDried()) {
            player.displayClientMessage(Component.translatable("message.smokingwarningmod.drying_rack_not_done"), true);
            return InteractionResult.CONSUME;
        }

        ItemStack collected = dryingRack.collectLeaves();
        if (!collected.isEmpty()) {
            giveOrDrop(player, collected);
            level.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 0.7F, 1.15F);
            player.displayClientMessage(Component.translatable("message.smokingwarningmod.drying_rack_collect_leaf"), true);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof DryingRackBlockEntity dryingRack && dryingRack.hasLeaf()) {
                popResource(level, pos, dryingRack.getDropStack());
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
