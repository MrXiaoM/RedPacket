package sandtechnology.redpacket.util;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class FoliaLibScheduler {
    private final FoliaLib foliaLib;
    public FoliaLibScheduler(JavaPlugin plugin) {
        foliaLib = new FoliaLib(plugin);
    }

    public void runTask(Runnable runnable) {
        foliaLib.getScheduler().runNextTick((t) -> runnable.run());
    }

    public WrappedTask runTaskLater(Runnable runnable, long delay) {
        return foliaLib.getScheduler().runLater(runnable, delay);
    }

    public WrappedTask runTaskTimer(Runnable runnable, long delay, long period) {
        return foliaLib.getScheduler().runTimer(runnable, delay, period);
    }

    public void runTaskAsync(Runnable runnable) {
        foliaLib.getScheduler().runNextTick((t) -> runnable.run());
    }

    public WrappedTask runTaskLaterAsync(Runnable runnable, long delay) {
        return foliaLib.getScheduler().runLaterAsync(runnable, delay);
    }

    public WrappedTask runTaskTimerAsync(Runnable runnable, long delay, long period) {
        return foliaLib.getScheduler().runTimerAsync(runnable, delay, period);
    }

    public void cancelTasks() {
        foliaLib.getScheduler().cancelAllTasks();
    }
}
