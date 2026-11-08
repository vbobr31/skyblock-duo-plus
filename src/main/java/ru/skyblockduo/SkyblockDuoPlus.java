package ru.skyblockduo;

import java.util.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class SkyblockDuoPlus extends JavaPlugin implements Listener {
  private UUID owner;
  private UUID invited;
  private final Map<UUID, Long> cooldown = new HashMap<UUID, Long>();
  private Location lucky;
  private final Material[] drops = new Material[] {
    Material.COBBLESTONE, Material.DIRT, Material.SAND, Material.GRAVEL, Material.LOG,
    Material.SAPLING, Material.COAL, Material.IRON_INGOT, Material.GOLD_INGOT,
    Material.REDSTONE, Material.LAPIS_LAZULI, Material.OBSIDIAN, Material.STRING,
    Material.BONE, Material.WHEAT, Material.POTATO_ITEM, Material.CARROT_ITEM,
    Material.SEEDS, Material.CACTUS, Material.SUGAR_CANE, Material.ENDER_PEARL,
    Material.DIAMOND, Material.EMERALD, Material.EXP_BOTTLE
  };

  public void onEnable() {
    saveDefaultConfig();
    Bukkit.getPluginManager().registerEvents(this, this);
    getCommand("island").setExecutor((sender, command, label, args) -> islandCommand(sender, label, args));
    getCommand("luckyblock").setExecutor((sender, command, label, args) -> luckyCommand(sender, label, args));
    String id = getConfig().getString("owner", "");
    if (!id.isEmpty()) try { owner = UUID.fromString(id); } catch (Exception ignored) {}
    String invite = getConfig().getString("invited", "");
    if (!invite.isEmpty()) try { invited = UUID.fromString(invite); } catch (Exception ignored) {}
    Bukkit.getScheduler().runTask(this, () -> {
      World w = Bukkit.getWorlds().get(0);
      createIsland(w);
    });
    getLogger().info("SkyblockDuoPlus enabled. No economy/shop/trading systems are included.");
  }

  private void createIsland(World w) {
    int x=0, z=0, y=100;
    // Expanded 31x31 starter island with a small resource-rich core.
    for (int xx=-15; xx<=15; xx++) for (int zz=-15; zz<=15; zz++) {
      double d=Math.sqrt(xx*xx+zz*zz);
      if (d<=15) {
        w.getBlockAt(x+xx,y-1,z+zz).setType(Material.STONE);
        w.getBlockAt(x+xx,y,z+zz).setType(d>13 ? Material.GRASS : Material.GRASS);
      }
    }
    for (int xx=-4;xx<=4;xx++) for(int zz=-4;zz<=4;zz++) {
      w.getBlockAt(x+xx,y-2,z+zz).setType(Material.DIRT);
    }
    w.getBlockAt(x-3,y+1,z-2).setType(Material.CHEST);
    w.getBlockAt(x+3,y+1,z+2).setType(Material.LOG);
    w.getBlockAt(x+3,y+2,z+2).setType(Material.LEAVES);
    w.getBlockAt(x-6,y+1,z).setType(Material.WATER);
    w.getBlockAt(x-7,y+1,z).setType(Material.LAVA);
    w.getBlockAt(x+7,y+1,z).setType(Material.COAL_ORE);
    w.getBlockAt(x+8,y+1,z).setType(Material.IRON_ORE);
    w.getBlockAt(x+9,y+1,z).setType(Material.GOLD_ORE);
    w.getBlockAt(x+10,y+1,z).setType(Material.REDSTONE_ORE);
    lucky = new Location(w, x, y+1, z+6);
    if (lucky.getBlock().getType()==Material.AIR) lucky.getBlock().setType(Material.SPONGE);
    w.setSpawnLocation(0,y+2,0);
  }

  private boolean islandCommand(CommandSender s, String label, String[] a) {
    if (!(s instanceof Player)) { s.sendMessage("Only players can use this command."); return true; }
    Player p=(Player)s;
    if (a.length==0) {
      if (owner==null) { owner=p.getUniqueId(); saveIds(); createIsland(p.getWorld()); p.sendMessage(ChatColor.GREEN+"Это ваш общий остров! Пригласите друга: /island invite <ник>"); }
      else if (allowed(p)) p.teleport(new Location(p.getWorld(),0,102,0));
      else p.sendMessage(ChatColor.RED+"Остров уже принадлежит другому игроку. Попросите владельца пригласить вас.");
      return true;
    }
    if (a[0].equalsIgnoreCase("invite") && a.length>1) {
      if (owner==null || !owner.equals(p.getUniqueId())) { p.sendMessage(ChatColor.RED+"Только владелец острова может приглашать."); return true; }
      Player target=Bukkit.getPlayerExact(a[1]);
      if(target==null){p.sendMessage(ChatColor.RED+"Игрок должен быть онлайн.");return true;}
      invited=target.getUniqueId(); saveIds();
      target.sendMessage(ChatColor.YELLOW+p.getName()+" приглашает вас на общий остров. Напишите /island accept");
      p.sendMessage(ChatColor.GREEN+"Приглашение отправлено.");
      return true;
    }
    if(a[0].equalsIgnoreCase("accept")) {
      if(invited!=null && invited.equals(p.getUniqueId())) { p.teleport(new Location(p.getWorld(),0,102,0)); p.sendMessage(ChatColor.GREEN+"Добро пожаловать на общий остров!"); }
      else p.sendMessage(ChatColor.RED+"У вас нет приглашения.");
      return true;
    }
    p.sendMessage(ChatColor.YELLOW+"Использование: /island, /island invite <ник>, /island accept");
    return true;
  }
  private boolean allowed(Player p) { return owner!=null && (owner.equals(p.getUniqueId()) || (invited!=null && invited.equals(p.getUniqueId()))); }
  private void saveIds(){ getConfig().set("owner",owner==null?"":owner.toString()); getConfig().set("invited",invited==null?"":invited.toString()); saveConfig(); }

  private boolean luckyCommand(CommandSender s, String label, String[] a) {
    if (!(s instanceof Player)) return true;
    Player p=(Player)s;
    if (!allowed(p) && owner!=null) { p.sendMessage(ChatColor.RED+"Сначала присоединитесь к острову."); return true; }
    p.getInventory().addItem(new ItemStack(Material.SPONGE,1));
    p.sendMessage(ChatColor.GREEN+"Получен блок удачи! Поставьте его и сломайте.");
    return true;
  }
  @EventHandler public void onBreak(BlockBreakEvent e) {
    Player p=e.getPlayer(); Block b=e.getBlock();
    if (lucky!=null && b.getWorld().equals(lucky.getWorld()) && b.getLocation().distanceSquared(lucky)<=1 && b.getType()==Material.SPONGE) {
      e.setCancelled(true);
      Long last=cooldown.get(p.getUniqueId());
      if(last!=null && System.currentTimeMillis()-last<500) return;
      cooldown.put(p.getUniqueId(),System.currentTimeMillis());
      Material reward=drops[new Random().nextInt(drops.length)];
      b.setType(Material.AIR);
      p.getInventory().addItem(new ItemStack(reward, reward==Material.DIAMOND||reward==Material.EMERALD?1:2));
      p.sendMessage(ChatColor.GOLD+"Lucky Block: "+ChatColor.YELLOW+reward.name());
      Bukkit.getScheduler().runTaskLater(this, () -> b.setType(Material.SPONGE), 60L);
      return;
    }
    if (owner!=null && !allowed(p) && b.getLocation().distanceSquared(new Location(b.getWorld(),0,100,0))<400) {
      e.setCancelled(true); p.sendMessage(ChatColor.RED+"Это общий остров. Попросите владельца пригласить вас.");
    }
  }
  @EventHandler public void onJoin(PlayerJoinEvent e) {
    if(owner==null) e.getPlayer().sendMessage(ChatColor.AQUA+"Создайте остров командой /island");
  }
}