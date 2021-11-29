package com.justinschaaf.twitchc4;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import com.electronwill.nightconfig.core.conversion.SpecIntInRange;
import com.electronwill.nightconfig.core.conversion.SpecNotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents this program's configuration
 *
 * @author Justin H. Schaaf (justinschaaf.com)
 */
@PreserveNotNull
public class C4Config {

    @Path("oauth")
    @SpecNotNull
    String oauth;

    @Path("id")
    @SpecNotNull
    String id;

    @Path("secret")
    @SpecNotNull
    String secret;

    @Path("channels")
    List<String> channels = new ArrayList<>();

    /*
     * COMMANDS
     */

    @Path("commands.prefix")
    String cmdPrefix = "c4!";

    @Path("commands.author-attribution")
    boolean cmdAttribution = true;

    @Path("commands.mod-toggle")
    boolean cmdModToggle = true;

    /*
     * GAME
     */

    @Path("game.timer")
    @SpecIntInRange(min = 15, max = Integer.MAX_VALUE)
    int gameTimer = 180;

    @Path("game.symbols.empty")
    String symbolEmpty = "⚫";

    @Path("game.symbols.p1")
    String symbolP1 = "🟠";

    @Path("game.symbols.p2")
    String symbolP2 = "🔵";

    @Path("game.while-online")
    boolean gameWhileOnline = false;

    @Path("game.while-offline")
    boolean gameWhileOffline = true;

    @Path("game.board-width")
    @SpecIntInRange(min = 1, max = 500)
    int gameBoardWidth = 7;

    @Path("game.board-height")
    @SpecIntInRange(min = 1, max = 500)
    int gameBoardHeight = 6;

    @Path("game.concurrent")
    boolean gameConcurrent = true;

    @Path("game.random-start")
    boolean gameRandomStart = true;

    /*
     * LOCALISATION
     */

    @Path("localisation.help")
    String locHelp = "Use c4!play [user] to challenge a user to a game, or don't specify a user to challenge anyone!\n" +
            "Use c4!put [1-7] to make a move in a game.\n" +
            "Use c4!forfeit to forfeit a game, to cancel a challenge, or to decline a challenge.";

    @Path("localisation.game-start")
    String locGameStart = "The game between %p1% and %p2% has begun!";

    @Path("localisation.game-turn-start")
    String locGameTurnStart = "%p%'s turn has begun! Use c4!put [1-7] to make a move.";

    @Path("localisation.game-tie")
    String locGameTie = "No player has won the game, sorry!";

    @Path("localisation.game-win")
    String locGameWin = "%p% has won the game! GG!";

    @Path("localisation.challenge-send")
    String locChallengeSend = "%f% has challenged any user to a game! Use c4!play or c4!play %f% to play!";

    @Path("localisation.challenge-send-to")
    String locChallengeSendTo = "%f% has challenged %t% to a game! Use c4!play %f% to accept!";

    @Path("localisation.challenge-expire")
    String locChallengeExpire = "The Connect 4 challenge from %f% has expired.";

    @Path("localisation.forfeit")
    String locForfeit = "%p% has forfeited the game.";

    @Path("localisation.forfeit-sent-challenge")
    String locForfeitSentChallenge = "Successfully cancelled your challenge.";

    @Path("localisation.forfeit-decline-challenge")
    String locForfeitDeclineChallenge = "Sorry, %f%, but %t% declined your challenge.";

    @Path("localisation.toggle-enable")
    String locToggleEnable = "New Connect 4 games enabled!";

    @Path("localisation.toggle-disable")
    String locToggleDisable = "New Connect 4 games disabled!";

    @Path("localisation.shutdown-cancel-challenge")
    String locShutdownCancelChallenge = "Sorry, %f%, but your challenge was cancelled because the bot shut down.";

    @Path("localisation.shutdown-cancel-game")
    String locShutdownCancelGame = "Sorry, %p1% and %p2%, but your game was cut short because the bot shut down.";

    @Path("localisation.error-generic")
    String locErrorGeneric = "Uh oh! Something went wrong.";

    @Path("localisation.error-unknown-command")
    String locErrorUnknownCommand = "Unknown command. Use c4!help for how to use the bot.";

    @Path("localisation.error-already-ingame")
    String locErrorAlreadyIngame = "You can't be in two games at once, %p%!";

    @Path("localisation.error-other-ingame")
    String locErrorOtherIngame = "Sorry, %f%, but %t% is already in a game!";

    @Path("localisation.error-not-ingame")
    String locErrorNotIngame = "You are currently not in a game!";

    @Path("localisation.error-not-turn")
    String locErrorNotTurn = "Please wait for your turn!";

    @Path("localisation.error-no-column")
    String locErrorNoColumn = "Please specify a column from 1 to 7.";

    @Path("localisation.error-invalid-column")
    String locErrorInvalidColumn = "You can't play there! Please try again.";

    @Path("localisation.error-already-challenging")
    String locErrorAlreadyChallenging = "You already have an outgoing challenge!";

    @Path("localisation.error-already-ongoing")
    String locErrorAlreadyOngoing = "Only one game can be played at a time!";

}
