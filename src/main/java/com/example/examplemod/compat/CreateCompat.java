package com.example.examplemod.compat;

import com.example.examplemod.Config;
import com.example.examplemod.cigaretteMod;
import net.neoforged.fml.ModList;

/**
 * Optional Create mod compatibility entry point.
 * All direct Create API references must be guarded by {@link #isActive()}
 * and kept in this package to avoid ClassNotFoundException when Create is absent.
 */
public final class CreateCompat {

    private static boolean createLoaded;
    private static boolean initialized;

    private CreateCompat() {}

    public static void init() {
        if (initialized) return;
        initialized = true;

        if (!Config.ENABLE_CREATE_COMPAT.get()) {
            cigaretteMod.LOGGER.info("Create compatibility disabled in config.");
            return;
        }

        if (ModList.get().isLoaded("create")) {
            createLoaded = true;
            cigaretteMod.LOGGER.info("Create mod detected — enabling mechanical tobacco processing.");
        } else {
            cigaretteMod.LOGGER.info("Create mod not found — using vanilla tobacco processing only.");
        }
    }

    public static boolean isActive() {
        return createLoaded;
    }
}
