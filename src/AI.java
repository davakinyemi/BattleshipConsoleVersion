
import java.util.Random;
import static java.lang.System.out;
import java.util.ArrayList;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dav
 */
public class AI {
        public static int AI_x = 0, AI_y = 0, hit_X = 0, hit_Y = 0;
        String AI_Y, AI_X; 
        private static ArrayList<Integer> saved_AI_orient1 = new ArrayList<>();
        private static ArrayList<Integer> saved_AI_orient2 = new ArrayList<>();
        private static Random rd                           = new Random();
        public static boolean guess_was_hit                = false;
        public static boolean next_guess_tried             = false;
        public static int orient;
        public boolean orient_success                      = false;
        public static boolean reverse_orientation          = false;
        public static int check_orient_based_guess         = 0;
        public static int original_x, original_y;
        public static boolean orient_failed                = false;
        
        
        
        // method to setup ai
        public void AI_setup(){
                String input, ship_name;
                String type = "ai";
                
                for(int i = 0; i < BattleshipGame.get_num_ships(); i++){
                        ship_name = BattleshipGame.get_fleet_list().get(i);
                        BattleshipGame.set_ship_size(ship_name);
                        
                        // get and test random positions and orientation to hide ship
                        BattleshipGame.x = rd.nextInt(BattleshipGame.get_grid_size());
                        BattleshipGame.y = rd.nextInt(BattleshipGame.get_grid_size());
                        BattleshipGame.orientation = rd.nextInt(4);
                        
                        BattleshipGame.set_ship_orientation(BattleshipGame.x, BattleshipGame.y, 
                        BattleshipGame.orientation, BattleshipGame.get_ship_size());
                        
                        try{
                                if(BattleshipGame.is_location_free(BattleshipGame.x, BattleshipGame.y, 
                                   BattleshipGame.get_ship_size(), BattleshipGame.get_AI_grid())){
                                    
                                        BattleshipGame.get_AI_ships().add(new Battleship(BattleshipGame.get_ship_size(), ship_name, BattleshipGame.x, BattleshipGame.y, BattleshipGame.width, BattleshipGame.height)); 
                                        BattleshipGame.get_AI_grid().add_ship(BattleshipGame.x, BattleshipGame.y, BattleshipGame.width, BattleshipGame.height, BattleshipGame.get_ship_size(), type, ship_name); 
                                } else {
                                        i--;
                                }
                        } catch(Exception e){
                                i--;
                        }
                }
                // cheat code
                BattleshipGame.get_AI_grid().print_grid();
                //
                
                // reveal a grid to the player without showing where ships are hidden
                BattleshipGame.get_AI_grid_for_player().print_grid();
                BattleshipGame.reset_joint_variables();
        }
        
        // used by AI_guess() to generate a random guess
        public void random_guess(){
                get_random_guess();

                while(!improve_AI_guess()){
                        get_random_guess();                                                                                             
                }
        }
        
        // extension of random_guess()
        public static void get_random_guess(){
                do{
                        AI_x = rd.nextInt(BattleshipGame.get_grid_size());
                        AI_y = rd.nextInt(BattleshipGame.get_grid_size());               
                } while(is_AI_guess_repeated());
        }
        
        // generates a guess based on the values of the conditional variables
        public void AI_guess(){
                if(orient_success == false){
                        if(orient_failed == true){
                               set_next_guess(); 
                        } else if(next_guess_tried == false && check_ship_destroyed()){
                               random_guess(); 
                        } else {
                               set_next_guess();
                        }                
                        AI_guess2();
                } else {
                        if(reverse_orientation == true){
                                reverse_orient();
                                AI_guess2();
                        } else {
                                set_orient_based_guess();
                                AI_guess2();
                        }
                }
        }
        
