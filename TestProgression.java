public class TestProgression {
    public static void main(String[] args) {
        Board board = new Board();
        board.setRedPlayer(true);
        
        System.out.println("=== GAME PROGRESSION TEST ===");
        
        // Initial state
        System.out.println("Initial moves for RED:");
        String[] initialMoves = MoveGenerator.move("red", board);
        System.out.println("  " + initialMoves.length + " moves: " + java.util.Arrays.toString(initialMoves));
        
        // Make the move A2A3
        boolean success = board.makeMoveFromServer("A2A3");
        System.out.println("\nMade move A2A3: " + success);
        
        // Now check what moves are available
        System.out.println("\nAfter A2A3, moves for RED:");
        String[] nextMoves = MoveGenerator.move("red", board);
        System.out.println("  " + nextMoves.length + " moves: " + java.util.Arrays.toString(nextMoves));
        
        // Check specific positions
        System.out.println("\nBoard state after A2A3:");
        System.out.println("  A1: " + getPieceStr(board.getPiece(7, 0)));
        System.out.println("  A2: " + getPieceStr(board.getPiece(6, 0)));
        System.out.println("  A3: " + getPieceStr(board.getPiece(5, 0)));
        System.out.println("  A4: " + getPieceStr(board.getPiece(4, 0)));
    }
    
    private static String getPieceStr(int piece) {
        switch (piece) {
            case Board.EMPTY: return "Empty";
            case Board.BLACK_PUSHED: return "Black Pushed";
            case Board.BLACK_PUSHER: return "Black Pusher";
            case Board.RED_PUSHED: return "Red Pushed";
            case Board.RED_PUSHER: return "Red Pusher";
            default: return "Unknown(" + piece + ")";
        }
    }
}
