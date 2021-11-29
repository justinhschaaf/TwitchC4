package com.justinschaaf.twitchc4;

import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;

import java.io.File;

/**
 * A Twitch bot for playing Connect 4 in chat
 *
 * @author Justin H. Schaaf
 */
public class TwitchC4 {

    private static C4Config config;
    private static TwitchClient client;
    private static C4Commands cmds;

    public static void main(String[] args) {

        loadConfig();
        loadClient();
        loadCmds();

        scheduleShutdownTasks();

    }

    /**
     * Loads configurable values in {@link C4Config} from the configuration file,
     * and creates the config file and exits the program if it doesn't exist
     */
    private static void loadConfig() {

        File confFile = new File("config.conf");
        boolean exists = confFile.exists();

        FileConfig cfg = FileConfig.builder(confFile)
                .defaultResource("/config.conf")
                .sync()
                .build();
        cfg.load();

        // Configure before using!
        if (!exists) System.exit(0);

        config = new ObjectConverter().toObject(cfg, C4Config::new);

    }

    /**
     * Instantiates the {@link TwitchClient} this bot uses to connect to Twitch
     * and joins the channels defined in {@link C4Config#channels}
     */
    private static void loadClient() {

        // Build Twitch client
        OAuth2Credential cred = new OAuth2Credential("twitch", config.oauth);
        client = TwitchClientBuilder.builder()
                .withClientId(config.id)
                .withClientSecret(config.secret)
                .withEnableHelix(true)
                .withChatAccount(cred)
                .withEnableChat(true)
                .withDefaultAuthToken(cred)
                .build();

        for (String c : config.channels) client.getChat().joinChannel(c);

    }

    /**
     * Instantiates the {@link C4Commands} command handler and registers it as
     * an event listener with Event4j's {@link SimpleEventHandler}
     */
    private static void loadCmds() {

        // Register chat command handler
        cmds = new C4Commands();
        client.getEventManager().getEventHandler(SimpleEventHandler.class).registerListener(cmds);

    }

    /**
     * Schedules the tasks to be performed when the bot program is halted
     */
    private static void scheduleShutdownTasks() {
        Runtime.getRuntime().addShutdownHook(new C4Shutdown());
    }

    /**
     * Gets the {@link C4Config} for this bot
     * @return The config for this bot
     */
    public static C4Config getConfig() {
        return config;
    }

    /**
     * Gets the {@link TwitchClient} this bot uses to connect to Twitch
     * @return the TwitchClient for this bot
     */
    public static TwitchClient getClient() {
        return client;
    }

    /**
     * Gets the {@link C4Commands} this bot uses to handle commands
     * @return The command handler for this bot
     */
    public static C4Commands getCmds() {
        return cmds;
    }

}
