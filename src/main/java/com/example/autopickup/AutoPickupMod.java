package com.example.autopickup;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod(AutoPickupMod.MODID)
public class AutoPickupMod {

    public static final String MODID = "autopickup";

    private final Map<BlockPos, UUID> recentBreaks = new HashMap<>();

    public AutoPickupMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {

        if (event.getPlayer() == null) {
            return;
        }

        recentBreaks.put(
                event.getPos().immutable(),
                event.getPlayer().getUUID()
        );
    }

    @SubscribeEvent
    public void onItemSpawn(EntityJoinLevelEvent event) {

        if (!(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }

        if (event.getLevel().isClientSide()) {
            return;
        }

        BlockPos pos = itemEntity.blockPosition();

        UUID ownerUUID = recentBreaks.remove(pos);

        if (ownerUUID == null) {
            return;
        }

        Player player = event.getLevel().getPlayerByUUID(ownerUUID);

        if (player == null) {
            return;
        }

        ItemStack stack = itemEntity.getItem();

        boolean added = player.getInventory().add(stack);

        if (added) {
            itemEntity.discard();
        } else {
            itemEntity.setThrower(player.getUUID());
        }
    }

    @SubscribeEvent
    public void onMobDrops(LivingDropsEvent event) {

        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        event.getDrops().removeIf(itemEntity -> {

            ItemStack stack = itemEntity.getItem();

            return player.getInventory().add(stack);
        });
    }
}