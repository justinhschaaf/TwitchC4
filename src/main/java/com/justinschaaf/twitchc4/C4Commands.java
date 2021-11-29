package com.justinschaaf.twitchc4;

import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.helix.domain.StreamList;

import java.util.*;

/**
 * Manages all the commands this bot accepts
 *
 * @author Justin H. Schaaf (justinschaaf.com)
 */
public class C4Commands {

    /**
     * Stores the currently active challenges in each channel
     */
    public final HashMap<String, LinkedList<C4Challenge>> CHALLENGES = new HashMap<>();

    /**
     * Stores the currently active games in each channel
     */
    public final HashMap<String, LinkedList<C4Game>> GAMES = new HashMap<>();

    /**
     * Stores whether or not games are manually enabled or disabled by mods in each channel
     */
    private final HashMap<String, Boolean> GAMES_ENABLED = new HashMap<>();

    /**
     * The primary method for receiving and handling commands
     * @param e The {@link ChannelMessageEvent} which may contain a command
     */
    @EventSubscriber
    public void handle(ChannelMessageEvent e) {

        String[] tokens = e.getMessage().split(" ");

        StreamList streams = TwitchC4
                .getClient()
                .getHelix()
                .getStreams(
                        null,
                        null,
                        null,
                        1,
                        null,
                        null,
                        null,
                        List.of(e.getChannel().getName())
                ).execute();

        if (tokens.length > 0 && tokens[0].startsWith(TwitchC4.getConfig().cmdPrefix)) {

            ensureDataExists(e.getChannel().getName());

            // If offline games not enabled and streamer is offline, abort
            // If online games not enabled and streamer is online, abort
            if (
                    (!TwitchC4.getConfig().gameWhileOffline && streams.getStreams().size() == 0) ||
                    (!TwitchC4.getConfig().gameWhileOnline && streams.getStreams().size() > 0)
            ) return;

            tokens[0] = tokens[0].replaceFirst(TwitchC4.getConfig().cmdPrefix, "");

            System.out.println(Arrays.toString(tokens));

            switch (tokens[0]) {

                case "accept":
                case "challenge":
                case "play":
                    if (GAMES_ENABLED.get(e.getChannel().getName()))
                        play(e.getChannel().getName(), e.getUser().getName(), Arrays.copyOfRange(tokens, 1, tokens.length));
                    break;

                case "put":
                    if (GAMES_ENABLED.get(e.getChannel().getName()))
                        put(e.getChannel().getName(), e.getUser().getName(), Arrays.copyOfRange(tokens, 1, tokens.length));
                    break;

                case "decline":
                case "forfeit":
                    if (GAMES_ENABLED.get(e.getChannel().getName()))
                        forfeit(e.getChannel().getName(), e.getUser().getName());
                    break;

                case "help":
                    if (GAMES_ENABLED.get(e.getChannel().getName()))
                        help(e.getChannel().getName());
                    break;

                case "toggle": // Mod toggle
                    if (TwitchC4.getConfig().cmdModToggle) toggle(e.getChannel().getName(), e.getUser().getName(), e.getPermissions());
                    else unknown(e.getChannel().getName(), e.getUser().getName());
                    break;

                case "author":
                case "info":
                case "about": // Author attribution

                    if (GAMES_ENABLED.get(e.getChannel().getName())) {
                        if (TwitchC4.getConfig().cmdAttribution) about(e.getChannel().getName());
                        else unknown(e.getChannel().getName(), e.getUser().getName());
                    }

                    break;

                default:
                    if (GAMES_ENABLED.get(e.getChannel().getName()))
                        unknown(e.getChannel().getName(), e.getUser().getName());

            }

        }

    }

