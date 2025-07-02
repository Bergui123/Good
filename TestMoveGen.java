public class TestMoveGen {
    public static void main(String[] args) {
        // Create a board with starting position
        Board board = new Board();
        board.setRedPlayer(true);
        
        System.out.println("Testing RED move generation from starting position:");
        
        // Generate moves for RED
        String[] redMoves = MoveGenerator.move("red", board);
        
        System.out.println("Generated " + redMoves.length + " moves for RED:");
        for (String move : redMoves) {
            System.out.println("  " + move);
        }
        
        // Let's also check what pieces are where in starting position
        System.out.println("\nStarting board positions:");
        System.out.println("Row 7 (Red pushers): ");
        for (int col = 0; col < 8; col++) {
            char file = (char)('A' + col);
            int piece = board.getPiece(7, col);
            System.out.println("  " + file + "1: " + getPieceStr(piece));
        }
        
        System.out.println("Row 6 (Red pushed): ");
        for (int col = 0; col < 8; col++) {
            char file = (char)('A' + col);
            int piece = board.getPiece(6, col);
            System.out.println("  " + file + "2: " + getPieceStr(piece));
        }
    }
    
    private static String getPieceStr(int piece) {
        switch (piece) {
            case Board.EMPTY: return "Empty";
            case Board.BLACK_PUSHED: return "Black Pushed";
            case Board.BLACK_PUSHER: return "Black Pusher";
            case Board.RED_PUSHED: return "Red Pushed";
            case Board.RED_PUSHER: return "Red Pusher";
            default: return "Unknown";
        }
    }
}
