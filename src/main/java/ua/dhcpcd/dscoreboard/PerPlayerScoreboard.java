package ua.dhcpcd.dscoreboard;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ScoreComponent;
import net.megavex.scoreboardlibrary.ScoreboardLibraryImplementation;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.ScoreboardManager;
import net.megavex.scoreboardlibrary.api.interfaces.Closeable;
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar;
import net.megavex.scoreboardlibrary.exception.ScoreboardLibraryLoadException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PerPlayerScoreboard {

    private final Component title;
    private final Function<Player, List<Component>> lines;
    public final HashMap<Player, Sidebar> sidebars = new HashMap<>();
    private final ScoreboardManager scoreboardManager;
    public int maxLines;


    public PerPlayerScoreboard(JavaPlugin plugin, Component title, Function<Player, List<Component>> lines, int maxLines) {
        this.title = title;
        this.lines = lines;
        this.maxLines = maxLines;

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

        Sidebar sidebar = scoreboardManager.sidebar(this.maxLines);

        sidebar.title(title);

        sidebar.addPlayer(player);
        sidebar.visible(true);

        sidebars.put(player, sidebar);


        update(player);
    }

    public void update(Player player) {

        Sidebar sidebar = sidebars.get(player);

        if (sidebar == null) {
            show(player);
            return;
        }
        List<Component> components = lines.apply(player);

        for (int i = sidebar.maxLines(); i > 0; i--) {
            Component component = components.get(i);
            sidebar.line(i - 1, component);
        }
    }

    public void hide(Player player) {
        Sidebar sidebar = sidebars.get(player);
        if (sidebar == null) {
            return;
        }
        sidebar.visible(false);
    }

    public void update() {
        scoreboardManager.sidebars().forEach(s -> s.players().stream().findFirst().ifPresent(this::update));
    }

    public void destroy(Player p) {
        hide(p);

        Sidebar sidebar = sidebars.get(p);
        if (sidebar == null) {
            return;
        }

        sidebar.removePlayer(p);
        sidebars.remove(p);
        sidebars.remove(p);
    }

    public void destroy() {
        scoreboardManager.sidebars().forEach(s -> s.players().stream().findFirst().ifPresent(this::destroy));
    }

    public List<Player> getPlayers() {
        return scoreboardManager.sidebars().stream().flatMap(s -> s.players().stream()).collect(Collectors.toList());
    }
}
