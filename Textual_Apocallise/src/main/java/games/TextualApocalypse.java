/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package games;

import adventure.GameDescription;
import parser.ParserOutput;
import type.AdvObject;
import type.AdvObjectContainer;
import type.Command;
import type.CommandType;
import type.Room;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

/**
 * ATTENZIONE: La descrizione del gioco Ã¨ fatta in modo che qualsiasi gioco
 * debba estendere la classe GameDescription. L'Engine Ã¨ fatto in modo che posso
 * eseguire qualsiasi gioco che estende GameDescription, in questo modo si
 * possono creare piÃ¹ gioci utilizzando lo stesso Engine.
 *
 * Diverse migliorie possono essere applicati: - la descrizione del gioco
 * potrebbe essere caricate da file o da DBMS in modo da non modificare il
 * codice sorgente - l'utilizzo di file e DBMS non Ã¨ semplice poichÃ© all'interno
 * del file o del DBMS dovrebbe anche essere codificata la logica del gioco
 * (nextMove) oltre alla descrizione di stanze, oggetti, ecc...
 *
 * @author pierpaolo
 */
public class TextualApocalypse extends GameDescription {

    @Override
    public void init() throws Exception {    	
    		//prologue();
    		try{
	    			Properties dbprops = new Properties();
	    			dbprops.setProperty("user", "user");
	    			dbprops.setProperty("password", "1234");
	    			ResultSet rs1;
	    			ResultSet rs2;
	    			ResultSet rs3;
	    			Connection conn = DriverManager.getConnection("jdbc:h2:./resources/db/TA", dbprops);
	                PreparedStatement pstm = conn.prepareStatement("SELECT * FROM ROOM");
	                rs1 = pstm.executeQuery();
                while(rs1.next())
                {
	                 Room room = new Room(rs1.getInt(1), rs1.getString(2), rs1.getString(3), rs1.getBoolean(4));
	                 room.setSouth(rs1.getInt(5));
	                 room.setNorth(rs1.getInt(6));
	                 room.setEast(rs1.getInt(7));
	                 room.setWest(rs1.getInt(8));
	                 room.setUp(rs1.getInt(9));
	                 room.setDown(rs1.getInt(10));
	                 PreparedStatement pstm2 = conn.prepareStatement("SELECT * FROM Object WHERE Room=? AND Container is null");
                	 pstm2.setInt(1,rs1.getInt(1));
                	 rs2 = pstm2.executeQuery();
                	 while(rs2.next())
                	 {
	                	 if(rs2.getBoolean(11)) {
	                		 //container
	                		 String[] parts = rs2.getString(4).split(",");	
	                		 Set<String> set = new HashSet(Arrays.asList(parts));
	                		 AdvObjectContainer cont = new AdvObjectContainer(rs2.getInt(1), rs2.getString(2), rs2.getString(3), set,rs2.getBoolean(5),rs2.getBoolean(6),
	                				                                          rs2.getBoolean(7),rs2.getBoolean(8),rs2.getBoolean(9),rs2.getString(13),rs2.getString(14));
	     	                 PreparedStatement pstm3 = conn.prepareStatement("SELECT * FROM Object WHERE Container=?");
    	                	 pstm3.setInt(1,rs2.getInt(1));
    	                	 rs3 = pstm3.executeQuery();
    	                	 while(rs3.next()){
		                	     String[] parts2 = rs3.getString(4).split(",");	
		                	     Set<String> set2 = new HashSet(Arrays.asList(parts2));
	    	                	 cont.add(new AdvObject(rs3.getInt(1), rs3.getString(2), rs3.getString(3), set2,rs3.getBoolean(5),rs3.getBoolean(6),
          				               rs3.getBoolean(7),rs3.getBoolean(8),rs3.getBoolean(9),rs3.getString(13),rs3.getString(14)));
	    	    			 }
	    	                 rs3.close();
	    	                 pstm3.close();
	                		 room.getObjects().add(cont);
	                	
	                	 } else if(rs2.getInt(10)==0) {
	                		 //non container e non contenuto
	                	     String[] parts3 = rs2.getString(4).split(",");	
	                	     Set<String> set3 = new HashSet(Arrays.asList(parts3));
	                		 room.getObjects().add(new AdvObject(rs2.getInt(1), rs2.getString(2), rs2.getString(3), set3,rs2.getBoolean(5),rs2.getBoolean(6),
	                				               rs2.getBoolean(7),rs2.getBoolean(8),rs2.getBoolean(9),rs2.getString(13),rs2.getString(14)));
	                	 }   	 
	                 }
	                 rs2.close();
	                 pstm2.close();
	                 getRooms().add(room);	
                }
                rs1.close();
                pstm.close();
           
                PreparedStatement pstm4 = conn.prepareStatement("SELECT * FROM COMMANDS");
    			ResultSet rs4 = pstm4.executeQuery();
	           	while(rs4.next()){
	        	    String[] parts4 = rs4.getString(2).split(",");	
	        	    Set<String> set4 = new HashSet(Arrays.asList(parts4));
	        	    getCommands().add(new Command(Enum.valueOf(CommandType.class, rs4.getString(3)),rs4.getString(1),set4));
	           	}
	            rs4.close();
	            pstm4.close();
	            //set starting room
	            setCurrentRoom(roomById(1));
	            getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
    		}catch (SQLException ex){
    			System.err.println(ex.getSQLState() + ": " + ex.getMessage());
    		}
    	}   
    
