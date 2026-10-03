package me.ichun.mods.portalgunclassic.portal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PortalConfig
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("portalgunremastered.json");

    private static volatile PortalConfig instance;

    public double range = 128.0;
    public int cooldownTicks = 10;
    public float soundVolume = 0.4F;
    public boolean crossDimension = true;
    public boolean teleportOtherEntities = true;

    public static PortalConfig get()
    {
        PortalConfig current = instance;
        if (current == null)
        {
            synchronized (PortalConfig.class)
            {
                current = instance;
                if (current == null)
                {
                    current = load();
                    instance = current;
                }
            }
        }
        return current;
    }

    public void save()
    {
        clamp();
        write(this);
    }

    private static PortalConfig load()
    {
        PortalConfig config = read();
        config.clamp();
        write(config);
        return config;
    }

    private static PortalConfig read()
    {
        if (Files.exists(FILE))
        {
            try (Reader reader = Files.newBufferedReader(FILE))
            {
                PortalConfig parsed = GSON.fromJson(reader, PortalConfig.class);
                if (parsed != null)
                {
                    return parsed;
                }
            }
            catch (IOException | JsonParseException e)
            {
                LOGGER.warn("Could not read {}, using defaults", FILE, e);
            }
        }
        return new PortalConfig();
    }

    private static void write(PortalConfig config)
    {
        try (Writer writer = Files.newBufferedWriter(FILE))
        {
            GSON.toJson(config, writer);
        }
        catch (IOException e)
        {
            LOGGER.warn("Could not write {}", FILE, e);
        }
    }

    private void clamp()
    {
        range = Mth.clamp(range, 8.0, 256.0);
        cooldownTicks = Mth.clamp(cooldownTicks, 1, 100);
        soundVolume = Mth.clamp(soundVolume, 0.0F, 1.0F);
    }
}
