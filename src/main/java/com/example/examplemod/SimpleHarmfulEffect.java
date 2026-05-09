package com.example.examplemod;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class SimpleHarmfulEffect extends MobEffect {
    public SimpleHarmfulEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }
}
