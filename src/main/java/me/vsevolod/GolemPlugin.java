package me.vsevolod;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class GolemPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("GolemPlugin enabled!");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (var world : Bukkit.getWorlds()) {
                    for (var golem : world.getEntitiesByClass(IronGolem.class)) {
                        if (!(golem.getTarget() instanceof Player player)) continue;

                        double golemY = golem.getLocation().getY();
                        double playerY = player.getLocation().getY();
                        double heightDiff = playerY - golemY;

                        if (heightDiff >= 3 && golem.isOnGround()) {
                            getLogger().info("Голем " + golem.getUniqueId() + " готовится прыгнуть к игроку " + player.getName()
                                    + " (разница Y = " + heightDiff + ")");

                            // Прыжок
                            Vector velocity = golem.getVelocity();
                            velocity.setY(0.42);
                            golem.setVelocity(velocity);
                            getLogger().info("Голем прыгнул");

                            // Отложенная установка блока
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Location below = golem.getLocation().subtract(0, 1, 0).getBlock().getLocation();
                                    if (below.getBlock().getType().isAir()) {
                                        below.getBlock().setType(Material.COBBLESTONE);
                                        getLogger().info("Голем поставил блок под себя");
                                    } else {
                                        getLogger().info("Под големом уже есть блок: " + below.getBlock().getType());
                                    }
                                }
                            }.runTaskLater(GolemPlugin.this, 3L); // через 3 тика
                        }
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L); // 1 раз в секунду
    }
}
