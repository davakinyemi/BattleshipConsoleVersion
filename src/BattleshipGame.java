
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Random;
import java.awt.Point;
import java.lang.NumberFormatException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dav
 */
public class BattleshipGame {
        /*
         *   SHIP SIZES
         *   Destroyer: 4
         *   Submarine: 3
         *   Carrier: 4
         *   Assault-ship: 3
         *   Patrol-boat: 2
         */
        static String[] fleet                             = {"Destroyer", 
                                                             "Submarine", 
                                                             "Carrier", 
                                                             "Assault-ship", 
                                                             "Patrol-boat"};
        private static int ship_size;
        //----------------------------------------------------------------------
        private static boolean game_complete = false;
        //----------------------------------------------------------------------
        private static int num_of_ships                   = 5;
        private static int adjusted_grid_size             = 10;
        private static int grid_size                      = adjusted_grid_size + 1;
        //----------------------------------------------------------------------
        private static Random r                           = new Random();        
        static Scanner sc                                 = new Scanner(System.in); 
        //----------------------------------------------------------------------
        private static BattleshipGrid AI_grid             = new BattleshipGrid(grid_size, "AI Grid");
        private static BattleshipGrid AI_grid_for_player  = new BattleshipGrid(grid_size, "AI Grid(Player)");
        private static AI ai = new AI();
        //----------------------------------------------------------------------
        private static BattleshipGrid Player_grid         = new BattleshipGrid(grid_size, "Player Grid");
        //----------------------------------------------------------------------
        private static ArrayList<String> fleet_list;
        private static ArrayList<Battleship> player_ships = new ArrayList<>();
        private static ArrayList<Battleship> AI_ships     = new ArrayList<>(); 
        //----------------------------------------------------------------------
        public static int orientation = 0, width = 0, height = 0, x, y = 0, num_of_orients = 3;        
        
        public static void main(String[] args){
                game_intro();
                
                get_ships();              
                
                player_setup();
                
                setup_AI();
                
                print_ships2();
                
                run_game();
        }
        
        /************************GAME METHODS**********************************/
        
        //----------------------Accessor methods-------------------------------/
        public static int get_grid_size(){
                return grid_size;
        }
        
        public static BattleshipGrid get_Player_grid(){
                return Player_grid;
        }
        
        public static ArrayList<Battleship> get_AI_ships(){
                return AI_ships;
        }
        
        public static BattleshipGrid get_AI_grid(){
                return AI_grid;
        }
        
        public static BattleshipGrid get_AI_grid_for_player(){
                return AI_grid_for_player;
        }
        
        public static int get_num_orients(){
                return num_of_orients;
        }
        
        public static int get_adjusted_grid_size(){
                return adjusted_grid_size;
        }
        
        public static int get_num_ships(){
                return num_of_ships;
        }
        
        public static ArrayList<String> get_fleet_list(){
                return fleet_list;
        }
        
        public static int get_ship_size(){
                return ship_size;
        }
        
        public static ArrayList<Battleship> get_player_ships(){
                return player_ships;
        }
        //---------------------------------------------------------------------/
        
        // for testing purposes, prints out ship coordinates
        public static void print_ships2(){
                out.println("-----------Player ships------------");
                print_ships(player_ships);
                out.println("-----------AI ships----------------");
                print_ships(AI_ships);                
        }
	public static void print_ships(ArrayList<Battleship> ship){
		for(Battleship s : ship){
                        out.println(s.get_name());
                        out.println(s.get_coords());
		}
                
                out.println();
	}
        
        public static void game_intro(){            
                out.println("Welcome to Battleship");                                
        }
        
        public static void get_ships() {
                /*
                 * Chooses the ships that the player and AI will both have.
                 * First five are all different, if num_of_ships > 5, they're
                 * selected randomly
                 */
                fleet_list = new ArrayList<>();

                for (int i = 0; i < num_of_ships; i++) {
                        if(i < 5) {
                                fleet_list.add(fleet[i]);
                        } else {
                                fleet_list.add(fleet[r.nextInt(5)]);
                        }
                }

                for(String s : fleet_list){
                        out.println(s);
                }
        }
        
