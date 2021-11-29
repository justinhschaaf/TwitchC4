package com.justinschaaf.twitchc4;

/**
 * Tasks to perform whenever the bot shuts down
 *
 * @author Justin H. Schaaf (justinschaaf.com)
 */
public class C4Shutdown extends Thread {

    /**
     * Cancels all currently pending challenges and all ongoing games
     */
    @Override
    public void run() {

        // Cancel any current challenges
        for (String u : TwitchC4.getCmds().CHALLENGES.keySet())
            for (C4Challenge c : TwitchC4.getCmds().CHALLENGES.get(u)) {

                C4Messages.send(u, TwitchC4.getConfig().locShutdownCancelChallenge, "%f%", c.getFrom());
                c.cancel();

            }

        // Cancel any current games
        for (String u : TwitchC4.getCmds().GAMES.keySet())
            for (C4Game g : TwitchC4.getCmds().GAMES.get(u)) {

                String[] p = g.getPlayers();
                C4Messages.send(u, TwitchC4.getConfig().locShutdownCancelGame, "%p1%", p[0], "%p2%", p[1]);
                g.endGame(null);

            }

    }

}
