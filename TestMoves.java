public class TestMoves {
    public static void main(String[] args) {
        Board board = new Board();
        MiniMax miniMax = new MiniMax();
        
        System.out.println("Initial board state:");
        board.printBoard();
        
        System.out.println("\nTesting move generation for RED:");
        String[] redMoves = MoveGenerator.move("red", board);
        System.out.println("RED moves count: " + redMoves.length);
        for (int i = 0; i < Math.min(redMoves.length, 10); i++) {
            System.out.println("  " + redMoves[i]);
        }
        
        System.out.println("\nTesting move generation for BLACK:");
        String[] blackMoves = MoveGenerator.move("black", board);
        System.out.println("BLACK moves count: " + blackMoves.length);
        for (int i = 0; i < Math.min(blackMoves.length, 10); i++) {
            System.out.println("  " + blackMoves[i]);
        }
        
        System.out.println("\nTesting AI for RED (multiple runs):");
        board.setRedPlayer(true);
        for (int i = 0; i < 5; i++) {
            String redBestMove = miniMax.findBestMove(board, "red");
            System.out.println("RED best move " + (i+1) + ": " + redBestMove);
        }
        
        System.out.println("\nTesting AI for BLACK (multiple runs):");
        board.setRedPlayer(false);
        for (int i = 0; i < 5; i++) {
            String blackBestMove = miniMax.findBestMove(board, "black");
            System.out.println("BLACK best move " + (i+1) + ": " + blackBestMove);
        }
    }
}
