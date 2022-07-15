package net.revivers.reviverstwo.arenas.games;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class GameManager {

    private static final Hashtable<String, Game> games = new Hashtable<>();

    public static void createGame(Game game) {
        games.put(game.getID(), game);
    }
    public static void removeGame(String gameId)
    {
        games.remove(gameId);
    }
    public static Game getGame(String gameId)
    {
        return games.get(gameId);
    }

    public static List<Game> getGames() {
        return new ArrayList<>(games.values());
    }
    public static boolean isGameRegistered(Game game) {
        return games.containsValue(game);
    }

}
