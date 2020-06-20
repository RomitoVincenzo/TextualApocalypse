/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adventure;

import games.TextualApocalypse;
import parser.Parser;
import parser.ParserOutput;
import type.AdvObject;
import type.CommandType;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ATTENZIONE: l'Engine e' molto spartano, in realta'  demanda la logica alla
 * classe che implementa GameDescription e si occupa di gestire I/O sul
 * terminale.
 *
 */

public class Engine {			

    private  GameDescription game;			//gioco
    
    private final Parser parser;

    public Engine(GameDescription game) {
        this.game = game;
        try {
            this.game.init();
        } catch (Exception ex) {
            System.err.println(ex);
        }
        parser = new Parser();
    }

    public boolean run() throws InterruptedException, IOException {
    	Scanner scanner = new Scanner(System.in);
    	String command = "0";
        game.firstScreen();
    	game.menu();
    	do {
    		System.out.print("\n"+"Fai la tua scelta (1..4) ? ");
    		command = scanner.nextLine();
    	}while (!command.equals("1") && !command.equals("2")  && !command.equals("3")  && !command.equals("4") );
    	
    	switch(Integer.parseInt(command)) {
	    	case 1:
	    		//nuova partita
	            //game.prologue();
	            iniziaGioco(command,scanner);
	    		break;
	    	case 2:
	    		//carica partita
	    		FileInputStream inFile= new FileInputStream("TA.dat");
	    		ObjectInputStream inStream= new ObjectInputStream(inFile);
			try {
				game = (TextualApocalypse)inStream.readObject();
	    		iniziaGioco(command,scanner);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    		break;
	    	case 3:
	    		game.instructions();
	    		game.setDead(true);
	    		break;
	    	case 4 : System.exit(0);
	    		//exit
	    		break;
    	}
    	return game.isDead();
    }
    
    public void iniziaGioco(String command,Scanner scanner) {
	    game.formattedString(game.getCurrentRoom().getDescription()+"\n");
		for (AdvObject obj : game.getCurrentRoom().interactiveObjects()) 
			System.out.println("Vedo "+obj.getArticle()+" "+obj.getName());
	    System.out.print("\n  Cosa devo fare ? ");
	    while (!game.isDead() && scanner.hasNextLine() ) {
	    	System.out.println();
	        command = scanner.nextLine();
	        List<AdvObject> list = new ArrayList();
	        list.addAll(game.getCurrentRoom().getObjects());
	        list.addAll(game.getCurrentRoom().getContainedObjects());
	        //list.addAll(game.getInventory().getList());
	        ParserOutput p = parser.parse(command, game.getCommands(),list, game.getInventory().getList());
	        if (p.getCommand() != null && p.getCommand().getType() == CommandType.END) {
	            System.out.println("Addio!\n\n");
	            game.setDead(true);
	            break;
	        } else {
	            game.nextMove(p, System.out);
	        }
	        if(!game.isDead())
	        	System.out.print("\n  Cosa devo fare ? ");
	    }
    }

    public static void main(String[] args) throws Exception {
        Engine engine = new Engine(new TextualApocalypse());
        boolean restart;
        do {
        	engine.game.init();
        	restart=engine.run();
        }while(restart);
        
    }
}




	



