    @Override
    public void nextMove(ParserOutput p, PrintStream out) {
        if (p.getCommand() == null) {
            out.println("Non ho capito cosa devo fare! Prova con un altro comando.");
        } else {
            //move
            boolean noroom = false;
            boolean move = false;
            if (p.getCommand().getType() == CommandType.NORD) {
                if (getCurrentRoom().getNorth() != 0) {
                    setCurrentRoom(roomById(getCurrentRoom().getNorth()));
                    getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                    move = true;
                } else {
                    noroom = true;
                }
            } else if (p.getCommand().getType() == CommandType.SOUTH) {
                if (getCurrentRoom().getSouth() != 0) {
                	setCurrentRoom(roomById(getCurrentRoom().getSouth()));
                	getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                    move = true;
                } else {
                    noroom = true;
                }
            } else if (p.getCommand().getType() == CommandType.EAST) {
                if (getCurrentRoom().getEast() != 0) {
                	setCurrentRoom(roomById(getCurrentRoom().getEast()));
                	getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                    move = true;
                } else {
                    noroom = true;
                }
            } else if (p.getCommand().getType() == CommandType.WEST) {
                if (getCurrentRoom().getWest() != 0) {
                	setCurrentRoom(roomById(getCurrentRoom().getWest()));
                	getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                    move = true;
                } else {
                    noroom = true;
                }    
            } else if (p.getCommand().getType() == CommandType.INVENTORY) {
            	if(getInventory().isEmpty())
            		out.println("Il inventario e' vuoto ");
            	else
            	{
	                out.println("Nel tuo inventario ci sono: ");
	                for (AdvObject o : getInventory()) {
	                    out.println(o.getName());
	                    if(o.getSpecificState() != null)
	                    	out.println(" "+o.getSpecificState());
	                }
                }
            } else if (p.getCommand().getType() == CommandType.LOOK_AT && p.getObject() == null) {
            	//out.println(getCurrentRoom().getDescription()+"\n");
            	formattedString(getCurrentRoom().getDescription());
            	for (AdvObject obj : getCurrentRoom().interactiveObjects()) {
            		out.println("Vedo "+obj.getArticle()+" "+obj.getName());
            		}
            } else if (p.getCommand().getType() == CommandType.LOOK_AT && p.getObject() != null) {
            	if (objectInInventory(p.getObject()) || getCurrentRoom().objectInRoom(p.getObject())) {
            		//GUARDA OGGETTI IN STANZA O IN INVENTARIO
            		formattedString(p.getObject().getDescription());
            	} else if(getCurrentRoom().objectContainer(p.getObject()) != null) {
            		if(getCurrentRoom().objectContainer(p.getObject()).isOpen()) {
            			formattedString(p.getObject().getDescription());
            		}else out.println("Non vedo questo oggetto");
            	} else out.println("Non vedo questo oggetto");    		
            } else if (p.getCommand().getType() == CommandType.PICK_UP) {
                if (p.getObject() != null && getCurrentRoom().objectInRoom(p.getObject())) {
                    if (p.getObject().isPickupable()) {
                		if(p.getObject().getId()==10) {
                			slowPrint("Rimuovi l'asse e un non morto ti affera dalla gola scaravendatoti fuori dalla finestra.\n" + 
                							  "In un men che non si dica vieni circondato da un'orda di zombi che ti riduce a brandelli.\n");
                			end(out);
                		} else if((p.getObject().getId()==10)) {
                			//////
                		}
                        getInventory().add(p.getObject());
                        getCurrentRoom().getObjects().remove(p.getObject());
                        out.println("Hai raccolto: " + p.getObject().getName());
                    }
                } else if (p.getObject() != null && getCurrentRoom().objectContainer(p.getObject()) != null) {
                	if(getCurrentRoom().objectContainer(p.getObject()).isOpen()) {
                		if (p.getObject().isPickupable()) {
                            getInventory().add(p.getObject());
                            getCurrentRoom().getObjects().remove(p.getObject());
                            out.println("Hai raccolto: " + p.getObject().getName());
                		}
                	}
                } else {
                    out.println("Non c'e' niente da raccogliere qui.");
                }
            } else if (p.getCommand().getType() == CommandType.OPEN) {     
                if (p.getObject() == null && p.getInvObject() == null) {
                    out.println("Non c'e' niente da aprire qui.");
                } else {
                    if (p.getObject() != null) {
                        if (p.getObject().isOpenable() && p.getObject().isOpen() == false) {
                            if (p.getObject() instanceof AdvObjectContainer) {
                                p.getObject().setOpen(true);
                                out.println("Hai aperto: " + p.getObject().getName());
                                AdvObjectContainer c = (AdvObjectContainer) p.getObject();
                                if (!c.getList().isEmpty()) {
                                    out.print(c.getName() + " contiene:");
                                    Iterator<AdvObject> it = c.getList().iterator();
                                    while (it.hasNext()) {
                                        AdvObject next = it.next();
                                        out.print(" " + next.getName());
                                    }
                                    out.println();
                                }
                            } else {
                                out.println("Hai aperto: " + p.getObject().getName());
                                p.getObject().setOpen(true);
                            }
                        } else {
                            out.println("Non puoi aprire questo oggetto.");
                        }
                    }
                    //APRIRE OGGETTI CONETENUTI IN INVENTARIO
                    /*if (p.getInvObject() != null) {
                        if (p.getInvObject().isOpenable() && p.getInvObject().isOpen() == false) {
                            if (p.getInvObject() instanceof AdvObjectContainer) {
                                AdvObjectContainer c = (AdvObjectContainer) p.getInvObject();
                                if (!c.getList().isEmpty()) {
                                    out.print(c.getName() + " contiene:");
                                    Iterator<AdvObject> it = c.getList().iterator();
                                    while (it.hasNext()) {
                                        AdvObject next = it.next();
                                        getInventory().add(next);
                                        out.print(" " + next.getName());
                                        it.remove();
                                    }
                                    out.println();
                                }
                            } else {
                                p.getInvObject().setOpen(true);
                            }
                            out.println("Hai aperto nel tuo inventario: " + p.getInvObject().getName());
                        } else {
                            out.println("Non puoi aprire questo oggetto.");
                        }
                    }*/
                }
            } else if (p.getCommand().getType() == CommandType.PUSH) {
                //ricerca oggetti pushabili
                if (p.getObject() != null && p.getObject().isPushable() && getCurrentRoom().objectInRoom(p.getObject())) {
                    out.println("Hai premuto: " + p.getObject().getName());
                    //AZIONE SU OGGETTI TRAMITE ID
                    /*
                    if (p.getObject().getId() == 3) {
                        end(out);
                    }*/
                }//premere oggetto in inventario
                /* else if (p.getInvObject() != null && p.getInvObject().isPushable()) {
                    out.println("Hai premuto: " + p.getInvObject().getName());
                    if (p.getInvObject().getId() == 3) {
                        end(out);
                    }*/
                } else {
                    out.println("Non ci sono oggetti che puoi premere qui.");
                }
            if (noroom) {
                out.println("Da quella parte non si puo' andare c'e' un muro!");
            } else if (move) {
            	if (getCurrentRoom().getVisited() >1) {
            		if(getCurrentRoom().interactiveObjects().isEmpty()) {
                        out.println("Sei in " + getCurrentRoom().getName());
            		} else {
                		out.print("Sei in ");
                        out.println(getCurrentRoom().getName()+"\n");
                    	for (AdvObject obj : getCurrentRoom().interactiveObjects()) 
                    		out.println("Vedo "+obj.getArticle()+" "+obj.getName());
            		}


            	}else {
            		if( getCurrentRoom().interactiveObjects().isEmpty()) {
                		//out.println(getCurrentRoom().getDescription());
            			formattedString(getCurrentRoom().getDescription());
            		} else {
                		//out.println(getCurrentRoom().getDescription()+"\n");
                		formattedString(getCurrentRoom().getDescription());
                		System.out.println();
                		for (AdvObject obj : getCurrentRoom().interactiveObjects()) 
                			out.println("Vedo "+obj.getArticle()+" "+obj.getName());
            		}
            	}
            }
        }
    }

