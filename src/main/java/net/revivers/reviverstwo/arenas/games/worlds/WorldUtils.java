package net.revivers.reviverstwo.arenas.games.worlds;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class WorldUtils {

    private static void copyFileStructure(File source, File target) {
        try {
            ArrayList<String> ignore = new ArrayList<>(Arrays.asList("uid.dat", "session.lock"));
            if (!ignore.contains(source.getName())) {
                if (source.isDirectory()) {
                    if (!target.exists())
                        if (!target.mkdirs())
                            throw new IOException("Couldn't create world directory!");
                    String[] files = source.list();
                    assert files != null;
                    for (String file : files) {
                        File srcFile = new File(source, file);
                        File destFile = new File(target, file);
                        copyFileStructure(srcFile, destFile);
                    }
                } else {
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0)
                        out.write(buffer, 0, length);
                    in.close();
                    out.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static World copyWorld(World originalWorld, String newWorldName) {
        Thread thread = new Thread(() -> {
            File copiedFile = new File(Bukkit.getWorldContainer(), newWorldName);
            copyFileStructure(originalWorld.getWorldFolder(), copiedFile);
        });
        thread.start();
        try {
            thread.join();
        } catch (Exception ignored) {}
        new WorldCreator(newWorldName).createWorld();
        return Bukkit.getWorld(newWorldName);
    }

    public static void deleteWorld(File path) {
        new Thread(() -> {
            try {
                // Try to delete using FileUtils
                FileUtils.deleteDirectory(path);
            } catch (Exception ignored) {}
        }).start();
    }

}
