package com.untamedears.humbug;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Humbug extends JavaPlugin implements Listener {
  public static void severe(String message) {
    log_.severe("[Humbug] " + message);
  }

  public static void warning(String message) {
    log_.warning("[Humbug] " + message);
  }

  public static void info(String message) {
    log_.info("[Humbug] " + message);
  }

  public static void debug(String message) {
    if (debug_log_) {
      log_.info("[Humbug] " + message);
    }
  }

  public static Humbug getPlugin() {
    return plugin_;
  }

  private static final Logger log_ = Logger.getLogger("Humbug");
  private static Humbug plugin_ = null;
  private static boolean debug_log_ = false;
  private static boolean anvil_enabled_ = false;
  private static boolean ender_chest_enabled_ = false;
  private static boolean villager_trades_enabled_ = false;
  private static boolean wither_enabled_ = true;
  private static boolean wither_explosions_enabled_ = false;
  private static boolean wither_insta_break_enabled_ = false;

  public Humbug() {}

  // ================================================
  // Villager

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    if (event.isCancelled()) {
      return;
    }
    Entity npc = event.getRightClicked();
    if (!villager_trades_enabled_ &&
        npc.getType() == EntityType.VILLAGER) {
      event.setCancelled(true);
    }
  }

  // ================================================
  // Anvil and Ender Chest

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      Material material = event.getClickedBlock().getType();
      if ((!anvil_enabled_ && material.equals(Material.ANVIL)) ||
          (!ender_chest_enabled_ && material.equals(Material.ENDER_CHEST))) {
        event.setCancelled(true);
      }
    }
  }

  // ================================================
  // Wither

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onEntityChangeBlock(EntityChangeBlockEvent event) {
    EntityType npc_type = event.getEntity().getType();
    if (!wither_insta_break_enabled_ &&
        npc_type.equals(EntityType.WITHER)) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onEntityExplode(EntityExplodeEvent event) {
    boolean leave_blocks_intact = false;
    EntityType npc_type = event.getEntity().getType();
    if (!wither_explosions_enabled_ &&
        (npc_type.equals(EntityType.WITHER) ||
         npc_type.equals(EntityType.WITHER_SKULL))) {
      event.blockList().clear();
    }
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onCreatureSpawn(CreatureSpawnEvent event) {
    if (!event.getEntityType().equals(EntityType.WITHER)) {
      return;
    }
    if (!wither_enabled_) {
      event.setCancelled(true);
    }
  }

  // ================================================
  // General

  public void onEnable() {
    registerEvents();
    loadConfiguration();
    plugin_ = this;
    info("Enabled");
  }

  public boolean isInitiaized() {
    return plugin_ != null;
  }

  private void registerEvents() {
    getServer().getPluginManager().registerEvents(this, this);
  }

  private void loadConfiguration() {
    reloadConfig();
    FileConfiguration config = getConfig();
    config.options().copyDefaults(true);
    debug_log_ = config.getBoolean(
        "debug", false);
    anvil_enabled_ = config.getBoolean(
        "anvil", anvil_enabled_);
    ender_chest_enabled_ = config.getBoolean(
        "ender_chest", ender_chest_enabled_);
    villager_trades_enabled_ = config.getBoolean(
        "villager_trades", villager_trades_enabled_);
    wither_enabled_ = config.getBoolean(
        "wither", wither_enabled_);
    wither_explosions_enabled_ = config.getBoolean(
        "wither_explosions", wither_explosions_enabled_);
    wither_insta_break_enabled_ = config.getBoolean(
        "wither_insta_break", wither_insta_break_enabled_);
  }
}
