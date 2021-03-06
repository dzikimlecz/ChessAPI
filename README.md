# ChessAPI
Simple api for managing chess games
## How to
Firstly you need to create instance of 
[```GameManager```](https://github.com/dzikimlecz/ChessAPI/blob/main/src/main/java/me/dzikimlecz/chessapi/GamesManager.java), or if it's needed, of your own subclass of it.<br>
Generic type parameter ```Key``` is used as a key for accessing games managed by the object.<br>
*Note: Type given as key **must** override both equals, and hashCode methods. It's required, due to internal use of HashMaps*<br>
Secondly you will need an implementation of
[```ChessEventListener```](https://github.com/dzikimlecz/ChessAPI/blob/main/src/main/java/me/dzikimlecz/chessapi/ChessEventListener.java).
One game per one instance is the recommended technique, altough if the client deals, with synchronisation it's not required.
There are also more optional features, but to create basic working implementation that's all of client code
