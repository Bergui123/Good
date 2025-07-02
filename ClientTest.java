public class ClientTest {
    public static void main(String[] args) {
        System.out.println("=== CLIENT AI TEST ===");
        
        Board board = new Board();
        MiniMax miniMax = new MiniMax();
        
        System.out.println("Initial board:");
        board.printBoard();
        
        // Test sequence: Red plays, then Black plays
        for (int turn = 1; turn <= 5; turn++) {
            System.out.println("\n--- Turn " + turn + " ---");
            
            // Red's turn
            System.out.println("RED's turn:");
            board.setRedPlayer(true);
            String redMove = miniMax.findBestMove(board, "red");
            System.out.println("RED plays: " + redMove);
            if (redMove != null) {
                board.makeMoveFromServer(redMove);
                board.printBoard();
            }
            
            // Black's turn
            System.out.println("\nBLACK's turn:");
            board.setRedPlayer(false);
            String blackMove = miniMax.findBestMove(board, "black");
            System.out.println("BLACK plays: " + blackMove);
            if (blackMove != null) {
                board.makeMoveFromServer(blackMove);
                board.printBoard();
            }
            
            if (board.isGameOver()) {
                System.out.println("Game Over! Winner: " + board.getWinner());
                break;
            }
        }
    }
}
