package net.silthus.sstats.listener;

import lombok.Getter;
import net.silthus.sstats.entities.PlayerSession;
import net.silthus.sstats.entities.Statistic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerListener implements Listener {

    private static final String MAX_PLAYER_COUNT = "max_players";
    private static final String ONLINE_PLAYER_COUNT = "online_players";

    @Getter
    private final Map<UUID, PlayerSession> sessions = new HashMap<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {

        startSession(event.getPlayer());
        Statistic.PLAYER_COUNT.max(MAX_PLAYER_COUNT, Bukkit.getServer().getOnlinePlayers().size());
        Statistic.PLAYER_COUNT.set(ONLINE_PLAYER_COUNT, Bukkit.getServer().getOnlinePlayers().size());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {

        endSession(event.getPlayer(), PlayerSession.Reason.QUIT);
        Statistic.PLAYER_COUNT.set(ONLINE_PLAYER_COUNT, Bukkit.getServer().getOnlinePlayers().size() - 1);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {

        endSession(event.getPlayer(), PlayerSession.Reason.KICK);
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldChange(PlayerChangedWorldEvent event) {

        endSession(event.getPlayer(), PlayerSession.Reason.CHANGED_WORLD);
        startSession(event.getPlayer());
    }

    private void startSession(Player player) {

        sessions.put(player.getUniqueId(), PlayerSession.start(player));
    }

    private void endSession(Player player, PlayerSession.Reason reason) {

        Optional.ofNullable(sessions.remove(player.getUniqueId()))
                .ifPresent(playerSession -> playerSession.end(reason));
    }
}
