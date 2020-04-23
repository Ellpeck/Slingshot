package de.ellpeck.slingshot;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

public class SlingshotEnchantment extends Enchantment {

    private final int levelAmounts;
    private final int minCost;
    private final int costPerLevel;
    private final int maxCostPerLevel;

    public SlingshotEnchantment(Rarity rarity, int levelAmounts, int minCost, int costPerLevel, int maxCostPerLevel) {
        super(rarity, EnchantmentType.WEAPON, new EquipmentSlotType[]{EquipmentSlotType.MAINHAND, EquipmentSlotType.OFFHAND});
        this.levelAmounts = levelAmounts;
        this.minCost = minCost;
        this.costPerLevel = costPerLevel;
        this.maxCostPerLevel = maxCostPerLevel;
    }

    @Override
    public int getMinEnchantability(int level) {
        return this.minCost + (level - 1) * this.costPerLevel;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return this.getMinEnchantability(level) + this.maxCostPerLevel;
    }

    @Override
    public int getMaxLevel() {
        return this.levelAmounts;
    }

    @Override
    public boolean canApply(ItemStack stack) {
        return stack.getItem() instanceof ItemSlingshot;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canApply(stack);
    }
}