    /**
     * The c4!play command. Usage: c4!play -OR- c4!play [user]
     * If no argument is provided, creates a new request to anyone
     * If one user argument is provided, either accepts the challenge
     * from the given user or sends them a challenge
     *
     * @param channel The channel in which this command was sent
     * @param user The user which executed this command
     * @param args The arguments this command was executed with
     */
    private void play(String channel, String user, String[] args) {

        C4Game fGame = findUserInGame(channel, user);
        C4Challenge fChallenge = findUserInChallenge(channel, user);

        if (fGame != null) C4Messages.send(channel, TwitchC4.getConfig().locErrorAlreadyIngame, "%p%", user);
        else if (fChallenge != null && fChallenge.getFrom().equalsIgnoreCase(user)) C4Messages.send(
                channel,
                TwitchC4.getConfig().locErrorAlreadyChallenging,
                "%p%", user
        );
        else if (args.length > 0) {

            args[0] = args[0].strip();

            C4Game tGame = findUserInGame(channel, args[0]);
            C4Challenge tChallenge = findUserInChallenge(channel, args[0]);

            if (tGame != null) C4Messages.send(channel, TwitchC4.getConfig().locErrorOtherIngame, "%f%", user, "%t%", args[0]);
            else if (tChallenge == null) {

                // Create a new challenge
                if (
                        !TwitchC4.getConfig().gameConcurrent &&
                        (CHALLENGES.get(channel).size() > 0 || GAMES.get(channel).size() > 0)
                ) C4Messages.send(
                        channel,
                        TwitchC4.getConfig().locErrorAlreadyOngoing,
                        "%p%", user
                );
                else {

                    CHALLENGES.get(channel).add(new C4Challenge(channel, user, args[0]));
                    C4Messages.send(
                            channel,
                            TwitchC4.getConfig().locChallengeSendTo,
                            "%f%", user,
                            "%t%", args[0]
                    );

                }

            } else if (tChallenge.getTo() == null || tChallenge.getTo().equalsIgnoreCase(user)) {

                // Accept the challenge
                tChallenge.setTo(user);
                GAMES.get(channel).add(tChallenge.accept());

            } else C4Messages.send(channel, TwitchC4.getConfig().locErrorGeneric); // Challenge was not meant for you

        } else {

            // No arguments provided

            boolean acceptedRandom = false;

            // Check for random challenge to accept
            for (C4Challenge c : CHALLENGES.get(channel))
                if (c.getTo() == null) {

                    c.setTo(user);
                    GAMES.get(channel).add(c.accept());
                    acceptedRandom = true;
                    break;

                }

            // Create a new challenge
            if (!acceptedRandom) {

                if (
                        !TwitchC4.getConfig().gameConcurrent &&
                        (CHALLENGES.get(channel).size() > 0 || GAMES.get(channel).size() > 0)
                ) C4Messages.send(
                        channel,
                        TwitchC4.getConfig().locErrorAlreadyOngoing,
                        "%p%", user
                );
                else {

                    CHALLENGES.get(channel).add(new C4Challenge(channel, user));
                    C4Messages.send(channel, TwitchC4.getConfig().locChallengeSend, "%f%", user);

                }

            }

        }

    }

    /**
     * The c4!put command. Usage: c4!put [1-7]
     * If the user is in a game and it's their turn, places a
     * coin/chip/whatever in the given column
     * Else spits out an error
     *
     * @param channel The channel in which this command was sent
     * @param user The user which executed this command
     * @param args The arguments this command was executed with
     */
    private void put(String channel, String user, String[] args) {

        C4Game game = findUserInGame(channel, user);

        if (game == null) C4Messages.send(channel, TwitchC4.getConfig().locErrorNotIngame, "%p%", user);
        else if (!user.equalsIgnoreCase(game.getPlayerTurn())) C4Messages.send(channel, TwitchC4.getConfig().locErrorNotTurn, "%p%", user);
        else if (args.length == 0) C4Messages.send(channel, TwitchC4.getConfig().locErrorNoColumn);
        else {

            try {

                int col = Integer.parseInt(args[0].strip());

                if (game.isValidMove(col)) game.doTurn(col);
                else C4Messages.send(channel, TwitchC4.getConfig().locErrorInvalidColumn, "%p%", user);

            } catch (NumberFormatException e) {
                C4Messages.send(channel, TwitchC4.getConfig().locErrorInvalidColumn, "%p%", user);
            }

        }

    }