        // extension on AI_guess() to act on guesses made by AI
        public void AI_guess2(){
                // debugging code (converts array values to string positions (e.g. array[2][3] == "B3")
                AI_Y = BattleshipGame.get_Player_grid().get_symbol(0, AI_y);
                AI_X = BattleshipGame.get_Player_grid().get_symbol(AI_x, 0);
                out.println("AI guess: " + AI_Y + AI_X);
                //
                
                // if guess corresponds to ship position, set the symbol to hit_symbol
                if (BattleshipGame.get_Player_grid().get_symbol(AI_x, AI_y).equals(BattleshipGame.get_Player_grid().ship_body_symbol)) {
                        System.out.println("AI hit your ship...");
                        BattleshipGame.get_Player_grid().set_symbol(AI_x, AI_y, BattleshipGame.get_Player_grid().hit_symbol); 
                        
                        // check if the ship has been destroyed and act accordingly
                        BattleshipGame.set_ship_destroyed(BattleshipGame.get_player_ships(), BattleshipGame.get_Player_grid());
                        
                        // next_guess_tried is only true when making guesses based on orientation, if previous guess was a hit
                        if(next_guess_tried == true && compare_ships()){
                                // if orientation resulted in another successful guess, set it to true
                                orient_success = true;
                                orient_failed  = false;
                        } else {
                                orient_success = false;
                                if(next_guess_tried == true){
                                        remove_orient();
                                }
                        }
                        
                        // reset all conditional variables if ship has been destroyed
                        if(check_ship_destroyed_based_orient()){
                                next_guess_tried = false;
                                orient_success = false;
                                reverse_orientation = false;
                        }
                                                
                } else{
                        System.out.println("AI Missed...");
                        BattleshipGame.get_Player_grid().set_symbol(AI_x, AI_y, BattleshipGame.get_Player_grid().miss_symbol);
                        
                        // if guess was made based on previous successful guess and orientation, and it it's a miss, remove the orientation
                        // from the arraylist it is saved in
                        if(next_guess_tried == true && orient_success != true){
                                remove_orient();                         
                        }
                        
                        if(orient_success != true){
                                next_guess_tried = false;
                        }
                        
                        // if the orientation is correction, but the guess is unsuccessful, reverse the orientation
                        if(orient_success == true){
                                reverse_orientation = true;
                        }
                }
                
                BattleshipGame.get_Player_grid().print_grid();
        }
        
        // used by AI_guess2() to remove unsuccessful orientation value from arraylist
        public static void remove_orient(){
                for(int i = 0; i < saved_AI_orient2.size(); i++){
                        if(saved_AI_orient2.get(i) == orient){
                                saved_AI_orient2.remove(i);
                        }
                }
        }
        
        // determines if a guess is repeated
        public static boolean is_AI_guess_repeated(){
                return (!BattleshipGame.get_Player_grid().get_symbol(AI_x, AI_y).equals(BattleshipGame.get_Player_grid().water_symbol) && 
                        !BattleshipGame.get_Player_grid().get_symbol(AI_x, AI_y).equals(BattleshipGame.get_Player_grid().ship_body_symbol));                
        }
        
