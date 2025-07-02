import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class MiniMax {
    private static final int MAX_DEPTH = 3; // Reduced to 3 for faster response
    private static final long TIME_LIMIT = 1000; // 1 second for much faster response
    private static final int POSITIVE_INFINITY = 1000000;
    private static final int NEGATIVE_INFINITY = -1000000;
    
    private long startTime;
    private boolean timeUp;
    private Random random;
    
    /**
     * Find the best move using minimax with alpha-beta pruning and time limit
     * @param board The current board state
     * @param color The color to move ("red" or "black")
     * @return The best move in string format (e.g., "A7A6")
     */
    public String findBestMove(Board board, String color) {
        startTime = System.currentTimeMillis();
        timeUp = false;
        random = new Random();
        
        String bestMove = null;
        
        // Get all possible moves for the current player
        String[] possibleMoves = MoveGenerator.move(color, board);
        
        if (possibleMoves.length == 0) {
            return null; // No moves available
        }
        
        // If only one move, return it immediately
        if (possibleMoves.length == 1) {
            return possibleMoves[0];
        }
        
        // Order moves to prioritize pusher moves and advancement
        possibleMoves = orderMoves(possibleMoves, board, color);
        
        // IMMEDIATE SAFETY CHECK: Return safe captures instantly for maximum efficiency
        for (String moveStr : possibleMoves) {
            Board.Move move = board.parseMove(moveStr);
            if (move != null) {
                int targetPiece = board.getPiece(move.toRow, move.toCol);
                
                // Check for captures first
                if (targetPiece != Board.EMPTY) {
                    // Quick safety check - if capture is safe, return immediately
                    if (!willBeExposedToCapture(board, move, color)) {
                        return moveStr; // Return immediately for efficiency!
                    }
                }
                
                // Check for winning moves
                Board tempBoard = copyBoard(board);
                if (tempBoard.makeMove(move) && tempBoard.hasWinner()) {
                    String winner = tempBoard.getWinner();
                    if ((color.equalsIgnoreCase("red") && "Red".equals(winner)) ||
                        (color.equalsIgnoreCase("black") && "Black".equals(winner))) {
                        return moveStr; // Return winning move immediately!
                    }
                }
            }
        }
        
        // Simplified iterative deepening - start with depth 2 for speed
        for (int depth = 2; depth <= MAX_DEPTH && !timeUp; depth++) {
            String currentBestMove = null;
            int currentBestScore = NEGATIVE_INFINITY;
            
            for (String moveStr : possibleMoves) {
                if (timeUp) break;
                
                // Make a copy of the board and apply the move
                Board tempBoard = copyBoard(board);
                Board.Move move = tempBoard.parseMove(moveStr);
                
                if (move != null && tempBoard.makeMove(move)) {
                    // Get the opponent's color
                    String opponentColor = color.equalsIgnoreCase("red") ? "black" : "red";
                    
                    // Evaluate this move using minimax
                    int score = minimax(tempBoard, depth - 1, NEGATIVE_INFINITY, POSITIVE_INFINITY, 
                                      false, opponentColor, color);
                    
                    // Add small random factor to break ties and avoid repetition
                    score += random.nextInt(3) - 1; // -1, 0, or 1
                    
                    if (score > currentBestScore) {
                        currentBestScore = score;
                        currentBestMove = moveStr;
                    }
                }
            }
            
            // If we completed this depth without timing out, update best move
            if (!timeUp && currentBestMove != null) {
                bestMove = currentBestMove;
            }
        }
        
        return bestMove != null ? bestMove : possibleMoves[0];
    }
    
    /**
     * Minimax algorithm with alpha-beta pruning
     * @param board Current board state
     * @param depth Remaining search depth
     * @param alpha Alpha value for pruning
     * @param beta Beta value for pruning
     * @param isMaximizing True if maximizing player, false if minimizing
     * @param currentColor Color of current player to move
     * @param originalColor Color of the original player (for evaluation)
     * @return The evaluation score
     */
    private int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizing, 
                       String currentColor, String originalColor) {
        
        // Check time limit
        if (System.currentTimeMillis() - startTime > TIME_LIMIT) {
            timeUp = true;
            return 0;
        }
        
        // Base case: depth 0 or game over
        if (depth == 0 || board.isGameOver() || timeUp) {
            return evaluatePosition(board, originalColor);
        }
        
        // Get all possible moves for current player
        String[] possibleMoves = MoveGenerator.move(currentColor, board);
        
        if (possibleMoves.length == 0) {
            // No moves available - evaluate current position
            return evaluatePosition(board, originalColor);
        }
        
        // Order moves for better alpha-beta pruning
        possibleMoves = orderMoves(possibleMoves, board, currentColor);
        
        if (isMaximizing) {
            int maxEval = NEGATIVE_INFINITY;
            
            for (String moveStr : possibleMoves) {
                if (timeUp) break;
                
                // Make a copy of the board and apply the move
                Board tempBoard = copyBoard(board);
                Board.Move move = tempBoard.parseMove(moveStr);
                
                if (move != null && tempBoard.makeMove(move)) {
                    String nextColor = currentColor.equalsIgnoreCase("red") ? "black" : "red";
                    int eval = minimax(tempBoard, depth - 1, alpha, beta, false, nextColor, originalColor);
                    
                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval);
                    
                    // Alpha-beta pruning
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return maxEval;
            
        } else {
            int minEval = POSITIVE_INFINITY;
            
            for (String moveStr : possibleMoves) {
                if (timeUp) break;
                
                // Make a copy of the board and apply the move
                Board tempBoard = copyBoard(board);
                Board.Move move = tempBoard.parseMove(moveStr);
                
                if (move != null && tempBoard.makeMove(move)) {
                    String nextColor = currentColor.equalsIgnoreCase("red") ? "black" : "red";
                    int eval = minimax(tempBoard, depth - 1, alpha, beta, true, nextColor, originalColor);
                    
                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval);
                    
                    // Alpha-beta pruning
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return minEval;
        }
    }
    
    /**
     * Evaluate the current board position using BoardEvaluation
     * @param board The board to evaluate
     * @param color The color to evaluate for
     * @return The evaluation score
     */
    private int evaluatePosition(Board board, String color) {
        // Convert Board to char[][] format expected by BoardEvaluation
        char[][] charBoard = new char[8][8];
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = board.getPiece(row, col);
                switch (piece) {
                    case Board.EMPTY:
                        charBoard[row][col] = ' ';
                        break;
                    case Board.BLACK_PUSHED:
                        charBoard[row][col] = 'b';
                        break;
                    case Board.BLACK_PUSHER:
                        charBoard[row][col] = 'B';
                        break;
                    case Board.RED_PUSHED:
                        charBoard[row][col] = 'r';
                        break;
                    case Board.RED_PUSHER:
                        charBoard[row][col] = 'R';
                        break;
                    default:
                        charBoard[row][col] = ' ';
                        break;
                }
            }
        }
        
        return BoardEvaluation.evaluate(charBoard, color);
    }
    
    /**
     * Create a deep copy of the board
     * @param original The original board
     * @return A copy of the board
     */
    private Board copyBoard(Board original) {
        Board copy = new Board();
        
        // Copy the board configuration
        String config = original.getBoardConfiguration();
        copy.parseBoardFromServer(config);
        copy.setRedPlayer(original.isRedPlayer());
        
        return copy;
    }
    
    /**
     * Optimized move ordering for maximum pruning efficiency
     */
    private String[] orderMoves(String[] moves, Board board, String color) {
        // Quick pre-filtering for immediate returns
        List<String> safeCaptures = new ArrayList<>();
        List<String> safeMoves = new ArrayList<>();
        List<String> riskyMoves = new ArrayList<>();
        
        for (String moveStr : moves) {
            Board.Move move = board.parseMove(moveStr);
            if (move == null) continue;
            
            int targetPiece = board.getPiece(move.toRow, move.toCol);
            boolean isCapture = (targetPiece != Board.EMPTY);
            boolean isSafe = !willBeExposedToCapture(board, move, color);
            
            if (isCapture && isSafe) {
                safeCaptures.add(moveStr); // Highest priority
            } else if (isSafe) {
                safeMoves.add(moveStr); // Medium priority
            } else {
                riskyMoves.add(moveStr); // Lowest priority
            }
        }
        
        // Combine in priority order
        List<String> orderedMoves = new ArrayList<>();
        orderedMoves.addAll(safeCaptures);
        orderedMoves.addAll(safeMoves);
        orderedMoves.addAll(riskyMoves);
        
        return orderedMoves.toArray(new String[0]);
    }
    
    /**
     * Calculate how threatening an enemy piece is based on its position
     */
    private int getEnemyThreatLevel(int row, boolean weAreRed) {
        if (weAreRed) {
            // We are red, enemy is black advancing toward row 7
            // Enemy at row 0 = threat 0, enemy at row 7 = threat 7
            return row;
        } else {
            // We are black, enemy is red advancing toward row 0  
            // Enemy at row 7 = threat 0, enemy at row 0 = threat 7
            return 7 - row;
        }
    }
    
    /**
     * CRITICAL: Ultra-fast check if a move will expose our piece to enemy capture
     * Optimized for speed with early termination
     */
    private boolean willBeExposedToCapture(Board board, Board.Move move, String color) {
        // Create a temporary board to test the move
        Board tempBoard = copyBoard(board);
        if (!tempBoard.makeMove(move)) {
            return false; // Invalid move
        }
        
        boolean isRed = color.equalsIgnoreCase("red");
        
        // Fast check: only look at enemy pieces that could potentially reach our destination
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = tempBoard.getPiece(row, col);
                
                // Skip empty squares and our own pieces
                if (piece == Board.EMPTY) continue;
                boolean isPieceRed = (piece == Board.RED_PUSHER || piece == Board.RED_PUSHED);
                if (isPieceRed == isRed) continue; // Skip our own pieces
                
                // Quick distance check - if too far, skip detailed move generation
                int distance = Math.abs(row - move.toRow) + Math.abs(col - move.toCol);
                if (distance > 2) continue; // Enemy pieces more than 2 squares away can't capture in one move
                
                // Check if this enemy piece can capture our piece
                if (canPieceCapturePosition(tempBoard, row, col, move.toRow, move.toCol)) {
                    return true; // Exposed to capture!
                }
            }
        }
        
        return false;
    }
    
    /**
     * Fast check if a piece at (fromRow, fromCol) can capture position (toRow, toCol)
     */
    private boolean canPieceCapturePosition(Board board, int fromRow, int fromCol, int toRow, int toCol) {
        int piece = board.getPiece(fromRow, fromCol);
        if (piece == Board.EMPTY) return false;
        
        boolean isPusher = (piece == Board.RED_PUSHER || piece == Board.BLACK_PUSHER);
        boolean isRed = (piece == Board.RED_PUSHER || piece == Board.RED_PUSHED);
        
        if (isPusher) {
            // Pusher movement rules
            int direction = isRed ? -1 : 1; // Red moves up (-1), Black moves down (+1)
            
            // Can move diagonally forward to capture
            if (toRow == fromRow + direction && Math.abs(toCol - fromCol) == 1) {
                return true;
            }
        }
        
        // For pushed pieces, we'd need to check if there's a pusher behind them
        // But for efficiency, we'll do a simplified check
        
        return false;
    }
    
    /**
     * Enhanced threat detection - check if any of our pieces are under immediate threat
     */
    private boolean isUnderImmediateThreat(Board board, int row, int col, String color) {
        boolean isRed = color.equalsIgnoreCase("red");
        String enemyColor = isRed ? "black" : "red";
        
        // Get all enemy moves and see if any target this position
        String[] enemyMoves = MoveGenerator.move(enemyColor, board);
        
        for (String enemyMoveStr : enemyMoves) {
            Board.Move enemyMove = board.parseMove(enemyMoveStr);
            if (enemyMove != null && enemyMove.toRow == row && enemyMove.toCol == col) {
                return true; // This piece is under threat!
            }
        }
        
        return false;
    }
}
