// Test the winning move detection and priority
public class TestWinningMoves {
    public static void main(String[] args) {
        Board board = new Board();
        MiniMax miniMax = new MiniMax();
        
        // Create a board state where Red can win in one move
        // Clear the board first
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board.setPiece(row, col, Board.EMPTY);
            }
        }
        
        // Set up a winning scenario for Red
        // Put a red pusher at row 1 (almost at goal)
        board.setPiece(1, 3, Board.RED_PUSHER); // D7 position
        
        // Add some other pieces so there are multiple moves available
        board.setPiece(6, 0, Board.RED_PUSHER); // A2 position
        board.setPiece(6, 1, Board.RED_PUSHED); // B2 position
        board.setPiece(1, 0, Board.BLACK_PUSHER); // A7 position
        board.setPiece(2, 0, Board.BLACK_PUSHED); // A6 position
        
        board.setRedPlayer(true);
        
        System.out.println("=== Test Winning Move Detection ===");
        System.out.println("Red pusher at D7 can move to D8 to win");
        
        // Generate moves and check if winning move is prioritized
        String[] moves = MoveGenerator.move("red", board);
        System.out.println("Available moves for Red:");
        for (String move : moves) {
            System.out.println("  " + move);
        }
        
        // Find the best move - should be the winning move
        String bestMove = miniMax.findBestMove(board, "red");
        System.out.println("\nBest move chosen: " + bestMove);
        
        if (bestMove != null && bestMove.equals("D7D8")) {
            System.out.println("✓ SUCCESS: AI correctly chose the winning move!");
        } else {
            System.out.println("✗ FAILURE: AI did not choose the winning move D7D8");
        }
        
        // Test the evaluation too
        char[][] charBoard = new char[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = board.getPiece(row, col);
                switch (piece) {
                    case Board.EMPTY: charBoard[row][col] = ' '; break;
                    case Board.BLACK_PUSHED: charBoard[row][col] = 'b'; break;
                    case Board.BLACK_PUSHER: charBoard[row][col] = 'B'; break;
                    case Board.RED_PUSHED: charBoard[row][col] = 'r'; break;
                    case Board.RED_PUSHER: charBoard[row][col] = 'R'; break;
                    default: charBoard[row][col] = ' '; break;
                }
            }
        }
        
        int evalBefore = BoardEvaluation.evaluate(charBoard, "red");
        System.out.println("Board evaluation before winning move: " + evalBefore);
        
        // Simulate the winning move
        charBoard[0][3] = 'R'; // Move pusher to goal
        charBoard[1][3] = ' '; // Remove from old position
        
        int evalAfter = BoardEvaluation.evaluate(charBoard, "red");
        System.out.println("Board evaluation after winning move: " + evalAfter);
        
        if (evalAfter > evalBefore + 500000) {
            System.out.println("✓ SUCCESS: Winning position has massive evaluation bonus!");
        } else {
            System.out.println("✗ FAILURE: Winning position doesn't have enough bonus");
        }
    }
}
