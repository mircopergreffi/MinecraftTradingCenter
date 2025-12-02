# Minecraft Trading Centers Mod

A Minecraft Forge mod for version 1.20.1 that allows players to create and participate in a centralized trading system using emeralds.

## Features

- **Trading Laptop**: A special item that opens the Trading Center GUI
- **Create Trades**: Players can create trades by specifying:
  - Wanted: Amount of emeralds per trade
  - Given: Amount and type of items to give per trade
  - The number of available trades is automatically calculated based on items deposited
- **Buy Trades**: Players can browse available trades and purchase them with emeralds
- **Bank System**: Sellers' emeralds are stored in a bank, which can be withdrawn through the GUI
- **Persistent Storage**: All trades and bank balances are saved to the world

## Building

1. Make sure you have Java 17 installed
2. Run `./gradlew build` (Linux/Mac) or `gradlew.bat build` (Windows)
3. The mod JAR will be in `build/libs/`

## Installation

1. Install Minecraft Forge 1.20.1 (version 47.2.0 or later)
2. Place the mod JAR in your `mods` folder
3. Launch Minecraft

## Usage

1. Obtain a Trading Laptop (you may need to add it via creative mode or commands)
2. Right-click with the Trading Laptop to open the Trading Center GUI
3. To create a trade:
   - Enter the emerald amount you want per trade in "Wanted"
   - Enter the item count you'll give per trade in "Given"
   - Have the items in your inventory (must be a multiple of the "Given" amount)
   - Click "Create Trade"
4. To buy a trade:
   - Click on a trade in the list to select it
   - Enter the multiplier (how many times you want to execute the trade)
   - Have enough emeralds in your inventory
   - Click "Buy"
5. To withdraw emeralds:
   - Enter the amount you want to withdraw
   - Click "Withdraw Emeralds"

## Technical Details

- Uses Minecraft's SavedData system for persistent storage
- Network packets handle client-server communication
- Trades are stored server-side and synchronized to clients
- Bank balances are tracked per player

## License

MIT License

