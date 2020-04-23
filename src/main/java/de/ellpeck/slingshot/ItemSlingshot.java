package de.ellpeck.slingshot;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class ItemSlingshot extends Item {

    private static final float[] RELOAD_PERCENTAGES = new float[]{0.95F, 0.9F, 0.8F, 0.65F, 0.6F, 0.5F};

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

        // special tnt behavior
        SlingshotBehavior behavior = Registry.getBehavior(charged);
        if (behavior == Registry.tntBehavior) {
            if (handIn != Hand.MAIN_HAND)
                return new ActionResult<>(ActionResultType.FAIL, stack);
            long time = getLightTime(stack);
            if (time <= 0) {
                ItemStack off = playerIn.getHeldItemOffhand();
                if (off.getItem() != Items.FLINT_AND_STEEL)
                    return new ActionResult<>(ActionResultType.FAIL, stack);
                off.damageItem(1, playerIn, p -> p.sendBreakAnimation(Hand.OFF_HAND));

                setLightTime(stack, worldIn.getGameTime());
                return new ActionResult<>(ActionResultType.SUCCESS, stack);
            }
        }

        if (!worldIn.isRemote) {
            behavior.projectileDelegate.createProjectiles(worldIn, playerIn, stack, charged, this);
            worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1, 1);
        }
        stack.damageItem(1, playerIn, p -> playerIn.sendBreakAnimation(handIn));

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

        SlingshotBehavior behavior = Registry.getBehavior(ammo);
        if (behavior == Registry.tntBehavior && EnchantmentHelper.getEnchantmentLevel(Registry.ignitionEnchantment, stack) > 0)
            setLightTime(stack, worldIn.getGameTime());
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (worldIn.isRemote)
            return;
        ItemStack charged = getChargedItem(stack);
        if (charged.isEmpty())
            return;
        SlingshotBehavior behavior = Registry.getBehavior(charged);
        if (behavior != Registry.tntBehavior)
            return;
        long time = getLightTime(stack);
        if (time <= 0)
            return;
        int litTime = (int) (worldIn.getGameTime() - time);
        if (litTime >= 20 * 4) {
            worldIn.createExplosion(null, entityIn.posX, entityIn.posY, entityIn.posZ, 4, Explosion.Mode.BREAK);
            if (entityIn instanceof PlayerEntity)
                stack.damageItem(stack.getMaxDamage(), (PlayerEntity) entityIn, e -> e.sendBreakAnimation(Hand.MAIN_HAND));
            setChargedItem(stack, ItemStack.EMPTY);
            setLightTime(stack, 0);
        }
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

    public static long getLightTime(ItemStack stack) {
        if (!stack.hasTag())
            return 0;
        return stack.getTag().getLong("light_time");
    }

    public static void setLightTime(ItemStack stack, long time) {
        stack.getOrCreateTag().putLong("light_time", time);
    }

    private static int getChargeTime(LivingEntity entity, ItemStack stack) {
        ItemStack ammo = getAmmo(entity);
        if (ammo.isEmpty())
            return 0;
        int time = Registry.getBehavior(ammo).chargeTime;

        int capacity = EnchantmentHelper.getEnchantmentLevel(Registry.capacityEnchantment, stack);
        if (capacity > 0)
            time *= (capacity + 1);

        int reload = EnchantmentHelper.getEnchantmentLevel(Registry.reloadEnchantment, stack);
        if (reload > 0)
            time *= RELOAD_PERCENTAGES[reload - 1];

        return time;
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
