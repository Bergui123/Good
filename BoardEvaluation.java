public class BoardEvaluation {
    
    // First 8x8 matrix filled with ones
    private static final int[][] RedPlaceValue = {
        {100, 100, 100, 100, 100, 100, 100, 100},
        {100, 100, 100, 100, 100, 100, 100, 100},
        {12, 12, 15, 15, 15, 15, 12, 12},
        {4, 4, 6, 6, 6, 6, 4, 4},
        {2, 2, 4, 4, 4, 4, 2, 2},
        {2, 2, 4, 4, 4, 4, 2, 2},
        {0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0}
    };
    
    // Second 8x8 matrix - 180 degree flipped version of RedPlaceValue
    private static final int[][] BlackPlaceValue = {
        {0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0},
        {2, 2, 4, 4, 4, 4, 2, 2},
        {2, 2, 4, 4, 4, 4, 2, 2},
        {4, 4, 6, 6, 6, 6, 4, 4},
        {12, 12, 15, 15, 15, 15, 12, 12},
        {100, 100, 100, 100, 100, 100, 100, 100},
        {100, 100, 100, 100, 100, 100, 100, 100}
    };
    
    // Piece values
    private static final int PUSHER_VALUE = 100;
    private static final int NORMAL_PIECE_VALUE = 50;
    
    // Bonus values
    private static final int CAPTURE_PUSHER_BONUS = 150;
    private static final int CAPTURE_NORMAL_BONUS = 75;
    private static final int CENTER_CONTROL_BONUS = 10;
    private static final int ADVANCEMENT_BONUS = 5;
    
    /**
     * Evaluates the board position for the given color
     * @param board 2D array representing the board state
     *              'R' = Red pusher, 'r' = Red normal piece
     *              'B' = Black pusher, 'b' = Black normal piece
     *              ' ' or null = Empty square
     * @param color "red" or "black" - the color to evaluate for
     * @return positive value indicating board evaluation score
     */
    public static int evaluate(char[][] board, String color) {
        if (board == null || board.length != 8 || board[0].length != 8) {
            throw new IllegalArgumentException("Board must be 8x8");
        }
        
        color = color.toLowerCase();
        if (!color.equals("red") && !color.equals("black")) {
            throw new IllegalArgumentException("Color must be 'red' or 'black'");
        }
        
        int score = 0;
        boolean isRed = color.equals("red");
        
        // Count pieces and calculate positional values
        int myPushers = 0, myNormal = 0;
        int enemyPushers = 0, enemyNormal = 0;
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board[row][col];
                
                if (piece == ' ' || piece == '\0') continue;
                
                // Determine if this piece belongs to us
                boolean isMyPiece = (isRed && (piece == 'R' || piece == 'r')) ||
                                   (!isRed && (piece == 'B' || piece == 'b'));
                
                // Determine if it's a pusher
                boolean isPusher = (piece == 'R' || piece == 'B');
                
                if (isMyPiece) {
                    // Our pieces - add positive value
                    if (isPusher) {
                        myPushers++;
                        score += PUSHER_VALUE;
                    } else {
                        myNormal++;
                        score += NORMAL_PIECE_VALUE;
                    }
                    
                    // Add positional bonus
                    score += getPositionalValue(row, col, isRed);
                    
                    // Add advancement bonus (closer to opponent's end)
                    score += getAdvancementBonus(row, isRed);
                    
                    // Add center control bonus
                    score += getCenterControlBonus(row, col);
                    
                } else {
                    // Enemy pieces - subtract value (but we'll make final result positive)
                    if (isPusher) {
                        enemyPushers++;
                        score -= PUSHER_VALUE;
                    } else {
                        enemyNormal++;
                        score -= NORMAL_PIECE_VALUE;
                    }
                    
                    // Subtract enemy positional advantages
                    score -= getPositionalValue(row, col, !isRed);
                }
            }
        }
        
        // Winning condition bonuses
        score += getWinningConditionBonus(myPushers, myNormal, enemyPushers, enemyNormal, board, isRed);
        
        // For empty board, return base value only
        if (myPushers == 0 && myNormal == 0 && enemyPushers == 0 && enemyNormal == 0) {
            return 1000;
        }
        
        // Ensure positive result by adding a base value
        score += 1000;
        
        return Math.max(1, score); // Ensure at least 1
    }
    
    /**
     * Gets positional value based on the position matrices
     */
    private static int getPositionalValue(int row, int col, boolean isRed) {
        if (isRed) {
            return RedPlaceValue[row][col];
        } else {
            return BlackPlaceValue[row][col];
        }
    }
    
    /**
     * Calculates advancement bonus - pieces closer to opponent's end get higher bonus
     */
    private static int getAdvancementBonus(int row, boolean isRed) {
        if (isRed) {
            // Red advances towards row 0 (top of board, black's territory)
            return row * ADVANCEMENT_BONUS;
        } else {
            // Black advances towards row 7 (bottom of board, red's territory)
            return (7 - row) * ADVANCEMENT_BONUS;
        }
    }
    
    /**
     * Calculates center control bonus for pieces in the middle columns
     */
    private static int getCenterControlBonus(int row, int col) {
        // Columns 3, 4 (D, E) get full bonus
        // Columns 2, 5 (C, F) get half bonus
        if (col == 3 || col == 4) {
            return CENTER_CONTROL_BONUS;
        } else if (col == 2 || col == 5) {
            return CENTER_CONTROL_BONUS / 2;
        }
        return 0;
    }
    
    /**
     * Calculates bonuses related to winning conditions
     */
    private static int getWinningConditionBonus(int myPushers, int myNormal, 
                                               int enemyPushers, int enemyNormal, 
                                               char[][] board, boolean isRed) {
        int bonus = 0;
        
        // Huge bonus if we can win by reaching the end
        bonus += checkEndZoneWin(board, isRed);
        
        // Large bonus if enemy has no pushers (we win)
        if (enemyPushers == 0 && enemyNormal > 0) {
            bonus += 10000;
        }
        
        // Bonus for having more pushers than enemy
        int pusherAdvantage = myPushers - enemyPushers;
        bonus += pusherAdvantage * CAPTURE_PUSHER_BONUS;
        
        // Bonus for having more total pieces
        int totalAdvantage = (myPushers + myNormal) - (enemyPushers + enemyNormal);
        bonus += totalAdvantage * CAPTURE_NORMAL_BONUS;
        
        return bonus;
    }
    
    /**
     * Checks if we have a piece in the opponent's end zone (winning condition)
     */
    private static int checkEndZoneWin(char[][] board, boolean isRed) {
        int targetRow = isRed ? 0 : 7; // Red targets row 0 (top), Black targets row 7 (bottom)
        
        for (int col = 0; col < 8; col++) {
            char piece = board[targetRow][col];
            
            // Check if we have a piece in the target row
            if ((isRed && (piece == 'R' || piece == 'r')) ||
                (!isRed && (piece == 'B' || piece == 'b'))) {
                return 50000; // Huge bonus for winning
            }
        }
        
        return 0;
    }
}
