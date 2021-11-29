package com.justinschaaf.twitchc4;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Represents a game of Connect 4
 *
 * @author Justin H. Schaaf (justinschaaf.com)
 */
public class C4Game {

    private String channel;

    private String p1;
    private String p2;

    private String[][] board;

    private boolean isP1Turn = true;
    private ScheduledFuture<?> turn;

    /**
     * Creates a new Connect 4 game
     *
     * @param channel The Twitch channel name in which this game is taking place
     * @param p1 The name of Player 1
     * @param p2 The name of Player 2
     */
    public C4Game(String channel, String p1, String p2) {

        this.channel = channel;
        this.p1 = p1;
        this.p2 = p2;

        initBoard();
        startGame();

    }

    /**
     * Starts the game of Connect 4
     */
    private void startGame() {

        C4Messages.send(channel, TwitchC4.getConfig().locGameStart, "%p1%", p1, "%p2%", p2);

        // Randomize starting player
        if (TwitchC4.getConfig().gameRandomStart)
            if (Math.random() >= .5)
                isP1Turn = !isP1Turn;

        startTurn();

    }

    /**
     * Starts the next turn. The player whose turn it is is determined by {@link #isP1Turn}
     */
    public void startTurn() {

        printBoard();
        C4Messages.send(channel, TwitchC4.getConfig().locGameTurnStart, "%p%", getPlayerTurn());

        // For future reference: https://stackoverflow.com/questions/54623222/how-to-schedule-tasks-with-a-delay
        turn = Executors
                .newScheduledThreadPool(1)
                .schedule(
                        () -> forfeit(isP1Turn ? p1 : p2),
                        TwitchC4.getConfig().gameTimer,
                        TimeUnit.SECONDS
                );

    }

    /**
     * Performs a turn on behalf of the current player
     * @param c The column to play in, as long as c has passed {@link #isValidMove(int)}
     */
    public void doTurn(int c) {

        for (int i = board.length - 1; i >= 0; i--)
            if (board[i][c - 1].equals(TwitchC4.getConfig().symbolEmpty)) {

                board[i][c - 1] = getPlayerChip();
                break;

            }

        endTurn();

    }

    /**
     * Ends the current turn and checks to see if the game should end
     * Automatically called at the end of {@link #doTurn(int)}
     */
    public void endTurn() {

        String winner = getWinner();

        // Do we have a winner?
        if (winner != null) endGame(getPlayerFromChip(winner));
        else {

            boolean topFull = !board[0][0].equals(TwitchC4.getConfig().symbolEmpty);

            if (topFull)
                for (int i = 1; topFull && i < board[0].length; i++)
                    if (!board[0][i].equals(board[0][i - 1]))
                        topFull = false;

            if (topFull) endGame(null); // The game ends in a draw
            else {

                isP1Turn = !isP1Turn;
                turn.cancel(true);
                startTurn();

            }

        }

    }

    /**
     * Forfeits the game by the given player, ending it in a victory for their opponent
     * @param player The player who forfeited
     */
    public void forfeit(String player) {

        C4Messages.send(channel, TwitchC4.getConfig().locForfeit, "%p%", player);

        String winner = player.equals(p1) ? p2 : p1;
        endGame(winner);

    }

    /**
     * Ends the game in a victory for the given player
     * @param winner The player who won the game
     */
    public void endGame(String winner) {

        if (!turn.isDone()) turn.cancel(true);

        printBoard();

        if (winner == null) C4Messages.send(channel, TwitchC4.getConfig().locGameTie, "%p1%", p1, "%p2%", p2);
        else C4Messages.send(channel, TwitchC4.getConfig().locGameWin, "%p%", winner);

        TwitchC4.getCmds().GAMES.get(channel).remove(this);

    }

