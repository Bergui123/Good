public class TestMove {
    public static void main(String[] args) {
        Board board = new Board();
        board.setRedPlayer(true);
        
        System.out.println("=== TESTING MOVE EXECUTION ===");
        
        // Test making the move A2A3
        System.out.println("Before move A2A3:");
        System.out.println("  A1: " + getPieceStr(board.getPiece(7, 0)));
        System.out.println("  A2: " + getPieceStr(board.getPiece(6, 0)));
        System.out.println("  A3: " + getPieceStr(board.getPiece(5, 0)));
        
        boolean success = board.makeMoveFromServer("A2A3");
        System.out.println("Move A2A3 result: " + success);
        
        System.out.println("After move A2A3:");
        System.out.println("  A1: " + getPieceStr(board.getPiece(7, 0)));
        System.out.println("  A2: " + getPieceStr(board.getPiece(6, 0)));
        System.out.println("  A3: " + getPieceStr(board.getPiece(5, 0)));
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
