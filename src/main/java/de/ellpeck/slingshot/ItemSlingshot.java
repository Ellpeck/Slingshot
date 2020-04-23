package de.ellpeck.slingshot;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class ItemSlingshot extends Item {
    public ItemSlingshot() {
        super(new Properties().maxStackSize(1).maxDamage(672).group(ItemGroup.COMBAT));
        this.setRegistryName(Slingshot.ID, "slingshot");

        this.addPropertyOverride(new ResourceLocation(Slingshot.ID, "pull"), (stack, world, entity) -> entity != null && getChargedItem(stack).isEmpty() ? (stack.getUseDuration() - entity.getItemInUseCount()) / (float) getChargeTime(entity, stack) : 0);
        this.addPropertyOverride(new ResourceLocation(Slingshot.ID, "pulling"), (stack, world, entity) -> entity != null && entity.isHandActive() && entity.getActiveItemStack() == stack && getChargedItem(stack).isEmpty() ? 1 : 0);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        ItemStack charged = getChargedItem(stack);
        // Start charging if not already charged
        if (charged.isEmpty()) {
            // Do we even have ammo?
            if (getAmmo(playerIn).isEmpty())
                return new ActionResult<>(ActionResultType.PASS, stack);
            playerIn.setActiveHand(handIn);
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }

        if (!worldIn.isRemote) {
            SlingshotBehavior behavior = Registry.getBehavior(charged);
            behavior.projectileDelegate.createProjectiles(worldIn, playerIn, stack, charged, this);
            worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1, 1);
        }

        ItemStack remain = charged.copy();
        remain.shrink(1);
        setChargedItem(stack, remain);
        if (remain.getCount() > 0)
            playerIn.getCooldownTracker().setCooldown(this, 10);
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
        if (!getChargedItem(stack).isEmpty())
            return;
        int using = this.getUseDuration(stack) - timeLeft;
        int remain = getChargeTime(entityLiving, stack) - using;
        if (remain > 0)
            return;

        // Charge the slingshot
        ItemStack ammo = getAmmo(entityLiving);
        if (ammo.isEmpty())
            return;
        ItemStack charged = ammo.copy();
        int amount = Math.min(ammo.getCount(), EnchantmentHelper.getEnchantmentLevel(Registry.capacityEnchantment, stack) + 1);
        charged.setCount(amount);
        setChargedItem(stack, charged);
        ammo.shrink(amount);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public int getItemEnchantability() {
        return 10;
    }

    public static ItemStack getChargedItem(ItemStack stack) {
        if (!stack.hasTag())
            return ItemStack.EMPTY;
        CompoundNBT tag = stack.getTag().getCompound("charged");
        if (tag.isEmpty())
            return ItemStack.EMPTY;
        return ItemStack.read(tag);
    }

    private static void setChargedItem(ItemStack stack, ItemStack charged) {
        stack.getOrCreateTag().put("charged", charged.write(new CompoundNBT()));
    }

    private static int getChargeTime(LivingEntity entity, ItemStack stack) {
        ItemStack ammo = getAmmo(entity);
        if (ammo.isEmpty())
            return 0;
        int capacity = EnchantmentHelper.getEnchantmentLevel(Registry.capacityEnchantment, stack);
        return Registry.getBehavior(ammo).chargeTime * (capacity + 1);
    }

    private static ItemStack getAmmo(LivingEntity entity) {
        for (Hand hand : Hand.values()) {
            ItemStack held = entity.getHeldItem(hand);
            if (Registry.getBehavior(held) != null)
                return held;
        }
        if (!(entity instanceof PlayerEntity))
            return ItemStack.EMPTY;
        PlayerEntity player = (PlayerEntity) entity;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (Registry.getBehavior(stack) != null)
                return stack;
        }
        return ItemStack.EMPTY;
    }
}