    /**
     * Checks whether or not a move in the given column is valid
     *
     * @param c The column to check for validity
     * @return true if c is a valid column starting indexing from 1 and if the column has an open spot
     */
    public boolean isValidMove(int c) {
        return c <= board[0].length && board[0][c - 1].equals(TwitchC4.getConfig().symbolEmpty);
    }

    /**
     * Prints the game board to this game's associated Twitch chat
     */
    public void printBoard() {

        for (String[] row : board) {

            StringBuilder msg = new StringBuilder();

            for (String e : row) msg.append(e);

            C4Messages.send(channel, msg.toString());

        }

    }

    /**
     * Populates the game board with the empty cell string
     */
    private void initBoard() {
        board = new String[TwitchC4.getConfig().gameBoardHeight][TwitchC4.getConfig().gameBoardWidth];
        for (String[] strings : board) Arrays.fill(strings, TwitchC4.getConfig().symbolEmpty);
    }

    /**
     * Gets the winner of the board, or null if neither player has won yet
     * @return The symbol corresponding to the winning player
     */
    public String getWinner() {

        String winner = null;

        for (int i = 0; winner == null && i < board.length; i++) {

            for (int j = 0; winner == null && j < board[i].length; j++) {

                // If it's the empty char, continue
                if (board[i][j].equals(TwitchC4.getConfig().symbolEmpty)) continue;

                // Horizontal Adjacent, e.g. -
                if (j + 4 <= board[i].length) {

                    boolean allEquals = true;

                    for (int n = j + 1; allEquals && n < j + 4; n++)
                        if (!board[i][n].equals(board[i][n - 1]))
                            allEquals = false;

                    if (allEquals) winner = board[i][j];

                }

                // Vertical Adjacent, e.g. |
                if (winner == null && i + 4 <= board.length) {

                    boolean allEquals = true;

                    for (int n = i + 1; allEquals && n < i + 4; n++)
                        if (!board[n][j].equals(board[n - 1][j]))
                            allEquals = false;

                    if (allEquals) winner = board[i][j];

                }

                // Upwards Diagonal, e.g. /
                if (winner == null && i - 4 >= 0 && j + 4 <= board[i].length) {

                    boolean allEquals = true;

                    for (int n = 1; allEquals && n < 4; n++)
                        if (!board[i - n][j + n].equals(board[i - n + 1][j + n - 1]))
                            allEquals = false;

                    if (allEquals) winner = board[i][j];

                }

                // Downwards Diagonal, e.g. \
                if (winner == null && i + 4 <= board.length && j + 4 <= board[i].length) {

                    boolean allEquals = true;

                    for (int n = 1; allEquals && n < 4; n++)
                        if (!board[i + n][j + n].equals(board[i + n - 1][j + n - 1]))
                            allEquals = false;

                    if (allEquals) winner = board[i][j];

                }

            }

        }

        return winner;

    }

    /**
     * Gets the player's name from the chip they play with
     *
     * @param chip The chip to retrieve the name from
     * @return The player associated with this chip
     */
    public String getPlayerFromChip(String chip) {
        if (chip.equals(TwitchC4.getConfig().symbolP1)) return p1;
        else return p2;
    }

    /**
     * Gets the chip for the current player
     * @return The chip that will be played this turn
     */
    public String getPlayerChip() {
        if (isP1Turn) return TwitchC4.getConfig().symbolP1;
        else return TwitchC4.getConfig().symbolP2;
    }

    /**
     * Gets the name of the current player
     * @return The name of the player whose turn it is
     */
    public String getPlayerTurn() {
        if (isP1Turn) return p1;
        else return p2;
    }

    /**
     * Determines whether or not the given user is in this game
     *
     * @param user The user to check for
     * @return true If the user is one of this game's players
     */
    public boolean hasPlayer(String user) {
        return p1.equalsIgnoreCase(user) || p2.equalsIgnoreCase(user);
    }

    /**
     * Gets a list of players in this game
     * @return A list of players in this game
     */
    public String[] getPlayers() {
        return new String[]{p1, p2};
    }

}
