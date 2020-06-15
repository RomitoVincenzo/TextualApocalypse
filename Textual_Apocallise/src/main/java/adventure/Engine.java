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

import java.io.IOException;
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

    private final GameDescription game;			//gioco
    
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

    public void run() {
    	//game.firstScreen();
    	
    	//game.menu();
    	//logica menu
        System.out.println(game.getCurrentRoom().getDescription());
        System.out.print("\n Cosa devo fare ? ");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            List<AdvObject> list =  game.getCurrentRoom().getObjects();
            list.addAll(game.getCurrentRoom().getContainedObjects());
            ParserOutput p = parser.parse(command, game.getCommands(),list, game.getInventory());
            if (p.getCommand() != null && p.getCommand().getType() == CommandType.END) {
                System.out.println("Addio!");
                break;
            } else {
                game.nextMove(p, System.out);
            }
            System.out.print("\n Cosa devo fare ? ");
            
        }
    }

    /**
     * @param args the command line arguments
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        Engine engine = new Engine(new TextualApocalypse());
        engine.run();
    }

}

































