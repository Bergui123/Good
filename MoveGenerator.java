import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {
    public static String[] move(String color, Board board) {
        List<String> moves = new ArrayList<>();
        
        // Check every tile in the board (8x8)
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int pieceValue = board.getPiece(row, col);
                
                // Only generate moves for pieces that belong to the current player
                if (isPieceOfColor(pieceValue, color)) {
                    moves.addAll(PossibleMoves(pieceValue, col, row, board));
                }
            }
        }
        
        return moves.toArray(new String[0]);
    }
    
    // Helper method to get a description of the piece
    private static String getPieceDescription(int piece) {
        switch (piece) {
            case Board.EMPTY: return "Empty";
            case Board.BLACK_PUSHED: return "Black Pushed";
            case Board.BLACK_PUSHER: return "Black Pusher";
            case Board.RED_PUSHED: return "Red Pushed";
            case Board.RED_PUSHER: return "Red Pusher";
            default: return "Unknown";
        }
    }
    private static List<String> PossibleMoves(int pieceValue, int col, int row, Board board)
    {
        List<String> moves = new ArrayList<>();

        if (getPieceDescription(pieceValue).equals("Black Pusher") || getPieceDescription(pieceValue).equals("Red Pusher"))
        {
            // Determine movement direction (Black moves down, Red moves up)
            int direction = getPieceDescription(pieceValue).startsWith("Black") ? 1 : -1;

            // Check front cell (row + direction, col) to see if blocked
            int frontRow = row + direction;
            if (frontRow >= 0 && frontRow < 8)
            {
                int frontPiece = board.getPiece(frontRow, col);
                if (frontPiece == Board.EMPTY)
                {
                    // Front is empty, can move
                    String fromPos = board.positionToString(row, col);
                    String toPos = board.positionToString(frontRow, col);
                    moves.add(fromPos + toPos);
                }
                // NOTE: Cannot move forward if ANY piece is directly in front (same or opposite color)
            }

            // Check diagonal left
            int diagLeftRow = row + direction;
            int diagLeftCol = col - 1;
            if (diagLeftRow >= 0 && diagLeftRow < 8 && diagLeftCol >= 0 && diagLeftCol < 8)
            {
                int diagLeftPiece = board.getPiece(diagLeftRow, diagLeftCol);
                if (diagLeftPiece == Board.EMPTY)
                {
                    // If empty, can move
                    String fromPos = board.positionToString(row, col);
                    String toPos = board.positionToString(diagLeftRow, diagLeftCol);
                    moves.add(fromPos + toPos);
                }
                else if (isOppositeColor(pieceValue, diagLeftPiece))
                {
                    // Can capture diagonally if opposite color is there
                    String fromPos = board.positionToString(row, col);
                    String toPos = board.positionToString(diagLeftRow, diagLeftCol);
                    moves.add(fromPos + toPos);
                }
                // NOTE: Cannot move diagonally if same color piece is there
            }

            // Check diagonal right
            int diagRightRow = row + direction;
            int diagRightCol = col + 1;
            if (diagRightRow >= 0 && diagRightRow < 8 && diagRightCol >= 0 && diagRightCol < 8)
            {
                int diagRightPiece = board.getPiece(diagRightRow, diagRightCol);
                if (diagRightPiece == Board.EMPTY)
                {
                    // If empty, can move
                    String fromPos = board.positionToString(row, col);
                    String toPos = board.positionToString(diagRightRow, diagRightCol);
                    moves.add(fromPos + toPos);
                }
                else if (isOppositeColor(pieceValue, diagRightPiece))
                {
                    // Can capture diagonally if opposite color is there
                    String fromPos = board.positionToString(row, col);
                    String toPos = board.positionToString(diagRightRow, diagRightCol);
                    moves.add(fromPos + toPos);
                }
                // NOTE: Cannot move diagonally if same color piece is there
            }
        }
        else if (getPieceDescription(pieceValue).equals("Black Pushed") || getPieceDescription(pieceValue).equals("Red Pushed"))
        {
            // Pushed pieces can only move when pushed by a pusher
            // Determine the direction this pushed piece moves
            int direction = getPieceDescription(pieceValue).startsWith("Black") ? 1 : -1;
            
            // Check for pushers that can push this piece
            // A pusher can be directly behind or in diagonal behind positions
            
            // Check directly behind - pushed piece moves straight forward
            int behindRow = row - direction;
            int behindCol = col;
            if (behindRow >= 0 && behindRow < 8)
            {
                int behindPiece = board.getPiece(behindRow, behindCol);
                if (isPusherOfSameColor(pieceValue, behindPiece))
                {
                    // Can move forward if front is empty
                    int frontRow = row + direction;
                    if (frontRow >= 0 && frontRow < 8)
                    {
                        int frontPiece = board.getPiece(frontRow, col);
                        if (frontPiece == Board.EMPTY)
                        {
                            String fromPos = board.positionToString(row, col);
                            String toPos = board.positionToString(frontRow, col);
                            moves.add(fromPos + toPos);
                        }
                        // NOTE: Cannot move forward if there's any piece (same or opposite color) directly in front
                    }
                }
            }
            
            // Check diagonal behind left - pushed piece moves to diagonal right
            int behindLeftRow = row - direction;
            int behindLeftCol = col - 1;
            if (behindLeftRow >= 0 && behindLeftRow < 8 && behindLeftCol >= 0 && behindLeftCol < 8)
            {
                int behindLeftPiece = board.getPiece(behindLeftRow, behindLeftCol);
                if (isPusherOfSameColor(pieceValue, behindLeftPiece))
                {
                    // Can move diagonally right (opposite of pusher's position)
                    int diagRightRow = row + direction;
                    int diagRightCol = col + 1;
                    if (diagRightRow >= 0 && diagRightRow < 8 && diagRightCol >= 0 && diagRightCol < 8)
                    {
                        int diagRightPiece = board.getPiece(diagRightRow, diagRightCol);
                        if (diagRightPiece == Board.EMPTY)
                        {
                            // Can move to empty diagonal
                            String fromPos = board.positionToString(row, col);
                            String toPos = board.positionToString(diagRightRow, diagRightCol);
                            moves.add(fromPos + toPos);
                        }
                        else if (isOppositeColor(pieceValue, diagRightPiece))
                        {
                            // Can capture diagonally if opposite color
                            String fromPos = board.positionToString(row, col);
                            String toPos = board.positionToString(diagRightRow, diagRightCol);
                            moves.add(fromPos + toPos);
                        }
                        // NOTE: Cannot move if same color piece is there
                    }
                }
            }
            
            // Check diagonal behind right - pushed piece moves to diagonal left
            int behindRightRow = row - direction;
            int behindRightCol = col + 1;
            if (behindRightRow >= 0 && behindRightRow < 8 && behindRightCol >= 0 && behindRightCol < 8)
            {
                int behindRightPiece = board.getPiece(behindRightRow, behindRightCol);
                if (isPusherOfSameColor(pieceValue, behindRightPiece))
                {
                    // Can move diagonally left (opposite of pusher's position)
                    int diagLeftRow = row + direction;
                    int diagLeftCol = col - 1;
                    if (diagLeftRow >= 0 && diagLeftRow < 8 && diagLeftCol >= 0 && diagLeftCol < 8)
                    {
                        int diagLeftPiece = board.getPiece(diagLeftRow, diagLeftCol);
                        if (diagLeftPiece == Board.EMPTY)
                        {
                            // Can move to empty diagonal
                            String fromPos = board.positionToString(row, col);
                            String toPos = board.positionToString(diagLeftRow, diagLeftCol);
                            moves.add(fromPos + toPos);
                        }
                        else if (isOppositeColor(pieceValue, diagLeftPiece))
                        {
                            // Can capture diagonally if opposite color
                            String fromPos = board.positionToString(row, col);
                            String toPos = board.positionToString(diagLeftRow, diagLeftCol);
                            moves.add(fromPos + toPos);
                        }
                        // NOTE: Cannot move if same color piece is there
                    }
                }
            }
        }

        return moves;
    }

    // Helper method to check if a piece is a pusher of the same color
    private static boolean isPusherOfSameColor(int pushedPiece, int pusherPiece) {
        if (pushedPiece == Board.BLACK_PUSHED && pusherPiece == Board.BLACK_PUSHER) {
            return true;
        }
        if (pushedPiece == Board.RED_PUSHED && pusherPiece == Board.RED_PUSHER) {
            return true;
        }
        return false;
    }

    private static boolean isBlackPiece(int piece) {
        return piece == Board.BLACK_PUSHED || piece == Board.BLACK_PUSHER;
    }

    private static boolean isRedPiece(int piece) {
        return piece == Board.RED_PUSHED || piece == Board.RED_PUSHER;
    }

    // Helper methods to check color (implementation depends on how colors are defined)
    private static boolean isSameColor(int pieceValue, int otherPieceValue) {
        if (otherPieceValue == Board.EMPTY) {
            return false;
        }
        return (isBlackPiece(pieceValue) && isBlackPiece(otherPieceValue)) ||
               (isRedPiece(pieceValue) && isRedPiece(otherPieceValue));
    }

    private static boolean isOppositeColor(int pieceValue, int otherPieceValue) {
        if (otherPieceValue == Board.EMPTY) {
            return false;
        }
        return (isBlackPiece(pieceValue) && isRedPiece(otherPieceValue)) ||
               (isRedPiece(pieceValue) && isBlackPiece(otherPieceValue));
    }

    // Helper method to check if a piece belongs to the specified color
    private static boolean isPieceOfColor(int piece, String color) {
        if (piece == Board.EMPTY) {
            return false;
        }
        
        if (color.equalsIgnoreCase("red") || color.equalsIgnoreCase("r")) {
            return isRedPiece(piece);
        } else if (color.equalsIgnoreCase("black") || color.equalsIgnoreCase("b")) {
            return isBlackPiece(piece);
        }
        
        return false;
    }

    // Add this helper method
    private static boolean isSameColorPushedPiece(int pusherPiece, int otherPiece) {
        if (pusherPiece == Board.BLACK_PUSHER && otherPiece == Board.BLACK_PUSHED) {
            return true;
        }
        if (pusherPiece == Board.RED_PUSHER && otherPiece == Board.RED_PUSHED) {
            return true;
        }
        return false;
    }
}
