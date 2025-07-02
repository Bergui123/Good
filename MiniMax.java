import java.util.Random;

public class MiniMax {
    private static final int MAX_DEPTH = 8; // Reduced depth for faster response
    private static final long TIME_LIMIT = 3000; // 3 seconds in milliseconds (faster)
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
        
        // Iterative deepening - start with depth 1 and increase
        for (int depth = 1; depth <= MAX_DEPTH && !timeUp; depth++) {
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
     * Order moves to prioritize pusher moves and advancement
     */
    private String[] orderMoves(String[] moves, Board board, String color) {
        // Create array of move scores for ordering
        MoveScore[] moveScores = new MoveScore[moves.length];
        
        for (int i = 0; i < moves.length; i++) {
            moveScores[i] = new MoveScore(moves[i], scoreMoveForOrdering(moves[i], board, color));
        }
        
        // Sort by score (highest first)
        java.util.Arrays.sort(moveScores, (a, b) -> Integer.compare(b.score, a.score));
        
        // Extract sorted moves
        String[] sortedMoves = new String[moves.length];
        for (int i = 0; i < moves.length; i++) {
            sortedMoves[i] = moveScores[i].move;
        }
        
        return sortedMoves;
    }
    
    /**
     * Score a move for ordering purposes (not evaluation)
     */
    private int scoreMoveForOrdering(String moveStr, Board board, String color) {
        Board.Move move = board.parseMove(moveStr);
        if (move == null) return 0;
        
        int piece = board.getPiece(move.fromRow, move.fromCol);
        int score = 0;
        
        // Heavily favor pusher moves
        if (piece == Board.RED_PUSHER || piece == Board.BLACK_PUSHER) {
            score += 100;
        }
        
        // Favor advancement moves
        boolean isRed = color.equalsIgnoreCase("red");
        if (isRed && move.toRow < move.fromRow) { // Red advancing north
            score += 50 + (move.fromRow - move.toRow) * 10;
        } else if (!isRed && move.toRow > move.fromRow) { // Black advancing south
            score += 50 + (move.toRow - move.fromRow) * 10;
        }
        
        // Favor captures
        int targetPiece = board.getPiece(move.toRow, move.toCol);
        if (targetPiece != Board.EMPTY) {
            score += 30;
            if (targetPiece == Board.RED_PUSHER || targetPiece == Board.BLACK_PUSHER) {
                score += 50; // Extra for capturing pushers
            }
        }
        
        // Favor center control
        if (move.toCol >= 2 && move.toCol <= 5) {
            score += 10;
        }
        
        return score;
    }
    
    /**
     * Helper class for move ordering
     */
    private static class MoveScore {
        String move;
        int score;
        
        MoveScore(String move, int score) {
            this.move = move;
            this.score = score;
        }
    }
}
