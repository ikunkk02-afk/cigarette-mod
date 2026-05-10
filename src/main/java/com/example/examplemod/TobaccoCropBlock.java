package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TobaccoCropBlock extends CropBlock {
    public static final MapCodec<TobaccoCropBlock> CODEC = simpleCodec(TobaccoCropBlock::new);

    public TobaccoCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends CropBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return SmokingWarningMod.TOBACCO_SEEDS.get();
    }
}
