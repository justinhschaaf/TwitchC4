package com.justinschaaf.twitchc4;

/**
 * Util class for formatting messages
 *
 * @author Justin H. Schaaf (justinschaaf.com)
 */
public class C4Messages {

    /**
     * Formats and sends a message in the given channel
     *
     * @param channel The channel to send the message in
     * @param message The message to send
     * @param replacements Any replacements to perform on the message. Should have an even
     *                     number of element pairs, with the first item being the token to
     *                     search for and the second item being what to replace it with.
     *
     *                     e.g. ["%f%", "justinhschaaf", "%t%", "TheOnlyCheezIt"] would
     *                     replace all instances of "%f%" in the message with
     *                     "justinhschaaf" and all instances of "%t%" with "TheOnlyCheezIt"
     */
    public static void send(String channel, String message, String... replacements) {

        if (message != null) {

            for (int i = 0; i < replacements.length - 1; i += 2)
                if (replacements[i + 1] != null)
                    message = message.replace(replacements[i], replacements[i + 1]);

            TwitchC4.getClient().getChat().sendMessage(channel, message);

        }

    }

}
