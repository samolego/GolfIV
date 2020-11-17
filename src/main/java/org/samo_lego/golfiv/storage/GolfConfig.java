package org.samo_lego.golfiv.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import static org.samo_lego.golfiv.utils.BallLogger.logError;

public class GolfConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static class Main {
        public boolean preventStrangeCreativeItems = true;
        public boolean yesFall = true;
        public boolean noFly = true;
    }

    public static class Logging {
        public boolean toConsole = true;
        public boolean toOps = true;
    }

    public ArrayList<String> kickMessages = new ArrayList<>(Arrays.asList(
            "Only who dares wins!",
            "Bad Liar ...",
            "Script kiddo?",
            "No risk it, no biscuit!",
            "Playing God? How about no?",
            "Who flies high falls low",
            "If you cheat, you only cheat yourself.",
            "I'm not upset that you lied to me,\n I'm upset that from now on I can't believe you.",
            "Hax bad.",
            "You better check your client. It seems to be lying."
    ));

    public final GolfConfig.Main main = new Main();
    public final GolfConfig.Logging logging = new Logging();

    public static GolfConfig loadConfig(File configFile) {
        GolfConfig golfConfig;
        if(configFile.exists() && configFile.isFile()) {
            try(
                    FileInputStream fileInputStream = new FileInputStream(configFile);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            ) {
                golfConfig = GSON.fromJson(bufferedReader, GolfConfig.class);
            }
            catch (IOException e) {
                throw new RuntimeException("[GolfIV] Problem occurred when trying to load config: ", e);
            }
        }
        else {
            golfConfig = new GolfConfig();
        }
        golfConfig.saveConfig(configFile);

        return golfConfig;
    }

    public void saveConfig(File configFile) {
        try (
                FileOutputStream stream = new FileOutputStream(configFile);
                Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)
        ) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            logError("Problem occurred when saving config: " + e.getMessage());
        }
    }
}