    /**
     * The c4!forfeit command. Usage: c4!forfeit
     * If the user is in a game, forfeits the game
     * If the user has sent a challenge, cancels the challenge
     * If the user has received a challenge, declines the challenge
     *
     * @param channel The channel in which this command was sent
     * @param user The user which executed this command
     */
    private void forfeit(String channel, String user) {

        C4Game game = findUserInGame(channel, user);
        C4Challenge challenge = findUserInChallenge(channel, user);

        if (game != null) game.forfeit(user);
        else if (challenge != null) {

            if (challenge.getFrom().equalsIgnoreCase(user)) C4Messages.send(
                    channel,
                    TwitchC4.getConfig().locForfeitSentChallenge,
                    "%f%", challenge.getFrom()
            );
            else C4Messages.send(
                    channel,
                    TwitchC4.getConfig().locForfeitDeclineChallenge,
                    "%f%", challenge.getFrom(),
                    "%t%", challenge.getTo()
            );

            challenge.cancel();

        } else C4Messages.send(channel, TwitchC4.getConfig().locErrorNotIngame, "%p%", user);

    }

    /**
     * The c4!help command. Usage: c4!help
     * Displays a message instructing users on how to use the bot
     *
     * @param channel The channel in which this command was sent
     */
    private void help(String channel) {
        C4Messages.send(channel, TwitchC4.getConfig().locHelp);
    }

    /**
     * The c4!toggle command. Usage: c4!toggle
     * Manually toggles whether games can be played or not. Games
     * being enabled by this. does not override default restrictions
     * (e.g. streamer being online with only offline games enabled)
     *
     * @param channel The channel in which this command was sent
     * @param user The user which executed this command
     * @param perms The permissions the command executor has
     */
    private void toggle(String channel, String user, Set<CommandPermission> perms) {

        if (perms.contains(CommandPermission.MODERATOR) || perms.contains(CommandPermission.BROADCASTER)) {

            boolean enabled = !GAMES_ENABLED.get(channel);
            GAMES_ENABLED.put(channel, enabled);

            if (enabled) C4Messages.send(channel, TwitchC4.getConfig().locToggleEnable, "%p%", user);
            else C4Messages.send(channel, TwitchC4.getConfig().locToggleDisable, "%p%", user);

        }

        // Don't bother with insufficient perms, it'd just spam chat

    }

    /**
     * The c4!about command. Usage: c4!about
     * Displays information about this bot's developer.
     *
     * @param channel The channel in which this command was sent.
     */
    private void about(String channel) {
        C4Messages.send(
                channel,
                "TwitchC4 was created by Justin H. Schaaf (justinschaaf.com). Licensed under MIT, view the source on GitHub."
        );
    }

    /**
     * The error message which is displayed when an invalid command is attempted
     *
     * @param channel The channel in which this command was sent
     * @param user The user which executed this command
     */
    private void unknown(String channel, String user) {
        C4Messages.send(channel, TwitchC4.getConfig().locErrorUnknownCommand, "%p%", user);
    }

    /*
     * UTILS
     */

    /**
     * Finds the given user as the sender or receiver of any challenge in the given channel
     *
     * @param channel The channel to find the given user in
     * @param user The user to find
     * @return The first {@link C4Challenge} which contains the user, or null if none is found
     */
    private C4Challenge findUserInChallenge(String channel, String user) {

        C4Challenge challenge = null;

        for (C4Challenge c : CHALLENGES.get(channel))
            if (c.getFrom().equalsIgnoreCase(user) || (c.getTo() != null && c.getTo().equalsIgnoreCase(user))) {
                challenge = c;
                break;
            }

        return challenge;

    }

    /**
     * Finds the given user in any game in the given channel
     *
     * @param channel The channel to find the given user in
     * @param user The user to find
     * @return The {@link C4Game} the user is currently playing in, or null if none is found
     */
    private C4Game findUserInGame(String channel, String user) {

        C4Game game = null;

        for (C4Game g : GAMES.get(channel))
            if (g.hasPlayer(user)) {
                game = g;
                break;
            }

        return game;

    }

    /**
     * Ensures the {@link #CHALLENGES}, {@link #GAMES}, and
     * {@link #GAMES_ENABLED} entries exist for this channel
     * @param channel The channel to make sure entries exist for
     */
    private void ensureDataExists(String channel) {
        if (!CHALLENGES.containsKey(channel)) CHALLENGES.put(channel, new LinkedList<>());
        if (!GAMES.containsKey(channel)) GAMES.put(channel, new LinkedList<>());
        if (!GAMES_ENABLED.containsKey(channel)) GAMES_ENABLED.put(channel, true);
    }

}
