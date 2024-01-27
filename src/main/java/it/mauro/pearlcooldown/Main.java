package it.mauro.pearlcooldown;

import org.bukkit.ChatColor;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin implements Listener {

    private Map<UUID, Long> cooldowns = new HashMap<>();
    private long defaultCooldown;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }
    private void loadConfig() {
        reloadConfig();
        defaultCooldown = getConfig().getLong("default-cooldown", 5000);
    }
    @EventHandler
    public void onPearlThrow(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof EnderPearl) {
            if (event.getEntity().getShooter() instanceof Player) {
                Player player = (Player) event.getEntity().getShooter();

                if (hasCooldown(player)) {
                    event.setCancelled(true);
                    long remainingTime = getRemainingTime(player);
                    player.sendMessage(ChatColor.RED + "Sei in cooldown! Aspetta " +ChatColor.DARK_RED + remainingTime / 1000 + ChatColor.RED + " secondi.");
                } else {
                    setCooldown(player);
                }
            }
        }
    }private long getRemainingTime(Player player) {
        if (cooldowns.containsKey(player.getUniqueId())) {
            long currentTime = System.currentTimeMillis();
            long cooldownTime = cooldowns.get(player.getUniqueId());
            long remainingTime = cooldownTime - currentTime;
            return Math.max(remainingTime, 0); // Ensure a non-negative result
        }
        return 0;
    }


    private boolean hasCooldown(Player player) {
        if (cooldowns.containsKey(player.getUniqueId())) {
            long currentTime = System.currentTimeMillis();
            long cooldownTime = cooldowns.get(player.getUniqueId());
            return currentTime < cooldownTime;
        }
        return false;
    }

    private void setCooldown(Player player) {
        long currentTime = System.currentTimeMillis();
        long cooldownTime = currentTime + defaultCooldown;
        cooldowns.put(player.getUniqueId(), cooldownTime);

        new BukkitRunnable() {
            @Override
            public void run() {
                cooldowns.remove(player.getUniqueId());
            }
        }.runTaskLater(this, defaultCooldown / 1000 * 20); // Convert cooldown from milliseconds to ticks
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
