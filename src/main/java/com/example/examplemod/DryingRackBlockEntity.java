package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DryingRackBlockEntity extends BlockEntity {
    public static final int DEFAULT_DRYING_TIME_TICKS = 12000;
    public static final int MAX_LEAF_COUNT = 5;

    private int leafCount;
    private int dryingProgress;
    private boolean isDried;
    private ItemStack dryingInput = ItemStack.EMPTY;

    public DryingRackBlockEntity(BlockPos pos, BlockState blockState) {
        super(cigaretteMod.DRYING_RACK_BLOCK_ENTITY.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity blockEntity) {
        if (!blockEntity.hasLeaf() || blockEntity.isDried) {
            return;
        }

        int dryingTime = blockEntity.currentDryingTime(level);
        blockEntity.dryingProgress++;
        if (blockEntity.dryingProgress >= dryingTime) {
            blockEntity.isDried = true;
            blockEntity.dryingProgress = dryingTime;
            blockEntity.updateBlockState();
        }

        blockEntity.setChanged();
    }

    public boolean hasLeaf() {
        return this.leafCount > 0;
    }

    public int leafCount() {
        return this.leafCount;
    }

    public boolean isDried() {
        return this.hasLeaf() && this.isDried;
    }

    public boolean isFull() {
        return this.leafCount >= MAX_LEAF_COUNT;
    }

    public int dryingProgress() {
        return this.dryingProgress;
    }

    public int dryingTime() {
        return this.level == null ? DEFAULT_DRYING_TIME_TICKS : this.currentDryingTime(this.level);
    }

    public int dryingProgressPercent() {
        if (!this.hasLeaf()) {
            return 0;
        }
        return Math.min(100, this.dryingProgress * 100 / this.dryingTime());
    }

    public int remainingDryingSeconds() {
        if (!this.hasLeaf() || this.isDried()) {
            return 0;
        }
        return Math.max(0, (this.dryingTime() - this.dryingProgress + 19) / 20);
    }

    public boolean insertLeaf(ItemStack input) {
        if (this.isDried || this.isFull()) {
            return false;
        }
        if (input.isEmpty()) {
            return false;
        }
        if (this.hasLeaf() && !ItemStack.isSameItemSameComponents(this.dryingInput, input)) {
            return false;
        }

        if (!this.hasLeaf()) {
            this.dryingInput = input.copyWithCount(1);
        }
        this.leafCount++;
        this.updateBlockState();
        this.setChanged();
        return true;
    }

    public ItemStack collectLeaves() {
        if (!this.isDried()) {
            return ItemStack.EMPTY;
        }

        int collected = this.leafCount;
        ItemStack result = this.getDryingResult();
        result.setCount(result.getCount() * collected);
        this.leafCount = 0;
        this.dryingProgress = 0;
        this.isDried = false;
        this.dryingInput = ItemStack.EMPTY;
        this.updateBlockState();
        this.setChanged();
        return result;
    }

    public ItemStack getDropStack() {
        if (!this.hasLeaf()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = this.isDried() ? this.getDryingResult() : this.getDryingInput();
        stack.setCount(stack.getCount() * this.leafCount);
        return stack;
    }

    private ItemStack getDryingInput() {
        if (this.dryingInput.isEmpty()) {
            return new ItemStack(cigaretteMod.TOBACCO_LEAF.get());
        }
        return this.dryingInput.copy();
    }

    private ItemStack getDryingResult() {
        if (this.level == null) {
            return new ItemStack(cigaretteMod.DRIED_TOBACCO_LEAF.get());
        }
        return this.findRecipe(this.level)
                .map(holder -> holder.value().assemble(new SingleRecipeInput(this.getDryingInput()), this.level.registryAccess()))
                .filter(stack -> !stack.isEmpty())
                .orElseGet(() -> new ItemStack(cigaretteMod.DRIED_TOBACCO_LEAF.get()));
    }

    private int currentDryingTime(Level level) {
        return this.findRecipe(level)
                .map(holder -> holder.value().dryingTime())
                .orElse(DEFAULT_DRYING_TIME_TICKS);
    }

    private java.util.Optional<RecipeHolder<DryingRackRecipe>> findRecipe(Level level) {
        if (!this.hasLeaf()) {
            return java.util.Optional.empty();
        }
        return level.getRecipeManager().getRecipeFor(
                cigaretteMod.DRYING_RACK_RECIPE_TYPE.get(),
                new SingleRecipeInput(this.getDryingInput()),
                level);
    }

    private void updateBlockState() {
        if (this.level == null) {
            return;
        }

        BlockState state = this.getBlockState();
        if (state.hasProperty(DryingRackBlock.LEAF_COUNT) && state.hasProperty(DryingRackBlock.DRIED)) {
            BlockState newState = state
                    .setValue(DryingRackBlock.LEAF_COUNT, this.leafCount)
                    .setValue(DryingRackBlock.DRIED, this.isDried());
            if (newState.hasProperty(DryingRackBlock.LEGACY_STATE)) {
                DryingRackState legacyState = this.leafCount == 0
                        ? DryingRackState.EMPTY
                        : this.isDried() ? DryingRackState.DRIED : DryingRackState.DRYING;
                newState = newState.setValue(DryingRackBlock.LEGACY_STATE, legacyState);
            }
            if (newState != state) {
                this.level.setBlock(this.worldPosition, newState, 3);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("LeafCount", this.leafCount);
        tag.putInt("DryingProgress", this.dryingProgress);
        tag.putBoolean("IsDried", this.isDried);
        if (!this.dryingInput.isEmpty()) {
            tag.put("DryingInput", this.dryingInput.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("LeafCount")) {
            this.leafCount = Math.max(0, Math.min(MAX_LEAF_COUNT, tag.getInt("LeafCount")));
        } else {
            this.leafCount = tag.getBoolean("HasLeaf") ? 1 : 0;
        }
        this.dryingProgress = tag.getInt("DryingProgress");
        this.isDried = tag.getBoolean("IsDried");
        this.dryingInput = ItemStack.parseOptional(registries, tag.getCompound("DryingInput"));
        if (this.leafCount == 0) {
            this.dryingProgress = 0;
            this.isDried = false;
            this.dryingInput = ItemStack.EMPTY;
        } else if (this.dryingInput.isEmpty()) {
            this.dryingInput = new ItemStack(cigaretteMod.TOBACCO_LEAF.get());
        }
    }
}
