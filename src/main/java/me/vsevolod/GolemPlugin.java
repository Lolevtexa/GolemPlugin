package me.vsevolod;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GolemPlugin extends JavaPlugin {

    private static final double BUILD_DISTANCE = 4.0;
    private static final int STUCK_TICKS_THRESHOLD = 60;
    private static final long BUILD_INTERVAL = 10L;

    private final Map<UUID, GolemState> golemStates = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("GolemPlugin enabled!");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (var world : Bukkit.getWorlds()) {
                    for (var golem : world.getEntitiesByClass(IronGolem.class)) {

                        UUID id = golem.getUniqueId();
                        GolemState state = golemStates.computeIfAbsent(id, k -> new GolemState(golem.getLocation()));

                        if (!(golem.getTarget() instanceof Player player))
                            continue;

                        Location gLoc = golem.getLocation();
                        Location pLoc = player.getLocation();

                        double dy = pLoc.getY() - gLoc.getY();
                        Location playerXZ = player.getLocation().clone();
                        playerXZ.setY(gLoc.getY());
                        double dxz = gLoc.distance(playerXZ);

                        // Обновить состояние — двигался ли голем?
                        if (gLoc.distanceSquared(state.lastPosition) < 0.05) {
                            state.ticksStationary += 1;
                        } else {
                            state.ticksStationary = 0;
                            state.lastPosition = gLoc.clone();
                        }

                        boolean closeEnough = dxz < BUILD_DISTANCE;
                        boolean stuck = state.ticksStationary >= STUCK_TICKS_THRESHOLD;

                        if (dy >= 3 && golem.isOnGround() && (closeEnough || stuck)) {
                            getLogger().info("Голем " + id + " строится (dy = " + dy + ", dxz = " + dxz + ", stuck = "
                                    + stuck + ")");

                            // Прыжок
                            Vector velocity = golem.getVelocity();
                            velocity.setY(0.42);
                            golem.setVelocity(velocity);

                            // Задержка перед установкой блока
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
                                        getLogger().info("Голем поставил блок");
                                    }

                                    // Вернуть взгляд вперёд
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            Location reset = golem.getLocation();
                                            reset.setPitch(0f);
                                            golem.teleport(reset);
                                        }
                                    }.runTaskLater(GolemPlugin.this, 6L);
                                }
                            }.runTaskLater(GolemPlugin.this, 3L);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 20L, BUILD_INTERVAL);
    }

    private static class GolemState {
        Location lastPosition;
        int ticksStationary;

        public GolemState(Location initialPos) {
            this.lastPosition = initialPos;
            this.ticksStationary = 0;
        }
    }
}
