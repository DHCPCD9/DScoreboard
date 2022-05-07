package ua.dhcpcd.dscoreboard;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ScoreComponent;
import net.megavex.scoreboardlibrary.ScoreboardLibraryImplementation;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.ScoreboardManager;
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar;
import net.megavex.scoreboardlibrary.exception.ScoreboardLibraryLoadException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.List;
import java.util.function.Function;

public class PerPlayerScoreboard {

    private final Component title;
    private final Function<Player, List<Component>> lines;
    private final ScoreboardManager scoreboardManager;


    public PerPlayerScoreboard(JavaPlugin plugin, Component title, Function<Player, List<Component>> lines) {
        this.title = title;
        this.lines = lines;

        try {
            ScoreboardLibraryImplementation.init();
        } catch (ScoreboardLibraryLoadException e) {
            throw new RuntimeException(e);
        }

        scoreboardManager = ScoreboardManager.scoreboardManager(plugin);


    }

    public Component getTitle() {
        return title;
    }

    public void show(Player player) {

        Sidebar sidebar = scoreboardManager.sidebar(Sidebar.MAX_LINES);
        sidebar.title(title);

        sidebar.addPlayer(player);

        update(player);
    }

    public void update(Player player) {

        Sidebar sidebar = scoreboardManager.sidebars().stream().filter(s -> s.players().contains(player)).findFirst().orElse(null);

        if (sidebar == null) {
            show(player);
            return;
        }
        List<Component> components = lines.apply(player);

        for (int i = sidebar.maxLines(); i > 0; i--) {
            Component component = components.get(i);
            sidebar.line(i, component);
        }
    }

    public void hide(Player player) {
        scoreboardManager.sidebars().stream().filter(s -> s.players().contains(player)).findFirst().ifPresent(s -> s.visible(false));
    }

    public void update() {
        scoreboardManager.sidebars().forEach(s -> s.players().stream().findFirst().ifPresent(this::update));
    }
}
