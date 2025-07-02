import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


class Client {
    public static void main(String[] args) {
    Socket MyClient;
    BufferedInputStream input;
    BufferedOutputStream output;
    Board board = new Board();
    MiniMax miniMax = new MiniMax();
    
    try {
        MyClient = new Socket("localhost", 8888);

        input    = new BufferedInputStream(MyClient.getInputStream());
        output   = new BufferedOutputStream(MyClient.getOutputStream());

        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.println("Connected to server. Waiting for commands...");
        
        while(true){
            char cmd = 0;
               
            cmd = (char)input.read();
            System.out.println("Received command: " + cmd);
            
            // Small delay to ensure all data is available
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // Ignore
            }
            
            // cmd == '1': Start new game as RED player
            // Receives initial board state and finds best move using AI
            if(cmd == '1'){
    // Read the board configuration data
    byte[] aBuffer = new byte[256];
    int size = input.available();
    if (size > 0) {
        input.read(aBuffer, 0, size);
        String boardData = new String(aBuffer, 0, size).trim();
        System.out.println("Board data received: " + boardData);
        board.parseBoardFromServer(boardData);
    }
    board.setRedPlayer(true);
    System.out.println("Finding best move for RED...");
    String move = miniMax.findBestMove(board, "red");
    System.out.println("Best move found: " + move);
    if (move != null) {
        board.makeMoveFromServer(move);
        output.write(move.getBytes(), 0, move.length());
        output.flush();
        System.out.println("Move sent: " + move);
    }
}

// cmd == '2': Start new game as BLACK player
// Receives initial board state and uses AI to find best move
if(cmd == '2'){
    // Read the board configuration data
    byte[] aBuffer = new byte[256];
    int size = input.available();
    if (size > 0) {
        input.read(aBuffer, 0, size);
        String boardData = new String(aBuffer, 0, size).trim();
        System.out.println("Board data received: " + boardData);
        board.parseBoardFromServer(boardData);
    }
    board.setRedPlayer(false);
    System.out.println("Finding best move for BLACK...");
    String move = miniMax.findBestMove(board, "black");
    System.out.println("Best move found: " + move);
    if (move != null) {
        board.makeMoveFromServer(move);
        output.write(move.getBytes(), 0, move.length());
        output.flush();
        System.out.println("Move sent: " + move);
    }
}

        // cmd == '3': Server requests next move (BLACK player turn)
        // Receives opponent's last move and responds with AI move
        if(cmd == '3'){
        byte[] aBuffer = new byte[64]; // Increased buffer size
                
        int size = input.available();
        if (size > 0) {
            input.read(aBuffer, 0, Math.min(size, aBuffer.length));
            String opponentMove = new String(aBuffer, 0, size).trim();
            System.out.println("Opponent's move received: " + opponentMove);
            // Apply opponent's move to our board
            if (!opponentMove.isEmpty()) {
                board.makeMoveFromServer(opponentMove);
            }
        }
        
        // Find our best move as black
        System.out.println("Finding best move for BLACK...");
        String move = miniMax.findBestMove(board, "black");
        System.out.println("Best move found: " + move);
        if (move != null) {
            board.makeMoveFromServer(move);
            output.write(move.getBytes(), 0, move.length());
            output.flush();
            System.out.println("Move sent: " + move);
        }
                
         }
         
            // cmd == '4': Server requests next move (RED player turn)
            // Receives opponent's move and responds with AI move
            if(cmd == '4'){
                byte[] aBuffer = new byte[64]; // Increased buffer size
                int size = input.available();
                if (size > 0) {
                    input.read(aBuffer, 0, Math.min(size, aBuffer.length));
                    String opponentMove = new String(aBuffer, 0, size).trim();
                    System.out.println("Opponent's move received: " + opponentMove);
                    // Apply opponent's move to our board
                    if (!opponentMove.isEmpty()) {
                        board.makeMoveFromServer(opponentMove);
                    }
                }
                
                // Find our best move as red
                System.out.println("Finding best move for RED...");
                String move = miniMax.findBestMove(board, "red");
                System.out.println("Best move found: " + move);
                if (move != null) {
                    board.makeMoveFromServer(move);
                    output.write(move.getBytes(), 0, move.length());
                    output.flush();
                    System.out.println("Move sent: " + move);
                }
                
            }
            
        // cmd == '5': Server requests next move with opponent's last move info
        // Receives opponent's move data and waits for user input
        if(cmd == '5'){
                byte[] aBuffer = new byte[16];
                int size = input.available();
                input.read(aBuffer,0,size);
        String move = console.readLine();
        output.write(move.getBytes(),0,move.length());
        output.flush();
                
        }
        }
    }
    catch (IOException e) {
        System.err.println("Connection error: " + e.getMessage());
        e.printStackTrace();
    }
    catch (Exception e) {
        System.err.println("Unexpected error: " + e.getMessage());
        e.printStackTrace();
    }
    
    }
}