    private void end(PrintStream out) {
        out.println("\n\n\nMORTO\n\n\n\n");
        System.exit(0);
    }
    
    public void firstScreen() {
    	System.out.println();
    	System.out.println();
    	System.out.print(""+
    	        "      ╔═══╗\r\n" + 
    	        "      ║► ◄║\r\n" + 
    	        "   ╔═╗╚═══╝╔═╗\r\n" + 
    	        "╔══════════════════════════════════════════════════════════════════════════════════════                                                                                                           \r\n" + 
    	        "║    ▄▄▄█████ █████▒██   ██ ▄▄█████ █    ██ ▄▄▄      ██                 ╔═══╗                    \r\n" + 
    	        "║    ▓  ██▒   █   ▀▒▒ █ █ ▒   ██▒  ▒██   ██▒████▄    ██▒                ║× ×║                    \r\n" + 
    	        "║    ▒  ██░ ▒▒███  ░░  █   ▒  ██░ ▒ ██  ▒██▒██  ▀█▄ ▒██░             ╔═╗╚═══╝╔═╗                 \r\n" + 
    	        "║    ░  ██  ░▒ █  ▄ ░ █ █ ▒░  ██  ░ ██  ░██░██▄▄▄▄██▒██░             ╚═╝  ▒  ╚═╝                 \r\n" + 
    	        "║      ▒██▒ ░░▒████▒██▒ ▒██▒ ▒██▒ ░▒▒█████▓ ▓█   ▓██░██████▒            ╚   ╝                    \r\n" + 
    	        "║      ▒ ░░  ░░ ▒░ ▒▒ ░ ░  ░ ▒ ░░  ░▒▓▒ ▒ ▒ ▒▒   ▓▒█░ ▒░▓  ░            ║ _ ║                    \r\n" + 
    	        "║        ░    ░ ░  ░░   ░▒ ░   ░   ░░▒░ ░ ░  ▒   ▒▒ ░ ░ ▒  ░            ╚╝ ╚╝                    \r\n" + 
    	        "║      ░        ░   ░    ░   ░      ░░░ ░ ░  ░   ▒    ░ ░                                        \r\n" + 
    	        "║               ░  ░░    ░            ░          ░  ░   ░  ░                                     ║\r\n" + 
    	        "║              ▄▄▄      ██▓███  ▒█████  ▄████▄  ▄▄▄      ██     ██   ██ ██▓███   ██████ █████    ║\r\n" + 
    	        "║             ▒████▄    ██░  ██▒██▒  ██▒██▀ ▀█ ▒████▄    ██▒   ▒██  ██ ██░  ██▒██    ▒ █   ▀     ║\r\n" + 
    	        "║             ▒██  ▀█▄  ██░ ██▓▒██░  ██▒▓█    ▄▒██  ▀█▄ ▒██░    ▒██ ██ ██░ ██▒░ ▒██▄  ▒███       ║\r\n" + 
    	        "║             ░██▄▄▄▄██ ██▄█▓▒ ▒██   ██▒▓▓▄ ▄██░██▄▄▄▄██▒██░    ░ ▐██  ██▄█▒▒ ▒ ▒   ██▒ █  ▄     ║\r\n" + 
    	        "║              ▓█   ▓██▒██▒ ░  ░ ████▓▒▒ ▓███▀ ░▓█   ▓██░██████▒░ ██▒  ██▒ ░  ▒██████▒░▒████▒    ║\r\n" + 
    	        "               ▒▒   ▓▒█▒ ▒░ ░  ░ ▒░▒░▒░░ ░▒ ▒  ░▒▒   ▓▒█░ ▒░▓  ░ ██▒▒▒▒▓▒░ ░  ▒ ▒▓▒ ▒ ░░ ▒░ ░    ║\r\n" + 
    	        "                 ▒   ▒▒ ░▒ ░      ░ ▒ ▒░  ░  ▒    ▒   ▒▒ ░ ░ ▒  ▓██ ░▒░░▒ ░    ░ ░▒  ░ ░░ ░  ░   ║\r\n" + 
    	        "                  ░   ▒  ░░      ░ ░ ░ ▒ ░         ░   ▒    ░ ░  ▒ ▒ ░░ ░░      ░  ░  ░    ░     ║\r\n" + 
    	        "                   ░  ░           ░ ░ ░ ░           ░  ░   ░  ░ ░                  ░    ░  ░     ║\r\n" + 
    	        "                                      ░                       ░ ░                                ║\r\n" + 
    	        "                                                                                                 ║\r\n" + 
    	        "        ═════════════════════════════════════════════════════════════════════════════════════════╝\n\n\n");
    	try {
        	Thread.sleep(2000);
       	} catch (InterruptedException e) {
       		e.printStackTrace(); 
       		}
    	}
    