        // sets ship size based on ship name
        public static void set_ship_size(String ship_name){
                switch (ship_name) {
                        case "Destroyer":
                                ship_size = 4;
                                break;
                        case "Submarine":
                                ship_size = 3;
                                break;
                        case "Carrier":
                                ship_size = 4;
                                break;
                        case "Assault-ship":
                                ship_size = 3;
                                break;
                        case "Patrol-boat":
                                ship_size = 2;
                                break;
                }
        }
        
        // determines which direction ship will face on the grid
        public static void set_ship_orientation(int Y, int X, int orient, int ship_size){
                switch (orient) {
                        case 0: //UP
                                y      = Y - ship_size + 1;
                                width  = 1;
                                height = ship_size;
                                break;
                        case 1: //RIGHT
                                width  = ship_size;
                                height = 1;
                                break;
                        case 2: //DOWN
                                height = ship_size;
                                width  = 1;
                                break;
                        case 3: //LEFT
                                x      = X - ship_size + 1;
                                height = 1;
                                width  = ship_size;
                                break;
                }
        }
        
        // checks if chosen position is available on the grid by comparing the symbols of the positions with the water symbol
        public static boolean is_location_free(int x, int y, int ship_size, BattleshipGrid grid){
                for (int i = 0; i < ship_size; i++) {
                        if (!grid.get_symbol(x, y).equals(grid.water_symbol)) {
                                return false;
                        }
                        if (width == 1) {
                                y++;
                        } else {
                                x++;
                        }
                }
            
                return true;
        }
        
        // resets variables that are change depending on which methods require them
        public static void reset_joint_variables(){
                orientation = 0;
                width       = 0;
                height      = 0;
                x           = 0;
                y           = 0;
        }
        
        // converts guess to 2d array values (e.g. B3 == array[2][3]) 
        public static void get_coord(String guess){
                reset_joint_variables();
                String Y = guess.substring(0, 1);
            
                try{
                        x = Integer.parseInt(guess.substring(1));
                } catch(NumberFormatException e){
                    
                }
                
                for(int a = 0; a < Player_grid.alphabets.length; a++){
                        if(Player_grid.alphabets[a].toLowerCase().equals(Y.toLowerCase())){
                                y = a + 1;
                        }
                }
        }
        
        // this method runs the game by checking if there any living ships on both grids
        public static void run_game(){
                while(game_complete == false){
                        out.println("Your turn...");
                        out.println();
                        run_player();
                        
                        if(!are_ships_alive(AI_grid)){
                                game_complete = true;
                                out.println("Game Over! You Won");
                        } else {
                                out.println("AI's turn...");
                                run_AI();

                                if (!are_ships_alive(Player_grid)) {
                                        game_complete = true;
                                        out.println("Game Over! You Lost!");
                                }
                        }
                }
        }
        
        // used by run_game() to check if there are any ships alive on the grid
        public static boolean are_ships_alive(BattleshipGrid grid){
                for (int j = 0; j < grid_size; j++) {
                        for (int k = 0; k < grid_size; k++) {
                                if (grid.get_symbol(k, j).equals(grid.ship_body_symbol)) {
                                        return true;
                                }
                        }
                }
            
                return false;
        }
        
        // method checks if a ship has been destroyed by by comparing the number of hit symbols of a ship to its size
        public static void set_ship_destroyed(ArrayList<Battleship> ship, BattleshipGrid grid){
                int count = 0;
                for(Battleship s : ship){
                        for(String a : s.get_coords()){
                                get_coord(a);
                                if(grid.get_symbol(x, y).equals(grid.hit_symbol)){
                                        count++;
                                }
                        }
                        if(count == s.get_size()){
                                s.set_destroyed(true);
                                out.println(s.get_name() + " destroyed!");
                        }
                        count = 0;
                }
        }
        /**********************************************************************/
        
        /************************AI METHODS************************************/
        
        // set's up AI
        public static void setup_AI(){
                ai.AI_setup();
        }
        
        // run's ai
        public static void run_AI(){
                ai.AI_guess();
        }       
        
        /**********************************************************************/
        
        /************************PLAYER METHODS********************************/
        
