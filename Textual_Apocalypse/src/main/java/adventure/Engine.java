
package adventure;

import games.TextualApocalypse;
import parser.Parser;
import parser.ParserOutput;
import type.AdvObject;
import type.CommandType;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Engine {			

    private  GameDescription game;		    
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

    public boolean run(){
    	Scanner scanner = new Scanner(System.in);
    	String command = "0";
    	game.menu();
    	do {
    		System.out.print("\n"+"Fai la tua scelta (1..4) ? ");
    		command = scanner.nextLine();
    	}while (!command.equals("1") && !command.equals("2")  && !command.equals("3")  && !command.equals("4") );
    	
    	switch(Integer.parseInt(command)) {
	    	case 1:
	    		//nuova partita
	            game.prologue();
	            startGame(command,scanner);
	    		break;
	    	case 2:
	    		//carica partita
			try {
	    		FileInputStream inFile= new FileInputStream("TA.dat");
	    		ObjectInputStream inStream= new ObjectInputStream(inFile);
				game = (TextualApocalypse)inStream.readObject();
				System.out.println("Salvataggio caricato con successo\n");
	    		startGame(command,scanner);
			} catch (Exception e) {
				System.out.println("File di salvataggio non trovato\n");
				game.setDead(true);
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
    
    public void startGame(String command,Scanner scanner) {
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
        engine.game.firstScreen();
        boolean restart;
        do {
        	engine.game.init();
        	restart=engine.run();
        }while(restart);
        
    }
}




	



























