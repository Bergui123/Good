public class TestDetailed {
    public static void main(String[] args) {
        // Create a board with starting position
        Board board = new Board();
        board.setRedPlayer(true);
        
        System.out.println("=== DETAILED MOVE GENERATION TEST ===");
        
        // Check what pieces are actually being detected as Red pieces
        System.out.println("\nChecking each position for Red pieces:");
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = board.getPiece(row, col);
                if (piece == Board.RED_PUSHER || piece == Board.RED_PUSHED) {
                    char file = (char)('A' + col);
                    int rank = 8 - row;
                    String pos = "" + file + rank;
                    String type = (piece == Board.RED_PUSHER) ? "RED_PUSHER" : "RED_PUSHED";
                    System.out.println("  " + pos + ": " + type + " (value=" + piece + ")");
                }
            }
        }
        
        // Generate moves for RED
        String[] redMoves = MoveGenerator.move("red", board);
        
        System.out.println("\nGenerated " + redMoves.length + " moves for RED:");
        for (String move : redMoves) {
            System.out.println("  " + move);
        }
        
        // Let's manually check what moves should be generated for pusher at A1
        System.out.println("\nManual check for pusher at A1 (row=7, col=0):");
        int pusherPiece = board.getPiece(7, 0);
        System.out.println("  Piece at A1: " + getPieceStr(pusherPiece));
        
        // Check front (A2)
        int frontPiece = board.getPiece(6, 0);
        System.out.println("  Piece at A2 (front): " + getPieceStr(frontPiece));
        
        // Check diagonal left (would be off board)
        // Check diagonal right (B2)
        if (7-1 >= 0 && 0+1 < 8) {
            int diagPiece = board.getPiece(6, 1);
            System.out.println("  Piece at B2 (diag right): " + getPieceStr(diagPiece));
        }
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
