package de.ellpeck.slingshot;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ItemSlingshot extends Item {
    public ItemSlingshot() {
        super(new Properties().maxStackSize(1).maxDamage(672).group(ItemGroup.COMBAT));
        this.setRegistryName(Slingshot.ID, "slingshot");

        this.addPropertyOverride(new ResourceLocation(Slingshot.ID, "pull"), (stack, world, entity) -> entity != null && !isCharged(stack) ? (stack.getUseDuration() - entity.getItemInUseCount()) / (float) getChargeTime(entity) : 0);
        this.addPropertyOverride(new ResourceLocation(Slingshot.ID, "pulling"), (stack, world, entity) -> entity != null && entity.isHandActive() && entity.getActiveItemStack() == stack && !isCharged(stack) ? 1 : 0);
        this.addPropertyOverride(new ResourceLocation(Slingshot.ID, "charged"), (stack, world, entity) -> entity != null && isCharged(stack) ? 1 : 0);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        // Start charging if not already charged
        if (!isCharged(stack)) {
            // Do we even have ammo?
            if (getAmmo(playerIn).isEmpty())
                return new ActionResult<>(ActionResultType.PASS, stack);
            playerIn.setActiveHand(handIn);
            System.out.println("Starting...");
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }

        // TODO shoot the projectile
        System.out.println("SHOOT");

        setCharged(stack, false);
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
        if (isCharged(stack))
            return;
        int using = this.getUseDuration(stack) - timeLeft;
        int remain = getChargeTime(entityLiving) - using;
        if (remain > 0)
            return;
        setCharged(stack, true);
        System.out.println("CHARGED");
        // TODO remove ammo and store ammo state
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    private static boolean isCharged(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("charged");
    }

    private static void setCharged(ItemStack stack, boolean charged) {
        stack.getOrCreateTag().putBoolean("charged", charged);
    }

    private static int getChargeTime(LivingEntity entity) {
        // TODO
        return 100;
    }

    private static ItemStack getAmmo(LivingEntity entity) {
        // TODO
        return new ItemStack(Items.CARROT);
    }
}
