# ChessAPI
Simple api for managing chess games
## How to
Firstly you need to create instance of 
[```GameManager```](https://github.com/dzikimlecz/ChessAPI/blob/main/src/main/java/me/dzikimlecz/chessapi/GamesManager.java), or if it's needed, of your own subclass of it.<br>
Generic type parameter ```Key``` is used as a key for accessing games managed by the object.<br>
*Note: Type given as key **must** override both equals, and hashCode methods. It's required, due to internal use of HashMaps*<br>
Secondly you will need an implementation of
[```ChessEventListener```](https://github.com/dzikimlecz/ChessAPI/blob/main/src/main/java/me/dzikimlecz/chessapi/ChessEventListener.java).
One game per one instance is the recommended technique, although if the client deals, with synchronisation it's not required.
There are also more optional features, but to create basic working implementation that's all of client code
### Most Basic Implementation
    
    class Listener implements ChessEventListener {
        private final GamesManager<String> manager;
    
        Listener(GamesManager<String> manager) {
            this.manager = manager;
        }
    
        @Override public void onMate(Color winner) {
            System.out.println(winner + " won!");
        }
    
        @Override public void onDraw(DrawReason reason) {
            System.out.println("That's a draw!");
        }
    
        @Override public void onMoveHandled() {
            var read = manager.read("Coffee");
            for (ChessPiece[] chessPieces : read) {
                for (ChessPiece piece : chessPieces) 
                    if (piece == null) System.out.print(" ".repeat(3));
                    else System.out.print(piece.color().name().charAt(0) + piece.toString() + " ");
                System.out.println();
            }
        }
    
        @Override public void onIllegalMove() {
            System.out.println("You can't move like that");
        }
    
        @Override public boolean onDrawRequest(Color requestor) {
            return false;
        }
        
        //i know i know, but it ain't hard to write just wordy.
        @Override public Class<? extends Piece> onPawnExchange() {
            return Queen.class;
        }
    }    

    public class Main {
        public static void main(String[] args) {
            var manager = new GamesManager<String>();
            manager.newGame("Coffee", new Listener());
            var in = new Scanner(System.in);
            for (String input = in.nextLine(); !input.equals("quit"); input = in.nextLine())
                manager.move("Coffee", input);
        }
    }

###Useful features
Class 
[GameInfo](https://github.com/dzikimlecz/ChessAPI/blob/main/src/main/java/me/dzikimlecz/chessapi/GameInfo.java)
lets you save info about players in the game, quite useful.
####Example
    manager.attachInfo(new GameInfo<>("Coffee", 21, 37));
* First parameter - gameKey
* Second and third parameter - white and black players
* First Type parameter - gameKey's type 
* First Type parameter - the players' type
<br><br>
