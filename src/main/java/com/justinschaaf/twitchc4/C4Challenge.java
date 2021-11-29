package com.justinschaaf.twitchc4;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Represents a challenge to play Connect 4
 *
 * @author Justin H. Schaaf (justinschaaf.com)
 */
public class C4Challenge {

    private String channel;
    private String from;
    private String to;

    private boolean accepted = false;
    private ScheduledFuture<?> expiration;

    /**
     * Represents a challenge to anyone to play Connect 4
     *
     * @param channel The channel in which this challenge was sent
     * @param from The user who sent the challenge
     */
    public C4Challenge(String channel, String from) {
        this(channel, from, null);
    }

    /**
     * Represents a challenge to a specific user to play Connect 4
     *
     * @param channel The channel in which this challenge was sent
     * @param from The user who sent the challenge
     * @param to The user this challenge was directed towards
     */
    public C4Challenge(String channel, String from, String to) {

        this.channel = channel;
        this.from = from;
        this.to = to;

        expiration = Executors
                .newScheduledThreadPool(1)
                .schedule(
                        this::expire,
                        TwitchC4.getConfig().gameTimer,
                        TimeUnit.SECONDS
                );

    }

    /**
     * Accepts the request to play Connect 4
     * @return The newly-created game
     */
    public C4Game accept() {

        accepted = true;
        cancel();

        return new C4Game(channel, from, to);

    }

    /**
     * Called when this challenge expires
     */
    public void expire() {
        C4Messages.send(channel, TwitchC4.getConfig().locChallengeExpire, "%f%", from);
        cancel();
    }

    /**
     * Cancels this challenge
     */
    public void cancel() {
        if (!expiration.isDone()) expiration.cancel(true);
        TwitchC4.getCmds().CHALLENGES.get(channel).remove(this);
    }

    /**
     * Gets the channel this challenge was sent in
     * @return The channel this challenge was sent in
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Gets the user that sent this challenge
     * @return The name of the user which sent this challenge
     */
    public String getFrom() {
        return from;
    }

    /**
     * Gets the user that this challenge was sent to
     * @return The name of the user this challenge was sent to, or null if this was a general challenge
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets the user this challenge was sent to. Should be used for when another user accepts the random challenge
     * @param to The user this challenge was sent to
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Whether or not this challenge has been accepted. There's probably no use for this, but whatever
     * @return true If this challenge has been accepted
     */
    public boolean isAccepted() {
        return accepted;
    }

}
