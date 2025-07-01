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
                        if (!(golem.getTarget() instanceof Player player))
                            continue;

                        Location gLoc = golem.getLocation();
                        Location pLoc = player.getLocation();

                        double dy = pLoc.getY() - gLoc.getY();
                        double distance = gLoc.distance(pLoc);

                        if (dy >= 3 && distance < 8 && golem.isOnGround()) {
                            getLogger()
                                    .info("Голем " + golem.getUniqueId() + " начинает строиться к " + player.getName()
                                            + " (dy = " + dy + ", dist = " + distance + ")");

                            // Прыжок
                            Vector velocity = golem.getVelocity();
                            velocity.setY(0.42);
                            golem.setVelocity(velocity);

                            // Пауза перед блоком
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Location base = golem.getLocation();
                                    Location below = base.clone().subtract(0, 1, 0).getBlock().getLocation();

                                    // Повернуть голову вниз
                                    Location lookDown = base.clone();
                                    lookDown.setYaw(base.getYaw());
                                    lookDown.setPitch(80.0f);
                                    golem.teleport(lookDown);

                                    if (below.getBlock().getType().isAir()) {
                                        below.getBlock().setType(Material.COBBLESTONE);
                                        getLogger().info("Голем поставил блок под себя");
                                    } else {
                                        getLogger().info("Под големом уже есть блок: " + below.getBlock().getType());
                                    }

                                    // Вернуть взгляд через 6 тиков (~0.3 сек)
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            Location reset = golem.getLocation();
                                            reset.setPitch(0f);
                                            golem.teleport(reset);
                                            getLogger().info("Голем вернул взгляд вперёд");
                                        }
                                    }.runTaskLater(GolemPlugin.this, 6L);

                                }
                            }.runTaskLater(GolemPlugin.this, 3L); // поставить блок через 3 тика
                        }
                    }
                }
            }
        }.runTaskTimer(this, 20L, 10L); // запуск через 1 сек, затем каждые 10 тиков (0.5 сек)
    }
}