        /*
          asks the player for the position of all ships, it
          adds each ship to the playerShips arraylist and adds the ships
          to the player grid.
        */
        public static void player_setup(){
                Player_grid.print_grid();
                
                String input, ship_name;
                String type = "player";
                
                for(int i = 0; i < num_of_ships; i++){
                        ship_name = fleet_list.get(i);
                        set_ship_size(ship_name);
                                               
                        
                        out.println("Please give a coordinate to hide your " + ship_name + " : (ex. 'A1')");
                        out.println(ship_name + " size: " + ship_size);
                        
                        input = sc.next();
                        
                        get_coord(input);
                        
                        out.println("Which direction would you like your " + ship_name + " to face?:");
                        out.print("Orientation Key:" + "\n" + "0 - up" + "\n" + "1 - right" + "\n" + "2 - down" + "\n" + "3 - left" + "\n");
                        
                        try {
                                orientation = sc.nextInt();                    
                        } catch (InputMismatchException ex){
                                out.println("Invalid position or invalid input for orientation");                    
                        }
                        
                        set_ship_orientation(y, x, orientation, ship_size);
                        
                        try{
                                if(is_location_free(x, y, ship_size, Player_grid)){
                                        player_ships.add(new Battleship(ship_size, ship_name, x, y, width, height)); //add ship to player ships
                                        Player_grid.add_ship(x, y, width, height, ship_size, type, ship_name); //prints ship to grid
                                } else {
                                        out.println("Invalid Location. Try again");
                                        i--;
                                }
                        } catch(Exception e){
                                out.println("Invalid Location. Try again");
                                i--;
                        }
                        Player_grid.print_grid();
                } 
                reset_joint_variables();
        }
        
        // run's player
        public static void run_player(){
                player_guess();
        }
        
        // selects a guess from player
        public static void player_guess(){
                String guess;
                out.println("Enter a guess: (ex. G1)"); 
                
                // check if player gives a valid guess (e.g. "E5")
                do{
                                guess = sc.next();
                } while(!test_player_guess(guess));
                
                // if player guess is repeated, ask for another guess
                while(is_player_guess_repeated(guess)){
                        out.println("You have already guess that. Try again.");
                        do{
                                guess = sc.next();
                        } while(!test_player_guess(guess));
                }
                out.println("player guess: " + guess);
                
                // convert player guess to array value
                get_coord(guess);
                
                // if string value at position guessed equals ship_body_symbol, set it to hit_symbol
                if(AI_grid.get_symbol(x, y).equals(AI_grid.ship_body_symbol)){
                        out.println("HIT!");
                        AI_grid.set_symbol(x, y, AI_grid.hit_symbol);
                        AI_grid_for_player.set_symbol(x, y, AI_grid_for_player.hit_symbol);
                } else {
                        out.println("Miss...");
                        AI_grid.set_symbol(x, y, AI_grid.miss_symbol);
                        AI_grid_for_player.set_symbol(x, y, AI_grid_for_player.miss_symbol);
                }
                
                set_ship_destroyed(AI_ships, AI_grid);
                AI_grid_for_player.print_grid();
        }
        
        // used by player_guess() to determine if player's guess is valid
        public static boolean test_player_guess(String guess){
                String alpha1 = guess.substring(0, 1);
                int numb1     = 0;

                try{
                        numb1  = Integer.parseInt(guess.substring(1));                
                } catch (Exception ex){
                        out.println("Invalid coordinate!");
                        return false;
                }

                int alpha2    = 300;
                int numb2     = 300;

                for(int i = 1; i < grid_size; i++){
                            if(alpha1.toLowerCase().equals(AI_grid.get_grid()[i][0].toLowerCase())){
                                    alpha2 = i;
                            }
                            if(numb1 == Integer.parseInt(AI_grid.get_grid()[0][i])){
                                    numb2  = i;
                            }
                }

                if(alpha2 == 300 || numb2 == 300){
                        out.println("Invalid input. Please try again");
                        return false;
                }
            
                return true;
        }
        
        // used by player_guess() to determine if guess is repeated
        public static boolean is_player_guess_repeated(String guess){
                get_coord(guess);
                
                return (!AI_grid.get_symbol(x, y).equals(AI_grid.water_symbol) && !AI_grid.get_symbol(x, y).equals(AI_grid.ship_body_symbol));                
        }
        /**********************************************************************/
}