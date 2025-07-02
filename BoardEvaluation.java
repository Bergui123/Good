public class BoardEvaluation {
    
    // Heavily favor advancement towards the goal with exponential bonuses
    private static final int[][] RedPlaceValue = {
        {100000, 100000, 100000, 100000, 100000, 100000, 100000, 100000}, // Goal line - MASSIVE bonus
        {50000, 50000, 60000, 60000, 60000, 60000, 50000, 50000},          // Almost there - huge bonus
        {10000, 10000, 15000, 15000, 15000, 15000, 10000, 10000},          // Deep in enemy territory
        {2000, 2000, 3000, 3000, 3000, 3000, 2000, 2000},                 // Mid-board advancing
        {500, 500, 800, 800, 800, 800, 500, 500},                         // Crossing center
        {100, 100, 200, 200, 200, 200, 100, 100},                         // Still in own half
        {20, 20, 40, 40, 40, 40, 20, 20},                                  // Near starting position
        {0, 0, 0, 0, 0, 0, 0, 0}                                           // Starting line
    };
    
    // Mirror for black pieces (they advance towards row 7)
    private static final int[][] BlackPlaceValue = {
        {0, 0, 0, 0, 0, 0, 0, 0},                                           // Starting line
        {20, 20, 40, 40, 40, 40, 20, 20},                                  // Near starting position
        {100, 100, 200, 200, 200, 200, 100, 100},                         // Still in own half
        {500, 500, 800, 800, 800, 800, 500, 500},                         // Crossing center
        {2000, 2000, 3000, 3000, 3000, 3000, 2000, 2000},                 // Mid-board advancing
        {10000, 10000, 15000, 15000, 15000, 15000, 10000, 10000},          // Deep in enemy territory
        {50000, 50000, 60000, 60000, 60000, 60000, 50000, 50000},          // Almost there - huge bonus
        {100000, 100000, 100000, 100000, 100000, 100000, 100000, 100000}  // Goal line - MASSIVE bonus
    };
    
    // Piece values - Pushers are significantly more valuable
    private static final int PUSHER_VALUE = 200;       // Higher base value for pushers
    private static final int NORMAL_PIECE_VALUE = 80;  // Lower value for pushed pieces
    
    // Bonus values - Heavy emphasis on advancement and pusher mobility
    private static final int CAPTURE_PUSHER_BONUS = 5000;    // MASSIVE bonus for capturing pushers
    private static final int CAPTURE_NORMAL_BONUS = 2000;    // Large bonus for capturing pushed pieces
    private static final int CENTER_CONTROL_BONUS = 25;      // Increased center control bonus
    private static final int ADVANCEMENT_BONUS = 100;   // Much higher advancement bonus
    private static final int PUSHER_MOBILITY_BONUS = 40; // Bonus for pusher moves
    private static final int NEAR_GOAL_BONUS = 10000;    // MASSIVE bonus for being very close to goal
    private static final int PUSHER_BEHIND_PUSHED_BONUS = 1000; // Big bonus for pusher-pushed formations
    
    // NEW: Piece preservation and positioning penalties/bonuses
    private static final int PIECE_EXPOSURE_PENALTY = 3000;    // Much higher penalty for pieces exposed to capture
    private static final int EDGE_COLUMN_PENALTY = 200;       // Penalty for pieces on edge columns A/H
    private static final int CENTER_COLUMN_BONUS = 150;       // Extra bonus for center columns C-F
    private static final int SAFE_POSITION_BONUS = 1000;      // Bonus for pieces in safe positions
    
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
                    
                    // NEW: Add center column bonus and edge column penalty
                    score += getCenterColumnBonus(row, col);
                    
                } else {
                    // Enemy pieces - subtract value AND calculate capture bonuses
                    if (isPusher) {
                        enemyPushers++;
                        score -= PUSHER_VALUE;
                        
                        // CRITICAL: Heavy penalty for enemy pushers close to our goal
                        int enemyAdvancement = getEnemyAdvancementThreat(row, !isRed);
                        score -= enemyAdvancement * 1000; // Massive penalty for advanced enemies
                        
                    } else {
                        enemyNormal++;
                        score -= NORMAL_PIECE_VALUE;
                        
                        // Penalty for enemy pushed pieces close to our goal
                        int enemyAdvancement = getEnemyAdvancementThreat(row, !isRed);
                        score -= enemyAdvancement * 500;
                    }
                    
                    // Subtract enemy positional advantages
                    score -= getPositionalValue(row, col, !isRed);
                }
            }
        }
        
        // Winning condition bonuses
        score += getWinningConditionBonus(myPushers, myNormal, enemyPushers, enemyNormal, board, isRed);
        
        // CRITICAL: Bonus for good pusher-pushed formations
        score += getPusherBehindPushedBonus(board, isRed);
        
        // NEW: Apply piece preservation penalties - check for exposed pieces
        score -= getPieceExposurePenalty(board, isRed);
        
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
     * Calculates how threatening an enemy piece is based on its advancement toward our goal
     */
    private static int getEnemyAdvancementThreat(int row, boolean enemyIsRed) {
        if (enemyIsRed) {
            // Red enemy advances towards row 0 - more threatening the lower the row
            return (7 - row); // 0-7, higher value = more threatening
        } else {
            // Black enemy advances towards row 7 - more threatening the higher the row  
            return row; // 0-7, higher value = more threatening
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
                return 1000000; // MASSIVE bonus for winning - highest possible priority
            }
        }
        
        return 0;
    }
    
    /**
     * Calculates bonus for having pushers positioned behind pushed pieces
     * This formation allows for forward advancement
     */
    private static int getPusherBehindPushedBonus(char[][] board, boolean isRed) {
        int bonus = 0;
        char myPusher = isRed ? 'R' : 'B';
        char myPushed = isRed ? 'r' : 'b';
        int direction = isRed ? -1 : 1; // Direction pushers need to be relative to pushed pieces
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board[row][col];
                
                // Find our pushed pieces
                if (piece == myPushed) {
                    // Check if there's a pusher behind this pushed piece
                    int pusherRow = row - direction; // Behind the pushed piece
                    
                    // Check straight behind
                    if (pusherRow >= 0 && pusherRow < 8 && board[pusherRow][col] == myPusher) {
                        bonus += PUSHER_BEHIND_PUSHED_BONUS;
                        
                        // Extra bonus if this formation is advancing
                        int advancementLevel = getAdvancementLevel(row, isRed);
                        bonus += advancementLevel * 200; // More bonus for advanced formations
                    }
                    
                    // Check diagonal behind positions too
                    if (pusherRow >= 0 && pusherRow < 8) {
                        if (col > 0 && board[pusherRow][col - 1] == myPusher) {
                            bonus += PUSHER_BEHIND_PUSHED_BONUS / 2; // Half bonus for diagonal support
                        }
                        if (col < 7 && board[pusherRow][col + 1] == myPusher) {
                            bonus += PUSHER_BEHIND_PUSHED_BONUS / 2; // Half bonus for diagonal support
                        }
                    }
                }
            }
        }
        
        return bonus;
    }
    
    /**
     * Gets advancement level (0-7) for bonus calculations
     */
    private static int getAdvancementLevel(int row, boolean isRed) {
        if (isRed) {
            return 7 - row; // Red: row 0 = level 7, row 7 = level 0
        } else {
            return row; // Black: row 7 = level 7, row 0 = level 0
        }
    }
    
    /**
     * NEW: Calculates center column bonus and edge column penalty
     * Strongly encourage center play and discourage edge play
     */
    private static int getCenterColumnBonus(int row, int col) {
        // Penalty for edge columns A (0) and H (7)
        if (col == 0 || col == 7) {
            return -EDGE_COLUMN_PENALTY;
        }
        
        // Extra bonus for center columns C-F (2-5)
        if (col >= 2 && col <= 5) {
            return CENTER_COLUMN_BONUS;
        }
        
        // Columns B and G get no bonus/penalty
        return 0;
    }
    
    /**
     * NEW: Calculates penalty for pieces that are exposed to enemy capture
     * This helps preserve our pieces by avoiding dangerous positions
     */
    private static int getPieceExposurePenalty(char[][] board, boolean isRed) {
        int penalty = 0;
        char myPusher = isRed ? 'R' : 'B';
        char myPushed = isRed ? 'r' : 'b';
        
        // Check each of our pieces to see if they can be captured
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board[row][col];
                
                // Check if this is one of our pieces
                if (piece == myPusher || piece == myPushed) {
                    // Check if this piece can be captured by enemy pieces
                    if (canBeCapturedByEnemy(board, row, col, isRed)) {
                        // Higher penalty for pushers being exposed
                        if (piece == myPusher) {
                            penalty += PIECE_EXPOSURE_PENALTY * 2; // Double penalty for exposed pushers
                        } else {
                            penalty += PIECE_EXPOSURE_PENALTY;
                        }
                        
                        // Extra penalty if the piece is advanced (losing advanced pieces is worse)
                        int advancementLevel = getAdvancementLevel(row, isRed);
                        if (advancementLevel >= 4) { // Advanced pieces
                            penalty += PIECE_EXPOSURE_PENALTY / 2;
                        }
                    }
                }
            }
        }
        
        return penalty;
    }
    
    /**
     * NEW: Checks if a piece at the given position can be captured by enemy pieces
     */
    private static boolean canBeCapturedByEnemy(char[][] board, int row, int col, boolean isRed) {
        char enemyPusher = isRed ? 'B' : 'R';
        
        // Check all possible enemy piece positions that could capture this piece
        // Pushers can move in any direction, pushed pieces can only be pushed
        
        // Check for enemy pushers that can move to capture this piece
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] == enemyPusher) {
                    // Check if this enemy pusher can move to capture our piece
                    if (canPusherMoveTo(r, c, row, col)) {
                        return true;
                    }
                    
                    // Check if this enemy pusher can push another piece to capture ours
                    if (canPusherPushTo(board, r, c, row, col, !isRed)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * NEW: Checks if a pusher can move directly to the target position
     */
    private static boolean canPusherMoveTo(int fromRow, int fromCol, int toRow, int toCol) {
        // Pushers can move one square in any direction (including diagonally)
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        
        return (rowDiff <= 1 && colDiff <= 1) && !(rowDiff == 0 && colDiff == 0);
    }
    
    /**
     * NEW: Checks if a pusher can push another piece to the target position
     */
    private static boolean canPusherPushTo(char[][] board, int pusherRow, int pusherCol, int targetRow, int targetCol, boolean enemyIsRed) {
        char enemyPushed = enemyIsRed ? 'r' : 'b';
        
        // Check all 8 directions from the pusher
        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};
        
        for (int dir = 0; dir < 8; dir++) {
            int pushedRow = pusherRow + dr[dir];
            int pushedCol = pusherCol + dc[dir];
            
            // Check if there's a pushed piece here
            if (pushedRow >= 0 && pushedRow < 8 && pushedCol >= 0 && pushedCol < 8) {
                if (board[pushedRow][pushedCol] == enemyPushed) {
                    // Check if pushing this piece would land it on the target
                    int newRow = pushedRow + dr[dir];
                    int newCol = pushedCol + dc[dir];
                    
                    if (newRow == targetRow && newCol == targetCol) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
}
