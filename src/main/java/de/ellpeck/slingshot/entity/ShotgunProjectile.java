package de.ellpeck.slingshot.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ShotgunProjectile extends EntityProjectile {

    public ShotgunProjectile(EntityType<? extends ProjectileItemEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public ShotgunProjectile(EntityType<? extends ProjectileItemEntity> type, LivingEntity entity, World worldIn, ItemStack stack) {
        super(type, entity, worldIn, stack);
    }
}
