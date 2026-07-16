# Multiplayer Platformer

A basic, but difficult, platformer game with levels stored in JSON for easy editing. The server uses sockets to communicate information between itself and all connected clients. First networking project!

## Features

- Real-time multiplayer over TCP sockets — up to 10 players per server
- Levels defined entirely in JSON ([`levels.json`](src/main/resources/levels.json)), including walls, death boxes, checkpoints, trampolines, ice, and finish lines
- Players advance to the next level together once everyone reaches the finish line
- In-game chat between players
- Zoom and pan camera controls

## Tech Stack

- Java 21
- Maven
- [`org.json`](https://mvnrepository.com/artifact/org.json/json) for level parsing
- Java Swing for the client GUI
- Raw `java.net.Socket` / `ServerSocket` for client-server communication

## Getting Started

### Build

```bash
mvn package
```

This produces two runnable jars in `target/`:

- `SimpleMultiplayerGameServer-server.jar` — the game server
- `SimpleMultiplayerGameServer-client.jar` — the game client

### Run the server

```bash
java -jar target/SimpleMultiplayerGameServer-server.jar
```

The server listens on port `8888`.

### Run a client

```bash
java -jar target/SimpleMultiplayerGameServer-client.jar
```

Enter the server's IP address and port (`8888`) in the client window to connect.

## Controls

| Action        | Key(s)              |
|---------------|----------------------|
| Move          | `WASD` / Arrow keys  |
| Jump          | `W` / `Up` / `Space` |
| Zoom          | Mouse scroll         |
| Pan camera    | Mouse drag           |
| Chat          | `Enter`              |

## Editing Levels

Levels live in [`src/main/resources/levels.json`](src/main/resources/levels.json) as an array of levels, each an array of objects. Every object has a `type` along with the fields it needs:

- `Spawn` — `x`, `y` starting position for the level
- `Wall`, `Checkpoint`, `FinishLine`, `DeathBox`, `Trampoline`, `Ice` — `x`, `y`, `w`, `h`, `color`, and an optional `equation` for moving platforms
- `Tip` — `x`, `y`, `w`, `h`, `message` shown when a player enters the area

Add a new level by appending another array to the top-level JSON array — no code changes required.

## Project Structure

```
src/main/java/
├── client/
│   ├── game/       # Game loop, rendering, server connection handling
│   ├── objects/    # Player, Box, Level, and other game objects
│   └── utility/    # Shared helpers (messages, vectors, ping)
└── server/         # GameServer and per-client connection handling
src/main/resources/
└── levels.json     # Level definitions
```
