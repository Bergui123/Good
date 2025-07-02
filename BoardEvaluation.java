public class BoardEvaluation {
    
    // Heavily favor advancement towards the goal with exponential bonuses
    private static final int[][] RedPlaceValue = {
        {1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000}, // Goal line - huge bonus
        {500, 500, 600, 600, 600, 600, 500, 500},          // Almost there
        {200, 200, 300, 300, 300, 300, 200, 200},          // Deep in enemy territory
        {80, 80, 120, 120, 120, 120, 80, 80},              // Mid-board advancing
        {30, 30, 50, 50, 50, 50, 30, 30},                  // Crossing center
        {10, 10, 20, 20, 20, 20, 10, 10},                  // Still in own half
        {5, 5, 10, 10, 10, 10, 5, 5},                      // Near starting position
        {0, 0, 0, 0, 0, 0, 0, 0}                           // Starting line
    };
    
    // Mirror for black pieces (they advance towards row 7)
    private static final int[][] BlackPlaceValue = {
        {0, 0, 0, 0, 0, 0, 0, 0},                           // Starting line
        {5, 5, 10, 10, 10, 10, 5, 5},                      // Near starting position
        {10, 10, 20, 20, 20, 20, 10, 10},                  // Still in own half
        {30, 30, 50, 50, 50, 50, 30, 30},                  // Crossing center
        {80, 80, 120, 120, 120, 120, 80, 80},              // Mid-board advancing
        {200, 200, 300, 300, 300, 300, 200, 200},          // Deep in enemy territory
        {500, 500, 600, 600, 600, 600, 500, 500},          // Almost there
        {1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000}  // Goal line - huge bonus
    };
    
    // Piece values - Pushers are significantly more valuable
    private static final int PUSHER_VALUE = 200;       // Higher base value for pushers
    private static final int NORMAL_PIECE_VALUE = 80;  // Lower value for pushed pieces
    
    // Bonus values - Heavy emphasis on advancement and pusher mobility
    private static final int CAPTURE_PUSHER_BONUS = 300;
    private static final int CAPTURE_NORMAL_BONUS = 120;
    private static final int CENTER_CONTROL_BONUS = 15;
    private static final int ADVANCEMENT_BONUS = 25;   // Much higher advancement bonus
    private static final int PUSHER_MOBILITY_BONUS = 40; // Bonus for pusher moves
    private static final int NEAR_GOAL_BONUS = 100;    // Extra bonus for being very close to goal
    
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
                        // Extra bonus for pushers being mobile and advancing
                        score += PUSHER_MOBILITY_BONUS;
                        
                        // CRITICAL: Pushers get MUCH higher positional and advancement bonuses
                        // since they are the key to winning and can move independently
                        int positionalValue = getPositionalValue(row, col, isRed);
                        score += positionalValue * 3; // Triple positional bonus for pushers
                        
                        // Heavy advancement bonus for pushers
                        score += getAdvancementBonus(row, isRed, isPusher) * 2;
                        
                        // Near goal bonus for pushers - they're the key to winning
                        if ((isRed && row <= 1) || (!isRed && row >= 6)) {
                            score += NEAR_GOAL_BONUS * 3; // Triple bonus for pushers near goal
                        }
                        
                    } else {
                        myNormal++;
                        score += NORMAL_PIECE_VALUE;
                        
                        // Pushed pieces get much smaller positional bonuses
                        // since they depend on pushers to move
                        int positionalValue = getPositionalValue(row, col, isRed);
                        score += positionalValue / 2; // Half positional bonus for pushed pieces
                        
                        // Small advancement bonus for pushed pieces
                        score += getAdvancementBonus(row, isRed, isPusher) / 2;
                        
                        // Small near goal bonus for pushed pieces
                        if ((isRed && row <= 1) || (!isRed && row >= 6)) {
                            score += NEAR_GOAL_BONUS / 2;
                        }
                    }
                    
                    // Add center control bonus (same for both types)
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
     * Pushers get extra bonus for advancing
     */
    private static int getAdvancementBonus(int row, boolean isRed, boolean isPusher) {
        int baseBonus;
        if (isRed) {
            // Red advances towards row 0 (top of board, black's territory)
            baseBonus = (7 - row) * ADVANCEMENT_BONUS;
        } else {
            // Black advances towards row 7 (bottom of board, red's territory)
            baseBonus = row * ADVANCEMENT_BONUS;
        }
        
        // Pushers get double advancement bonus to encourage their movement
        return isPusher ? baseBonus * 2 : baseBonus;
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
        
        // Additional bonus for pushers in advanced positions that can push pieces forward
        bonus += getPusherAdvancementBonus(board, isRed);
        
        return bonus;
    }
    
    /**
     * Special bonus for pushers that are in good positions to advance and push pieces
     */
    private static int getPusherAdvancementBonus(char[][] board, boolean isRed) {
        int bonus = 0;
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board[row][col];
                
                // Check if this is our pusher
                boolean isMyPusher = (isRed && piece == 'R') || (!isRed && piece == 'B');
                
                if (isMyPusher) {
                    // Bonus for pushers in advanced positions
                    if (isRed && row <= 3) { // Red pusher in upper half
                        bonus += 50 * (4 - row); // More bonus for being closer to goal
                    } else if (!isRed && row >= 4) { // Black pusher in lower half
                        bonus += 50 * (row - 3); // More bonus for being closer to goal
                    }
                    
                    // Check if this pusher can push a piece forward
                    int direction = isRed ? -1 : 1;
                    int frontRow = row + direction;
                    int behindRow = row - direction;
                    
                    // Check if there's a pushed piece behind that can be pushed
                    if (behindRow >= 0 && behindRow < 8) {
                        char behindPiece = board[behindRow][col];
                        boolean isPushedPieceBehind = (isRed && behindPiece == 'r') || (!isRed && behindPiece == 'b');
                        
                        if (isPushedPieceBehind && frontRow >= 0 && frontRow < 8) {
                            char frontPiece = board[frontRow][col];
                            if (frontPiece == ' ' || frontPiece == '\0') {
                                // Can push piece forward - big bonus!
                                bonus += 100;
                            }
                        }
                    }
                }
            }
        }
        
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
