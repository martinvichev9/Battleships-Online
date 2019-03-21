# Battleships-Online
A course project for the elective discipline **"Modern Java Technologies" at FMI**

# What is that ?
Battleships-Online is a client-server console application, which presents a game between two players.
-  Map's size is **10x10** 
-  Before game starts, every player places all his ships on map horizontally or vertically
-  Count of ships for every player is 10 (1 x **5 cells**, 2 x **4 cells**, 3 x **3 cells** and 4 x **2 cells**) 
-  The main objective for every player is to hit some opponent's ship, when shooting 
-  The player who is on turn chooses some **cell and shoot it**. If he hits some opponent ship's cell and that cell was **the last one**, the ship is considered as **sunk**
- Game ends when all ships of some player are **sunk**

# Game's commands:
```
-  connect "username" - connects to server
-  create-game "game-name" - creates a new game 
-  list-games - lists all current games with information about if game is started, how many players there are , etc...
-  saved-games - lists all saved games
-  save-game - saves game's state in a text file, which name is formed as: game-name_player1-name_VS_player2-name_.txt
-  join-game "game-name" - starts "game-name" if that game exists and there is only one player. If name is missing then is joined to a random game
-  load-game "game-name" - !!! NOT DEFINED YET !!! - loads the saved game "game-name" and joins to it 
-  delete-game "game-name" - deletes "game-name" game
-  help - prints information about commands
-  exit - exits from server