    public void menu() {
    	//inserire copyright
    	System.out.println("Vuoi :"+"\n");
    	System.out.println("1) Iniziare una nuova partita"+"\n");
    	System.out.println("2) Riprendere una situazione da disco"+"\n");
    	System.out.println("3) Ripassare le istruzioni"+"\n");
    	System.out.println("4) Smettere prima ancora di incominciare");
    }
 
    public void prologue() {
    	String message ="\n\n[ PROLOGO ]\n\n" + 
    			"Nell’anno 2050 una terribile malattia infettiva si è scatenata sull’intero Globo causando negli umani "+"\n"+
    			"atteggiamenti quali stati di collera e desiderio incessante di nutrirsi di carne umana. \r\n" + 
    			"Con il passare degli anni più e più persone sono morte e intere città andate in rovina. " +"\n"+ 
    			"Il tuo addestramento nelle forze speciali ti ha consentito di rimanere in vita per tutto questo tempo "+"\n"
    			+ "e ora sei alla ricerca di altri superstiti. \r\n" + 
    			"Con gli operatori telefonici e Internet ormai fuori uso da anni  il tuo unico mezzo di comunicazione è" +"\n"
    			+ "la radio che hai nello scantinato e che cerchi di riparare da settimane.\r\n"
    			+"\n\n";
    	slowPrint(message);
    }
    
