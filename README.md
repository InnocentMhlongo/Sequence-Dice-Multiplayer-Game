# Sequence Dice - Multiplayer Game

A real-time multiplayer dice game for Android with Java server backend, supporting multiple concurrent games through TCP socket connections.

## 🎯 Game Rules
The aim of the game is for a player (or team of players) to complete a sequence of five tokens of their colour (or six tokens in a 3 or 4 player game), in either a connected line in a row, column or diagonal. The game play rules are simple, to decide who starts the game, each player rolls the two die and the player with the highest total starts.
Thereafter each player gets a turn in a clockwise order, until there is a winner.
On a player’s turn, they roll the die. The sum of the die indicate what the player can do:
1. If there are one or more cells with the same number as the sum of the die that are empty, the player must place one of their tokens in one of the corresponding cells.
2. If there are no empty cells with the same number as the sum of the die (including 2 and 12), then the player must replace an opponent’s token on one of these cells with their own token. If all the cells are occupied by the player’s (or team’s) tokens, then the player cannot place a token;
3. If a 2 or 12 is rolled, then in addition to #1 and #2 above, the player gets an extra turn;
4. If a 10 is rolled (called a defensive roll), then the player must remove one of their opponent’s tokens from the board, except those in the grey cells (2s and 12s);
5. If an 11 is rolled (called a wild roll), then the player must place one of their tokens in any empty cell on the board.

## 🏗️ Architecture

### Client (Android)
- Real-time gameplay interface
- Socket-based communication with server
- Room creation and joining system
- Multi-threaded UI updates

### Server (Java)
- Multi-threaded TCP server handling concurrent connections
- BlockingQueue for thread-safe message handling
- Room management system supporting [X] simultaneous games
- Player synchronization and game state management

## 🛠️ Tech Stack
- **Client:** Java, Android SDK, Socket.IO
- **Server:** Java, TCP Sockets, Multi-threading, BlockingQueue
- **Communication:** Custom protocol over TCP/IP

## 🚀 Setup Instructions

### Server Setup
1. Navigate to `JavaServer/` directory
2. Compile: `javac *.java`
3. Run: `java GameServer`
4. Server starts on port [ADD PORT NUMBER]

### Client Setup
1. Open `AndroidClient/` in Android Studio
2. Update server IP in `[CONFIG FILE]` (line [X])
3. Run on Android device or emulator (API level [X]+)
4. Enter server IP and port to connect

## 📱 Features
- **Online Multiplayer:** Connect with friends over network
- **Room System:** Create or join existing game rooms
- **Real-time Updates:** Instant game state synchronization
- **Multiple Games:** Server supports [X] concurrent games
- **Player Management:** Automatic disconnect handling

## 🎓 Learning Outcomes
- Implemented TCP socket communication between Android and Java
- Designed thread-safe message queue system using BlockingQueue
- Built room-based multiplayer architecture
- Handled network error recovery and disconnections

## 📸 Screenshots
See images images folder

## 🤝 Contributors
- **Innocent Mhlongo** - implemented everything

## 📝 Future Improvements
- Add chat functionality
- Deploy server to cloud platform
