package com.untamedears.humbug;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class Config {
  private static Config global_instance_ = null;

  // ================================================
  // Configuration defaults
  private static final boolean debug_log_ = false;
  private static final boolean anvil_enabled_ = false;
  private static final boolean ender_chest_enabled_ = false;
  private static final boolean villager_trades_enabled_ = false;
  private static final boolean wither_enabled_ = true;
  private static final boolean wither_explosions_enabled_ = false;
  private static final boolean wither_insta_break_enabled_ = false;
  private static final boolean cobble_from_lava_enabled_ = false;
  private static final boolean ench_book_craftable_ = false;
  private static final boolean scale_protection_enchant_ = true;
  private static final int player_max_health_ = 20;
  // For Enchanted GOLDEN_APPLES
  private static final boolean ench_gold_app_edible_ = false;
  private static final boolean ench_gold_app_craftable_ = false;

  private static Config get() {
    return global_instance_;
  }

  public static Config initialize(Plugin plugin) {
    if (global_instance_ == null) {
      global_instance_ = new Config(plugin);
      global_instance_.load();
    }
    return global_instance_;
  }

  private FileConfiguration config_ = null;
  private Plugin plugin_ = null;

  public Config(Plugin plugin) {
    plugin_ = plugin;
  }

  public void load() {
    plugin_.reloadConfig();
    FileConfiguration config = plugin_.getConfig();
    config.options().copyDefaults(true);
    config_ = config;
  }

  public void reload() {
    plugin_.reloadConfig();
  }

  public void save() {
    plugin_.saveConfig();
  }

  public boolean getDebug() {
    return config_.getBoolean("debug", debug_log_);
  }

  public void setDebug(boolean value) {
    config_.set("debug", value);
  }

  public boolean getAnvilEnabled() {
    return config_.getBoolean("anvil", anvil_enabled_);
  }

  public void setAnvilEnabled(boolean value) {
    config_.set("anvil", value);
  }

  public boolean getEnderChestEnabled() {
    return config_.getBoolean("ender_chest", ender_chest_enabled_);
  }

  public void setEnderChestEnabled(boolean value) {
    config_.set("ender_chest", value);
  }

  public boolean getVillagerTradesEnabled() {
    return config_.getBoolean("villager_trades", villager_trades_enabled_);
  }

  public void setVillagerTradesEnabled(boolean value) {
    config_.set("villager_trades", value);
  }

  public boolean getWitherEnabled() {
    return config_.getBoolean("wither", wither_enabled_);
  }

  public void setWitherEnabled(boolean value) {
    config_.set("wither", value);
  }

  public boolean getWitherExplosionsEnabled() {
    return config_.getBoolean("wither_explosions", wither_explosions_enabled_);
  }

  public void setWitherExplosionsEnabled(boolean value) {
    config_.set("wither_explosions", value);
  }

  public boolean getWitherInstaBreakEnabled() {
    return config_.getBoolean("wither_insta_break", wither_insta_break_enabled_);
  }

  public void setWitherInstaBreakEnabled(boolean value) {
    config_.set("wither_insta_break", value);
  }

  public boolean getEnchGoldAppleEdible() {
    return config_.getBoolean("ench_gold_app_edible", ench_gold_app_edible_);
  }

  public void setEnchGoldAppleEdible(boolean value) {
    config_.set("ench_gold_app_edible", value);
  }

  public boolean getEnchGoldAppleCraftable() {
    return config_.getBoolean("ench_gold_app_craftable", ench_gold_app_craftable_);
  }

  public void setEnchGoldAppleCraftable(boolean value) {
    config_.set("ench_gold_app_craftable", value);
  }

  public boolean getCobbleFromLavaEnabled() {
    return config_.getBoolean("cobble_from_lava", cobble_from_lava_enabled_);
  }

  public void setCobbleFromLavaEnabled(boolean value) {
    config_.set("cobble_from_lava", value);
  }

  public boolean getEnchBookCraftable() {
    return config_.getBoolean("ench_book_craftable", ench_book_craftable_);
  }

  public void setEnchBookCraftable(boolean value) {
    config_.set("ench_book_craftable", value);
  }

  public boolean getScaleProtectionEnchant() {
    return config_.getBoolean("scale_protection_enchant", scale_protection_enchant_);
  }

  public void setScaleProtectionEnchant(boolean value) {
    config_.set("scale_protection_enchant", value);
  }

  public int getMaxHealth() {
	return config_.getInt("player_max_health", player_max_health_);
  }

  public void setMaxHealth(int value) {
    config_.set("player_max_health", value);
  }
}
