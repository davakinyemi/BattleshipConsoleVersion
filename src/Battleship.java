/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dav
 */
import java.awt.Point;
import java.util.ArrayList;

public class Battleship {
        // Instance variables
	private int Size, x, y, width, height;
	private String Name;	
        private ArrayList<Point> coords;	
	private boolean destroyed;
        private ArrayList<String> ship_coords;
	
        // Constructor
	public Battleship(int size, String name, int x, int y, int width, int height){
		Size        = size;
		Name        = name;
                this.x      = x;
                this.y      = y;
                this.width  = width;
                this.height = height;
		destroyed   = false;
                ship_coords = new ArrayList<>(size);
	}
        
        //Update methods
        public void set_destroyed(boolean condition){
                destroyed = condition;
        }
        
        // Accessor methods
        public int get_size(){
                return Size;
        }
        
        public String get_name(){
                return Name;
        }
        
        public boolean get_destroyed(){
                return destroyed;
        }
        
        public void set_coords(String coords){
                ship_coords.add(coords);
        }
        
        public ArrayList<String> get_coords(){
                return ship_coords;
        }
}