        // improves AI guess by checking if it is possible for a ship to be stored in position, by checking the values of nearby 
        // cells using orientation values. Also stores possible orientation values to saved_AI_orient2 arraylist to use in next guesses
        public static boolean improve_AI_guess(){
                saved_AI_orient1.clear();
                saved_AI_orient2.clear();
                String adjacent_cell;
                
                for(int i = 0; i <= BattleshipGame.get_num_orients(); i++){
                        if(i == 0){
                                adjacent_cell = BattleshipGame.get_Player_grid().get_symbol(AI_x, AI_y - 1);
                                if(check_symbol(adjacent_cell)){
                                        saved_AI_orient1.add(i);
                                        if(!adjacent_cell.equals(BattleshipGame.get_Player_grid().hit_symbol)){
                                                saved_AI_orient2.add(i);
                                        }
                                }
                                
                        }
                        if(i == 1){
                               if(AI_x < BattleshipGame.get_adjusted_grid_size()){
                                        adjacent_cell = BattleshipGame.get_Player_grid().get_symbol(AI_x + 1, AI_y);
                                        if(check_symbol(adjacent_cell)){
                                                saved_AI_orient1.add(i);
                                                if(!adjacent_cell.equals(BattleshipGame.get_Player_grid().hit_symbol)){
                                                        saved_AI_orient2.add(i);
                                                }
                                        }
                                        
                               } 
                        }
                        if(i == 2){
                               if(AI_y < BattleshipGame.get_adjusted_grid_size()){
                                        adjacent_cell = BattleshipGame.get_Player_grid().get_symbol(AI_x, AI_y + 1);
                                        if(check_symbol(adjacent_cell)){
                                                saved_AI_orient1.add(i);
                                                if(!adjacent_cell.equals(BattleshipGame.get_Player_grid().hit_symbol)){
                                                        saved_AI_orient2.add(i);
                                                }
                                        }
                                        
                               } 
                        }
                        if(i == 3){
                                adjacent_cell = BattleshipGame.get_Player_grid().get_symbol(AI_x - 1, AI_y);
                                if(check_symbol(adjacent_cell)){
                                        saved_AI_orient1.add(i);
                                        if(!adjacent_cell.equals(BattleshipGame.get_Player_grid().hit_symbol)){
                                                saved_AI_orient2.add(i);
                                        }
                                }
                                
                        }
                }
                
                if(saved_AI_orient1.size() == 0){
                        return false;
                }
                
                return true;
        }
        
        // used by improve_AI_guess to get a boolean value based on value of cell on grid
        public static boolean check_symbol(String cell){
            
                if(cell.equals(BattleshipGame.get_Player_grid().water_symbol) || 
                   cell.equals(BattleshipGame.get_Player_grid().ship_body_symbol) || 
                   cell.equals(BattleshipGame.get_Player_grid().hit_symbol)){
                        return true;
                }
            
                return false;
        }
        
        // checks if there are any successful guesses made but whose ships haven't been destroyed yet 
        public static boolean check_ship_destroyed(){
                String X, Y;
                String coord;
                
                for (int j = 0; j < BattleshipGame.get_grid_size(); j++) {
                        for (int k = 0; k < BattleshipGame.get_grid_size(); k++) {
                                if (BattleshipGame.get_Player_grid().get_symbol(k, j).equals(BattleshipGame.get_Player_grid().hit_symbol)) {
                                        Y = BattleshipGame.get_Player_grid().get_symbol(0, j);
                                        X = BattleshipGame.get_Player_grid().get_symbol(k, 0); 
                                        coord = Y + X;
                                        
                                        for(Battleship s : BattleshipGame.get_player_ships()){
                                                for(String a : s.get_coords()){
                                                        if(coord.equals(a)){
                                                                if(s.get_destroyed() == false){
                                                                        hit_X      = k;
                                                                        hit_Y      = j;
                                                                        original_x = k;
                                                                        original_y = j;
                                                                        return false;                                                                    
                                                                }
                                                        }
                                                }                                                        
                                        }
                                }
                        }
                }            
                return true;
        }
        
        // generates guess based on values in saved_AI_orient2
        public static void set_next_guess(){
                out.println("saved_AI_orient2 size: " + saved_AI_orient2.size());
                int x = rd.nextInt(saved_AI_orient2.size());
                int i = saved_AI_orient2.get(x);
                
                if(orient_failed == true){
                        hit_X = original_x;
                        hit_Y = original_y;
                }
                
                                if(i == 0){
                                        AI_x             = hit_X;
                                        AI_y             = hit_Y - 1; 
                                        hit_Y--;
                                        next_guess_tried = true;
                                        orient           = i;
                                }
                                if(i == 1){
                                        AI_x             = hit_X + 1;
                                        hit_X++;
                                        AI_y             = hit_Y;
                                        next_guess_tried = true;
                                        orient           = i;
                                }
                                if(i == 2){
                                        AI_x             = hit_X;
                                        AI_y             = hit_Y + 1;
                                        hit_Y++;
                                        next_guess_tried = true;
                                        orient           = i;
                                }
                                if(i == 3){
                                        AI_x             = hit_X - 1;
                                        hit_X--;
                                        AI_y             = hit_Y;
                                        next_guess_tried = true;
                                        orient           = i;
                                }
                                        
        }
        
