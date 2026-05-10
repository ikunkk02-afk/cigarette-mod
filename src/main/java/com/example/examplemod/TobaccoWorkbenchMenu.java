package com.example.examplemod;

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

public class TobaccoWorkbenchMenu extends CraftingMenu {
    private final ContainerLevelAccess access;
    private final Player player;

    public TobaccoWorkbenchMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(containerId, playerInventory, access);
        this.access = access;
        this.player = playerInventory.player;

        Slot vanillaResultSlot = this.slots.get(0);
        TobaccoWorkbenchResultSlot resultSlot = new TobaccoWorkbenchResultSlot(vanillaResultSlot.container, vanillaResultSlot.x, vanillaResultSlot.y);
        resultSlot.index = 0;
        this.slots.set(0, resultSlot);
    }

    @Override
    public void slotsChanged(Container inventory) {
        this.access.execute((level, pos) -> this.updateTobaccoWorkbenchResult(level));
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, SmokingWarningMod.TOBACCO_WORKBENCH.get());
    }

    private void updateTobaccoWorkbenchResult(Level level) {
        if (level.isClientSide()) {
            return;
        }

        ItemStack result = this.getTobaccoWorkbenchResult(level);
        Slot resultSlot = this.slots.get(0);
        resultSlot.set(result);
        this.setRemoteSlot(0, result);

        if (this.player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), 0, result));
        }
    }

    private ItemStack getTobaccoWorkbenchResult(Level level) {
        return this.getTobaccoWorkbenchRecipe(level)
                .map(holder -> holder.value().assemble(this.getWorkbenchInput(), level.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }

    private java.util.Optional<RecipeHolder<TobaccoWorkbenchRecipe>> getTobaccoWorkbenchRecipe(Level level) {
        CraftingInput input = this.getWorkbenchInput();
        if (input.ingredientCount() != 2) {
            return java.util.Optional.empty();
        }
        return level.getRecipeManager().getRecipeFor(SmokingWarningMod.TOBACCO_WORKBENCH_RECIPE_TYPE.get(), input, level);
    }

    private CraftingInput getWorkbenchInput() {
        NonNullList<ItemStack> stacks = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int i = 0; i < 9; i++) {
            stacks.set(i, this.slots.get(i + 1).getItem());
        }
        return CraftingInput.of(3, 3, stacks);
    }

    private class TobaccoWorkbenchResultSlot extends Slot {
        TobaccoWorkbenchResultSlot(Container container, int x, int y) {
            super(container, 0, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            stack.onCraftedBy(player.level(), player, stack.getCount());
            consumeWorkbenchInputs(player);
            access.execute((level, pos) -> updateTobaccoWorkbenchResult(level));
            super.onTake(player, stack);
        }
    }

    private void consumeWorkbenchInputs(Player player) {
        for (int i = 1; i <= 9; i++) {
            Slot slot = this.slots.get(i);
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                continue;
            }

            ItemStack remainder = ItemStack.EMPTY;
            if (stack.getItem().hasCraftingRemainingItem()) {
                remainder = new ItemStack(stack.getItem().getCraftingRemainingItem());
            }

            stack.shrink(1);
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (!remainder.isEmpty()) {
                if (slot.getItem().isEmpty()) {
                    slot.set(remainder);
                } else if (!player.getInventory().add(remainder)) {
                    player.drop(remainder, false);
                }
            }
        }
    }
}
