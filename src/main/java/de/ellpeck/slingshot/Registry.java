package de.ellpeck.slingshot;

import de.ellpeck.slingshot.entity.EntityProjectile;
import de.ellpeck.slingshot.entity.SeedProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Registry {

    private static final List<SlingshotBehavior> BEHAVIORS = new ArrayList<>();
    public static Item slingshot;

    public static EntityType<SeedProjectile> seedProjectile;

    public static void setup(FMLCommonSetupEvent event) {
        addSeedBehavior("carrot", Items.CARROT, 40, 3, 0.75F);
        addSeedBehavior("potato", Items.POTATO, 40, 3, 0.75F);
        addSeedBehavior("wheat_seeds", Items.WHEAT_SEEDS, 20, 0.75F, 0.45F);
        addSeedBehavior("beetroot_seeds", Items.BEETROOT_SEEDS, 20, 0.75F, 0.45F);
        addSeedBehavior("melon_seeds", Items.MELON_SEEDS, 20, 0.75F, 0.45F);
        addSeedBehavior("pumpkin_seeds", Items.PUMPKIN_SEEDS, 20, 0.75F, 0.45F);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(slingshot = new ItemSlingshot());
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().registerAll(
                seedProjectile = (EntityType<SeedProjectile>) EntityType.Builder
                        .<SeedProjectile>create(SeedProjectile::new, EntityClassification.MISC)
                        .size(0.25F, 0.25F).setShouldReceiveVelocityUpdates(true).setTrackingRange(64)
                        .setUpdateInterval(3).build(Slingshot.ID + ":seed").setRegistryName("seed")
        );
    }

    public static void addBehavior(SlingshotBehavior behavior) {
        BEHAVIORS.add(behavior);
        slingshot.addPropertyOverride(new ResourceLocation(Slingshot.ID, behavior.name), (stack, world, entity) -> entity != null && ItemSlingshot.getChargedItem(stack).isItemEqual(behavior.item) ? 1 : 0);
    }

    private static void addSeedBehavior(String name, Item seed, int chargeTime, float damage, float velocity) {
        addBehavior(new SlingshotBehavior(name, new ItemStack(seed), chargeTime, (world, player, stack, charged, item) -> {
            SeedProjectile projectile = new SeedProjectile(seedProjectile, player, world);
            projectile.setDamage(damage);
            projectile.setItem(charged.copy());
            projectile.dropItem = true;
            projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0, velocity, 0);
            return projectile;
        }));
    }

    public static SlingshotBehavior getBehavior(ItemStack stack) {
        if (stack.isEmpty())
            return null;
        for (SlingshotBehavior behavior : BEHAVIORS) {
            if (behavior.item.isItemEqual(stack))
                return behavior;
        }
        return null;
    }

    public static final class Client {
        public static void setup(FMLClientSetupEvent event) {
            Minecraft mc = event.getMinecraftSupplier().get();
            ItemRenderer renderer = mc.getItemRenderer();

            RenderingRegistry.registerEntityRenderingHandler(SeedProjectile.class, manager -> new SpriteRenderer<>(manager, renderer, 0.35F));
        }
    }
}