        // generates guess based on successfull orientation value
        public static void set_orient_based_guess(){
                if(orient == 0){
                                        AI_x = hit_X;
                                        AI_y = hit_Y - 1; 
                                        hit_Y--;
                                }
                                if(orient == 1){
                                        AI_x = hit_X + 1;
                                        hit_X++;
                                        AI_y = hit_Y;
                                }
                                if(orient == 2){
                                        AI_x = hit_X;
                                        AI_y = hit_Y + 1;
                                        hit_Y++;
                                }
                                if(orient == 3){
                                        AI_x = hit_X - 1;
                                        hit_X--;
                                        AI_y = hit_Y;
                                }
                                
                                // if guess is repeated, or if x and y values are greater than the grid then reverse orientation
                                if(is_AI_guess_repeated() || AI_x > BattleshipGame.get_adjusted_grid_size() || 
                                   AI_y > BattleshipGame.get_adjusted_grid_size()){
                                        reverse_orient();
                                }
        }
        
        // checks if ship has been destroyed based on first hit made on ship
        public boolean check_ship_destroyed_based_orient(){
            String Y, X;
            int check = 0;
            String coord;
            
                if(orient_success == true){
                        Y = BattleshipGame.get_Player_grid().get_symbol(0, original_y);
                        X = BattleshipGame.get_Player_grid().get_symbol(original_x, 0); 
                        coord = Y + X;
                        
                        for(Battleship s : BattleshipGame.get_player_ships()){
                                for(String a : s.get_coords()){
                                        if(coord.toLowerCase().equals(a.toLowerCase())){
                                                if(s.get_destroyed() == true){
                                                        check = 1;
                                                }
                                        }
                                }                                                        
                        }
                }
                
                if(check == 1){
                        return true;
                }
                return false;
        }
        
        // method to reverse orientation based on successful orientation values
        public static void reverse_orient(){
                if(orient == 0){                                        
                        AI_x = original_x;
                        AI_y = original_y + 1;
                        original_y++;
                }
                if(orient == 1){                                     
                        AI_x = original_x - 1;
                        original_x--;
                        AI_y = original_y;
                }
                if(orient == 2){
                        AI_x = original_x;
                        AI_y = original_y - 1; 
                        original_y--;                                        
                }
                if(orient == 3){
                        AI_x = original_x + 1;
                        original_x++;
                        AI_y = original_y;
                }
        }
        
        // extention of compare_ships(). It returns the name of the ship based on coordinate
        public static String compare_ships2(String coord){
                String name = "";
                
                for(Battleship s : BattleshipGame.get_player_ships()){
                        for(String a : s.get_coords()){
                                if(coord.toLowerCase().equals(a.toLowerCase())){
                                        name = s.get_name();
                                }
                        }                                                        
                }
                return name;
        }
        
        // method to keep focus on one ship until it is destroyed by comparing the original hit (first hit) made with any consecutive hits 
        public boolean compare_ships(){
                String Y1, X1, Y2, X2;
                int check = 0;
                String coord1, coord2, name1, name2;

                Y1     = BattleshipGame.get_Player_grid().get_symbol(0, AI_y);
                X1     = BattleshipGame.get_Player_grid().get_symbol(AI_x, 0); 
                coord1 = Y1 + X1;
                name1  = compare_ships2(coord1);

                Y2     = BattleshipGame.get_Player_grid().get_symbol(0, original_y);
                X2     = BattleshipGame.get_Player_grid().get_symbol(original_x, 0); 
                coord2 = Y2 + X2;
                name2  = compare_ships2(coord2);

                if(name1.equals(name2)){
                        return true;
                } 

                orient_failed = true;
                return false;                
        }
}
