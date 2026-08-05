package com.xxmicloxx.NoteBlockAPI.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;

/**
 * Cross-version scheduler helper that transparently adapts between the classic
 * Bukkit scheduler (Paper / Spigot) and Folia's regionized schedulers.
 *
 * <p>On Folia the classic {@link org.bukkit.scheduler.BukkitScheduler} is removed
 * and every call throws {@link UnsupportedOperationException}, so all scheduling
 * must be routed through {@link org.bukkit.Server#getGlobalRegionScheduler()},
 * {@link org.bukkit.Server#getAsyncScheduler()} and the per-entity schedulers.
 * On plain Paper these new schedulers exist too and behave like the main thread /
 * async pool, which keeps this library usable on both platforms.</p>
 */
public final class FoliaCompat {

	private static final boolean FOLIA;

	static {
		boolean folia = false;
		try {
			Class.forName("io.papermc.paper.threadedregions.ThreadedRegionizer");
			folia = true;
		} catch (ClassNotFoundException ignored) {
			// not Folia
		}
		FOLIA = folia;
	}

	private FoliaCompat() {
	}

	public static boolean isFolia() {
		return FOLIA;
	}

	/**
	 * Runs on the global region thread (Folia) or the main thread (Paper).
	 * Suitable for firing Bukkit events and global state changes.
	 */
	public static void runSync(Plugin plugin, Runnable runnable) {
		if (FOLIA) {
			Bukkit.getGlobalRegionScheduler().run(plugin, task -> runnable.run());
		} else {
			Bukkit.getScheduler().runTask(plugin, runnable);
		}
	}

	/**
	 * Runs on the region that owns {@code location} (Folia) or the main thread
	 * (Paper). When {@code location} is {@code null} it falls back to the global
	 * region thread. Use this for firing events that listeners will react to with
	 * block/entity access.
	 */
	public static void runSync(Location location, Plugin plugin, Runnable runnable) {
		if (location != null) {
			if (FOLIA) {
				Bukkit.getRegionScheduler().run(plugin, location, task -> runnable.run());
			} else {
				Bukkit.getScheduler().runTask(plugin, runnable);
			}
		} else {
			runSync(plugin, runnable);
		}
	}

	/**
	 * Runs a task on a thread independent of the server tick process.
	 */
	public static void runAsync(Plugin plugin, Runnable runnable) {
		if (FOLIA) {
			Bukkit.getAsyncScheduler().runNow(plugin, task -> runnable.run());
		} else {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
		}
	}

	/**
	 * Runs a task after {@code delayTicks} on the global region thread (Folia) or
	 * the main thread (Paper).
	 */
	public static void runDelayed(Plugin plugin, Runnable runnable, long delayTicks) {
		if (FOLIA) {
			Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> runnable.run(), delayTicks);
		} else {
			Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
		}
	}

	/**
	 * Runs a repeating task on an async thread. Returns an opaque handle that can be
	 * passed to {@link #cancel(Object)}.
	 */
	public static Object runAtFixedRate(Plugin plugin, Runnable runnable, long initialDelayTicks, long periodTicks) {
		if (FOLIA) {
			// The async scheduler runs on wall-clock time, not ticks: 1 tick = 50ms.
			return Bukkit.getAsyncScheduler().runAtFixedRate(
					plugin, task -> runnable.run(),
					initialDelayTicks * 50L, periodTicks * 50L, TimeUnit.MILLISECONDS);
		}
		return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, initialDelayTicks, periodTicks);
	}

	/**
	 * Runs a task on the player's own region thread (Folia) or the main thread
	 * (Paper). Any access to that player's location / world data inside the task is
	 * therefore thread-safe on regionized servers.
	 */
	public static void runAtPlayer(Plugin plugin, Player player, Runnable runnable) {
		player.getScheduler().execute(plugin, runnable, () -> {}, 0L);
	}

	public static void cancel(Object handle) {
		if (handle instanceof io.papermc.paper.threadedregions.scheduler.ScheduledTask) {
			((io.papermc.paper.threadedregions.scheduler.ScheduledTask) handle).cancel();
		} else if (handle instanceof BukkitTask) {
			((BukkitTask) handle).cancel();
		}
	}
}