    public void slowPrint(String message){
        for (int i = 0; i < message.length(); i++)
        {
            System.out.print(message.charAt(i));

            try
            {
                Thread.sleep(50);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
  
    public void loadGame() {}
    
    public void instructions() {
    	System.out.println("Il tuo obbiettivo principale è quello di aiutare un gruppo di ricercatori alle prese con un antidoto.\r\n" + 
    			"Il mondo è stato colpito da una terribile piaga, per farcela dovrai affrontare i suoi effetti…e non solo.\r\n" + 
    			"Non preoccuparti, ti accompagnerò all’interno di questa tua eroica avventura ma spetterà solo a te prendere le decisioni,\nio ti potrò comunicare solo le loro conseguenze. Speriamo non siano troppo gravi!\r\n" + 
    			"\r\n" + 
    			"Per muoverti, usa:\r\n" + 
    			" - NORD, SUD, EST, OVEST, ALTO, BASSO, oppure soltanto:\r\n" + 
    			" - N, S, E, O, A, B.\r\n" + 
    			"Ti darò la descrizione completa di ciò che ti circonda solo la prima volta che arriverai in un determinato ambiente. Se necessiti di una descrizione completa, scrivi:\r\n" + 
    			" - GUARDA o\r\n" + 
    			" - GUARDA LA STANZA.\r\n\n" + 
    			"Fra le azioni fondamentali ci sono:\r\n" + 
    			" - PRENDI qualcosa,\r\n" + 
    			" - LASCIA qualcosa,\r\n" + 
    			" - GUARDA qualcosa, ad esempio GUARDA LA PROVETTA\r\n\n" + 
    			"Non usare frasi troppo complesse.\r\n\n" + 
    			"Ricorda che hai a disposizione un inventario e quando prenderai un oggetto quest’ultimo finirà lì dentro. Stai attento il tuo inventario non è infinito.\r\n" + 
    			"Altri comandi importanti sono:\r\n" + 
    			" - DOVE ti dice dove ti trovi\r\n" + 
    			" - COSA elenca il contenuto del tuo inventario\r\n" + 
    			" - SAVE serve a registrare la situazione su disco e\r\n" + 
    			" - LOAD a caricare la partita\r\n" + 
    			" - BASTA chiude il gioco e\r\n" + 
    			" - ISTRUZIONI ti mostra tutto ciò che hai appena visto\r\n\n" + 
    			"Divertiti con la nostra Apocalisse Testuale!\r\n" + 
    			"");
    }
   
    public void formattedString(String input) {
    	int n = input.length()/100;
    	if(n>=1) {
    		int y=0;
    		for (int i = 1; i<=n ;i++) {
        		while(y<i*100) {
        			System.out.print(input.charAt(y));
        			y++;
        		}
        		if(input.charAt(y) == ' ') {
        			System.out.println();
        			y++;
        		} else {
        			while(input.charAt(y) != ' ') {
        				System.out.print(input.charAt(y));
        				y++;
        			}
        			System.out.println();
        			y++;
        		}
    		} 
        	int r = input.length()-y;
    		for (int i=0; i<r;i++) {
    			System.out.print(input.charAt(y));
    			y++;
    		}
    	} else {
    		System.out.print(input);
    	}
    	System.out.println();
    }
}




/*
     STANZE ID
     Soggiorno = 1
     Bagno = 2 
     Corridoio = 3
     Cucina = 4
     Armeria = 5
     Ingresso = 6
     Garage = 7
     
    */ 	

