package de.ellpeck.slingshot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SlingshotBehavior {

    public final String name;
    public final ItemStack item;
    public final int chargeTime;
    public final float damage;
    public final float velocity;
    public final IProjectileDelegate projectileDelegate;

    public SlingshotBehavior(String name, ItemStack item, int chargeTime, float damage, float velocity) {
        // TODO default projectile here
        this(name, item, chargeTime, damage, velocity, null);
    }

    public SlingshotBehavior(String name, ItemStack item, int chargeTime, float damage, float velocity, IProjectileDelegate projectileDelegate) {
        this.name = name;
        this.item = item;
        this.chargeTime = chargeTime;
        this.damage = damage;
        this.velocity = velocity;
        this.projectileDelegate = projectileDelegate;
    }

    public interface IProjectileDelegate {
        void createProjectile(World world, PlayerEntity player, ItemStack stack, ItemSlingshot item);
    }
}
