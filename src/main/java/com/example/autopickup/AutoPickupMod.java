package com.example.autopickup;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(AutoPickupMod.MODID)
public class AutoPickupMod {

    public static final String MODID = "autopickup";

    public AutoPickupMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onMobDrops(LivingDropsEvent event) {

        if (!(event.getSource().getEntity() instanceof Player player)) return;

        event.getDrops().removeIf(itemEntity -> {
            ItemStack stack = itemEntity.getItem();
            return player.getInventory().add(stack);
        });
    }

    @SubscribeEvent
    public void onItemSpawn(EntityJoinLevelEvent event) {

        if (!(event.getEntity() instanceof ItemEntity item)) return;

        if (event.getLevel().isClientSide()) return;

        if (item.hasPickUpDelay()) return;

        Player player = event.getLevel().getNearestPlayer(item, 3.0);

        if (player == null) return;

        ItemStack stack = item.getItem();

        if (player.getInventory().add(stack)) {
            item.discard();
        }
    }
}