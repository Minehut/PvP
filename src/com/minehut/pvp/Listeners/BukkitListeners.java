package com.minehut.pvp.Listeners;

import com.minehut.api.API;
import com.minehut.api.util.player.GamePlayer;
import com.minehut.api.util.player.Rank;
import com.minehut.commons.common.chat.C;
import com.minehut.commons.common.level.Level;
import com.minehut.commons.common.player.PlayerUtil;
import com.minehut.pvp.Core;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Fish;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.*;

/**
 * Created by luke on 5/16/15.
 */
public class BukkitListeners implements Listener {
    private Core core;
    public Location spawn;

    public BukkitListeners(Core core) {
        this.core = core;
        Bukkit.getServer().getPluginManager().registerEvents(this, core);

        /* Spawn. todo: load/update via database for spawn updates without needing to restart. */
        this.spawn = new Location(Bukkit.getServer().getWorlds().get(0), 0, 72, 0);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {

        // Make sure its a living mob
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity hurtEntity = (LivingEntity) event.getEntity();
        Projectile projectile = getProjectile(event);
        LivingEntity damagerEntity = null;

        // Check to see if there is a damager
        if (event instanceof EntityDamageByEntityEvent) {
            if (((EntityDamageByEntityEvent) event).getDamager() != null && ((EntityDamageByEntityEvent) event).getDamager() instanceof LivingEntity) {
                damagerEntity = (LivingEntity) ((EntityDamageByEntityEvent) event).getDamager();
            }
        }

        // Get the arrow shooter
        if (projectile != null) {
            if (projectile.getShooter() instanceof LivingEntity) {
                damagerEntity = (LivingEntity) projectile.getShooter();
            }
        }

        /* Check if hurt player is in arena */

        /* Check if damager player is in arena with hurt player */

    }

    Projectile getProjectile(EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent)) {
            return null;
        }
        EntityDamageByEntityEvent eventEE = (EntityDamageByEntityEvent)event;

        if ((eventEE.getDamager() instanceof Projectile)) {
            if (eventEE.getDamager() instanceof Fish) {
                return null;
            }
            return (Projectile) eventEE.getDamager();
        }
        return null;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(this.spawn);
        PlayerUtil.clearAll(event.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().teleport(this.spawn);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        /* Fall below spawn */
        if (event.getPlayer().getLocation().getY() <= 20) {
            event.setTo(this.spawn);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        /* todo: database stat updates (kills/deaths). */
    }



    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (!API.getAPI().getGamePlayer(event.getPlayer()).getRank().has(null, Rank.Admin, false)) {
            event.setCancelled(true);
            event.getPlayer().updateInventory();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        GamePlayer gamePlayer = API.getAPI().getGamePlayer(event.getPlayer());
        if (!gamePlayer.getRank().has(null, Rank.Admin, false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        GamePlayer gamePlayer = API.getAPI().getGamePlayer(event.getPlayer());
        if (!gamePlayer.getRank().has(null, Rank.Admin, false)) {
            event.setCancelled(true);
            event.getPlayer().updateInventory();
        }
    }

    @EventHandler
    public void onMoveInventory(InventoryMoveItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled())
            return;
        Player player = event.getPlayer();
        GamePlayer gamePlayer = API.getAPI().getGamePlayer(player);
        Rank rank = gamePlayer.getRank();
        int level = gamePlayer.getLevel();

        if (rank == Rank.regular) {
            event.setFormat(Level.getLevelColor(level) + Integer.toString(level) + " " + rank.getTag() + player.getDisplayName() + C.gray + ": " + C.gray + "%2$s");
        } else {
            event.setFormat(Level.getLevelColor(level) + Integer.toString(level) + " " + rank.getTag() + player.getDisplayName() + C.white + ": " + C.white + "%2$s");
        }
    }
}
