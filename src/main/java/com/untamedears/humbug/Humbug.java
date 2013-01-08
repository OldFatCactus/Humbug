package com.untamedears.humbug;

import java.util.Iterator;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.untamedears.humbug.Config;

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
    if (config_.getDebug()) {
      log_.info("[Humbug] " + message);
    }
  }

  public static Humbug getPlugin() {
    return global_instance_;
  }

  private static final Logger log_ = Logger.getLogger("Humbug");
  private static Humbug global_instance_ = null;
  private static Config config_ = null;
  private static int max_golden_apple_stack_ = 1;

  static {
    max_golden_apple_stack_ = Material.GOLDEN_APPLE.getMaxStackSize();
    if (max_golden_apple_stack_ > 64) {
      max_golden_apple_stack_ = 64;
    }
  }

  private Random prng_ = new Random();

  public Humbug() {}

  // ================================================
  // Villager Trading

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    if (config_.getVillagerTradesEnabled()) {
      return;
    }
    Entity npc = event.getRightClicked();
    if (npc == null) {
        return;
    }
    if (npc.getType() == EntityType.VILLAGER) {
      event.setCancelled(true);
    }
  }

  // ================================================
  // Anvil and Ender Chest usage

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (config_.getAnvilEnabled() &&
        config_.getEnderChestEnabled()) {
      return;
    }
    Action action = event.getAction();
    Material material = event.getClickedBlock().getType();
    boolean anvil = !config_.getAnvilEnabled() &&
                    action == Action.RIGHT_CLICK_BLOCK &&
                    material.equals(Material.ANVIL);
    boolean ender_chest = !config_.getEnderChestEnabled() &&
                          action == Action.RIGHT_CLICK_BLOCK &&
                          material.equals(Material.ENDER_CHEST);
    if (anvil || ender_chest) {
      event.setCancelled(true);
    }
  }

  // ================================================
  // Wither Insta-breaking and Explosions

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onEntityChangeBlock(EntityChangeBlockEvent event) {
    if (config_.getWitherInstaBreakEnabled()) {
      return;
    }
    Entity npc = event.getEntity();
    if (npc == null) {
        return;
    }
    EntityType npc_type = npc.getType();
    if (npc_type.equals(EntityType.WITHER)) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onEntityExplode(EntityExplodeEvent event) {
    if (config_.getWitherExplosionsEnabled()) {
      return;
    }
    boolean leave_blocks_intact = false;
    Entity npc = event.getEntity();
    if (npc == null) {
        return;
    }
    EntityType npc_type = npc.getType();
    if ((npc_type.equals(EntityType.WITHER) ||
         npc_type.equals(EntityType.WITHER_SKULL))) {
      event.blockList().clear();
    }
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onCreatureSpawn(CreatureSpawnEvent event) {
    if (config_.getWitherEnabled()) {
      return;
    }
    if (!event.getEntityType().equals(EntityType.WITHER)) {
      return;
    }
    event.setCancelled(true);
  }

  // ================================================
  // Enchanted Golden Apple

  public boolean isEnchantedGoldenApple(ItemStack item) {
    // Golden Apples are GOLDEN_APPLE with 0 durability
    // Enchanted Golden Apples are GOLDEN_APPLE with 1 durability
    if (item == null) {
      return false;
    }
    if (item.getDurability() != 1) {
      return false;
    }
    Material material = item.getType();
    return material.equals(Material.GOLDEN_APPLE);
  }

  public void replaceEnchantedGoldenApple(
      String player_name, ItemStack item, int inventory_max_stack_size) {
    if (!isEnchantedGoldenApple(item)) {
      return;
    }
    int stack_size = max_golden_apple_stack_;
    if (inventory_max_stack_size < max_golden_apple_stack_) {
      stack_size = inventory_max_stack_size;
    }
    info(String.format(
          "Replaced %d Enchanted with %d Normal Golden Apples for %s",
          item.getAmount(), stack_size, player_name));
    item.setDurability((short)0);
    item.setAmount(stack_size);
  }

  public void removeRecipies() {
    if (config_.getEnchGoldAppleCraftable()) {
      return;
    }
    Iterator<Recipe> it = getServer().recipeIterator();
    while (it.hasNext()) {
      Recipe recipe = it.next();
      ItemStack resulting_item = recipe.getResult();
      if ( // !ench_gold_app_craftable_ &&
          isEnchantedGoldenApple(resulting_item)) {
        it.remove();
        info("Enchanted Golden Apple Recipe disabled");
      }
    }
  }

  @EventHandler(priority = EventPriority.LOWEST) // ignoreCancelled=false
  public void onPlayerInteractAll(PlayerInteractEvent event) {
    // The event when eating is cancelled before even LOWEST fires when the
    //  player clicks on AIR.
    if (config_.getEnchGoldAppleEdible()) {
      return;
    }
    Player player = event.getPlayer();
    Inventory inventory = player.getInventory();
    ItemStack item = event.getItem();
    replaceEnchantedGoldenApple(
        player.getName(), item, inventory.getMaxStackSize());
  }

  // ================================================
  // Enchanted Book

  public boolean isNormalBook(ItemStack item) {
    if (item == null) {
      return false;
    }
    Material material = item.getType();
    return material.equals(Material.BOOK);
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
  public void onPrepareItemEnchantEvent(PrepareItemEnchantEvent event) {
    if (config_.getEnchBookCraftable()) {
        return;
    }
    ItemStack item = event.getItem();
    if (isNormalBook(item)) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
  public void onEnchantItemEvent(EnchantItemEvent event) {
    if (config_.getEnchBookCraftable()) {
        return;
    }
    ItemStack item = event.getItem();
    if (isNormalBook(item)) {
      event.setCancelled(true);
      Player player = event.getEnchanter();
      warning(
          "Prevented book enchant. This should not trigger. Watch player " +
          player.getName());
    }
  }

  // ================================================
  // Stop Cobble generation from lava+water

  private static final BlockFace[] faces_ = new BlockFace[] {
      BlockFace.NORTH,
      BlockFace.SOUTH,
      BlockFace.EAST,
      BlockFace.WEST,
      BlockFace.UP,
      BlockFace.DOWN
    };


  private BlockFace WaterAdjacentLava(Block lava_block) {
    for (BlockFace face : faces_) {
      Block block = lava_block.getRelative(face);
      Material material = block.getType();
      if (material.equals(Material.WATER) ||
          material.equals(Material.STATIONARY_WATER)) {
        return face;
      }
    }
    return BlockFace.SELF;
  }

  public void ConvertLava(Block block) {
    int data = (int)block.getData();
    if (data == 0) {
      return;
    }
    Material material = block.getType();
    if (!material.equals(Material.LAVA) &&
        !material.equals(Material.STATIONARY_LAVA)) {
      return;
    }
    if (isLavaSourceNear(block, 3)) {
      return;
    }
    BlockFace face = WaterAdjacentLava(block);
    if (face == BlockFace.SELF) {
      return;
    }
    block.setType(Material.AIR);
  }

  public boolean isLavaSourceNear(Block block, int ttl) {
    int data = (int)block.getData();
    if (data == 0) {
      Material material = block.getType();
      if (material.equals(Material.LAVA) ||
          material.equals(Material.STATIONARY_LAVA)) {
        return true;
      }
    }
    if (ttl <= 0) {
      return false;
    }
    for (BlockFace face : faces_) {
      Block child = block.getRelative(face);
      if (isLavaSourceNear(child, ttl - 1)) {
        return true;
      }
    }
    return false;
  }

  public void LavaAreaCheck(Block block, int ttl) {
    ConvertLava(block);
    if (ttl <= 0) {
      return;
    }
    for (BlockFace face : faces_) {
      Block child = block.getRelative(face);
      LavaAreaCheck(child, ttl - 1);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onBlockPhysicsEvent(BlockPhysicsEvent event) {
    if (config_.getCobbleFromLavaEnabled()) {
      return;
    }
    Block block = event.getBlock();
    LavaAreaCheck(block, 1);
  }

  // ================================================
  // Counteract 1.4.6 protection enchant nerf

  @EventHandler(priority = EventPriority.LOWEST) // ignoreCancelled=false
  public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
    if (!config_.getScaleProtectionEnchant()) {
        return;
    }
    int damage = event.getDamage();
    if (damage <= 0) {
      return;
    }
    DamageCause cause = event.getCause();
    if (!cause.equals(DamageCause.ENTITY_ATTACK) &&
            !cause.equals(DamageCause.PROJECTILE)) {
        return;
    }
    Entity entity = event.getEntity();
    if (!(entity instanceof Player)) {
      return;
    }
    Player defender = (Player)entity;
    PlayerInventory inventory = defender.getInventory();
    int enchant_level = 0;
    for (ItemStack armor : inventory.getArmorContents()) {
      enchant_level += armor.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
    }
    int damage_adjustment = 0;
    if (enchant_level >= 3 && enchant_level <= 6) {
      // 0 to 2
      damage_adjustment = prng_.nextInt(3);
    } else if (enchant_level >= 7 && enchant_level <= 10) {
      // 0 to 3
      damage_adjustment = prng_.nextInt(4);
    } else if (enchant_level >= 11 && enchant_level <= 14) {
      // 1 to 4
      damage_adjustment = prng_.nextInt(4) + 1;
    } else if (enchant_level >= 15) {
      // 2 to 4
      damage_adjustment = prng_.nextInt(3) + 2;
    }
    damage = Math.max(damage - damage_adjustment, 0);
    event.setDamage(damage);
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
  public void onPlayerJoinEvent(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    player.setMaxHealth(config_.getMaxHealth());
  }

  // ================================================
  // General

  public void onEnable() {
    registerEvents();
    registerCommands();
    loadConfiguration();
    removeRecipies();
    global_instance_ = this;
    info("Enabled");
  }

  public boolean isInitiaized() {
    return global_instance_ != null;
  }

  public boolean toBool(String value) {
    if (value.equals("1") || value.equalsIgnoreCase("true")) {
      return true;
    }
    return false;
  }

  public int toInt(String value, int default_value) {
    try {
      return Integer.parseInt(value);
    } catch(Exception e) {
      return default_value;
    }
  }

  public boolean onCommand(
      CommandSender sender,
      Command command,
      String label,
      String[] args) {
    if (!(sender instanceof ConsoleCommandSender) ||
        !command.getName().equals("humbug") ||
        args.length < 1) {
      return false;
    }
    String option = args[0];
    String value = null;
    boolean set = false;
    String msg = "";
    if (args.length > 1) {
      value = args[1];
      set = true;
    }
    if (option.equals("debug")) {
      if (set) {
        config_.setDebug(toBool(value));
      }
      msg = String.format("debug = %s", config_.getDebug());
    } else if (option.equals("anvil")) {
      if (set) {
        config_.setAnvilEnabled(toBool(value));
      }
      msg = String.format("anvil = %s", config_.getAnvilEnabled());
    } else if (option.equals("ender_chest")) {
      if (set) {
        config_.setEnderChestEnabled(toBool(value));
      }
      msg = String.format("ender_chest = %s", config_.getEnderChestEnabled());
    } else if (option.equals("villager_trades")) {
      if (set) {
        config_.setVillagerTradesEnabled(toBool(value));
      }
      msg = String.format("villager_trades = %s", config_.getVillagerTradesEnabled());
    } else if (option.equals("wither")) {
      if (set) {
        config_.setWitherEnabled(toBool(value));
      }
      msg = String.format("wither = %s", config_.getWitherEnabled());
    } else if (option.equals("wither_explosions")) {
      if (set) {
        config_.setWitherExplosionsEnabled(toBool(value));
      }
      msg = String.format(
          "wither_explosions = %s", config_.getWitherExplosionsEnabled());
    } else if (option.equals("wither_insta_break")) {
      if (set) {
        config_.setWitherInstaBreakEnabled(toBool(value));
      }
      msg = String.format(
          "wither_insta_break = %s", config_.getWitherInstaBreakEnabled());
    } else if (option.equals("ench_gold_app_edible")) {
      if (set) {
        config_.setEnchGoldAppleEdible(toBool(value));
      }
      msg = String.format(
          "ench_gold_app_edible = %s", config_.getEnchGoldAppleEdible());
    } else if (option.equals("ench_gold_app_craftable")) {
      if (set) {
        config_.setEnchGoldAppleCraftable(toBool(value));
      }
      msg = String.format(
          "ench_gold_app_craftable = %s", config_.getEnchGoldAppleCraftable());
    } else if (option.equals("cobble_from_lava")) {
      if (set) {
        config_.setCobbleFromLavaEnabled(toBool(value));
      }
      msg = String.format("cobble_from_lava = %s", config_.getCobbleFromLavaEnabled());
    } else if (option.equals("ench_book_craftable")) {
      if (set) {
        config_.setEnchBookCraftable(toBool(value));
      }
      msg = String.format("ench_book_craftable = %s", config_.getEnchBookCraftable());
    } else if (option.equals("scale_protection_enchant")) {
      if (set) {
        config_.setScaleProtectionEnchant(toBool(value));
      }
      msg = String.format(
          "scale_protection_enchant = %s", config_.getScaleProtectionEnchant());
    } else if (option.equals("player_max_health")) {
      if (set) {
        config_.setMaxHealth(toInt(value, config_.getMaxHealth()));
      }
      msg = String.format("player_max_health = %d", config_.getMaxHealth());
    } else if (option.equals("save")) {
      config_.save();
      msg = "Configuration saved";
    } else if (option.equals("reload")) {
      config_.reload();
      msg = "Configuration loaded";
    } else {
      msg = String.format("Unknown option %s", option);
    }
    sender.sendMessage(msg);
    return true;
  }

  public void registerCommands() {
    ConsoleCommandSender console = getServer().getConsoleSender();
    console.addAttachment(this, "humbug.console", true);
  }

  private void registerEvents() {
    getServer().getPluginManager().registerEvents(this, this);
  }

  private void loadConfiguration() {
    config_ = Config.initialize(this);
  }
}
