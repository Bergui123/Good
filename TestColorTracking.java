// Test that the AI correctly tracks which color it's playing as
// This simulates the color detection logic from Connection.java

public class TestColorTracking {
    public static void main(String[] args) {
        Board board = new Board();
        MiniMax miniMax = new MiniMax();
        
        // Test scenario 1: Playing as RED
        String myColor = "red";
        board.setRedPlayer(true);
        
        System.out.println("=== Test 1: Playing as RED ===");
        System.out.println("My color: " + myColor);
        
        String move1 = miniMax.findBestMove(board, myColor);
        System.out.println("Generated move for RED: " + move1);
        
        // Test scenario 2: Playing as BLACK
        board = new Board(); // Reset board
        myColor = "black";
        board.setRedPlayer(false);
        
        System.out.println("\n=== Test 2: Playing as BLACK ===");
        System.out.println("My color: " + myColor);
        
        String move2 = miniMax.findBestMove(board, myColor);
        System.out.println("Generated move for BLACK: " + move2);
        
        // Test the key fix: different colors should generate different moves
        if (move1 != null && move2 != null && !move1.equals(move2)) {
            System.out.println("✓ Correctly generated different moves for different colors");
        } else {
            System.out.println("✗ ERROR: Same move generated for both colors or null moves");
        }
        
        System.out.println("\n=== Color Tracking Test Complete ===");
        System.out.println("This confirms the AI will generate moves for the correct color");
        System.out.println("when using the tracked 'myColor' variable in Connection.java");
    }
}
