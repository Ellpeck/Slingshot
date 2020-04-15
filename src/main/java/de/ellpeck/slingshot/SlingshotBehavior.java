package de.ellpeck.slingshot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SlingshotBehavior {

    public final String name;
    public final ItemStack item;
    public final int chargeTime;
    public final IProjectileDelegate projectileDelegate;

    public SlingshotBehavior(String name, ItemStack item, int chargeTime, IProjectileDelegate projectileDelegate) {
        this.name = name;
        this.item = item;
        this.chargeTime = chargeTime;
        this.projectileDelegate = projectileDelegate;
    }

    public interface IProjectileDelegate {
        Entity createProjectile(World world, PlayerEntity player, ItemStack stack, ItemStack charged, ItemSlingshot item);
    }
}
