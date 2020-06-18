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
	
	boolean radioComunication = false;
	boolean weapon = false;
	
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
                	if(getCurrentRoom().getId() == 9) {
                		if(getCurrentRoom().objectById(28).getSpecificState().equals("affamata")) {
                			slowPrint("Ti avevo avvisato... ti sei fatto notare e adesso sei circondato di zombie.. esplodi qualche colpo ma è tutto vano, dovevi darmi ascolto");
                			end(out);
                		} else if (getCurrentRoom().objectById(30).getSpecificState().equals("elettrificato")) {
                			slowPrint("Cerchi di attraversare il cancello elettrico nonostante fosse attivo .. 2000 V attraversano il tuo corpo \n bruciandoti vivo.");
                			end(out);
                		}
                	}
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
            } else if (p.getCommand().getType() == CommandType.UP) {
                if (getCurrentRoom().getUp() != 0) {
                	setCurrentRoom(roomById(getCurrentRoom().getUp()));
                	getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                    move = true;
                } else {
                    noroom = true;
                }    
            }else if (p.getCommand().getType() == CommandType.DOWN) {
                if (getCurrentRoom().getDown() != 0) {
                	if(getCurrentRoom().getId()==5 && getCurrentRoom().objectById(5).isOpen()==true) {
                    	setCurrentRoom(roomById(getCurrentRoom().getDown()));
                    	getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                        move = true;
                	} else 
                		noroom = true;

                } else {
                    noroom = true;
                }  

            } else if(p.getCommand().getType() == CommandType.USE && p.getObject().getId() == -1) { 
            	if(p.getInvObject().getId() > 0) {
            		if(p.getInvObject().getId() == 22) {
            			AdvObject radio = getCurrentRoom().objectById(21);
            			if (radio != null) {
            				if(radio.getSpecificState().equals("senza antenna")) {
            					radio.setSpecificState("funzionante");
            					getInventory().remove(getCurrentRoom().objectById(21));	
            					out.println("Complimenti sei riuscito ad aggiustare la radio con un semplice filo creando un'antenna.. geniale !! \n");
            					slowPrint("Ops…sembra che la corrente sia andata via! Per fortuna che la serranda del garage è parzialmente alzata"+
            								"\n"+"e la luce riesce ad entrare. E’ meglio che tu dia un’occhiata al generatore!\n");
            					getCurrentRoom().objectById(23).setSpecificState("non in moto");
            				}
            			} else
            				out.println("Non vedo come potrei usarlo qui");
            		} else if(p.getInvObject().getId() == 18 && getCurrentRoom().getId()==5) {
                    		out.println("Hai aperto la botola");
                    	    getCurrentRoom().objectById(22).setOpen(true);
                    	    p.getObject().setSpecificState("aperta");
            		}else
            			out.println("Non vedo come potrei usarlo qui");	
            	} else
            		out.println("Non vedo questo oggetto");
            } else if (p.getCommand().getType() == CommandType.USE && p.getObject().getId() > 0) {
            	if (p.getObject().getId() == 21) {
            		if(p.getObject().getSpecificState().equals("funzionante") && getCurrentRoom().objectById(23).getSpecificState().equals("in moto")) {
            			slowPrint("[Radio]: “Crrr…Crrrrrrrr…Qui BASE CHARLIE…Crrr…mi ricevete?” \n");
            			slowPrint("[Tu]:“Vi ricevo, passo” \n");
            			slowPrint("[Radio]: “Crrr…Allora c’è ancora qualcuno lì fuori! Abbiamo bisogno di aiuto qui, passo” \n");
            			slowPrint("[Tu]:“Dove vi trovate, passo” \n");
            			slowPrint("[Radio]: “Nel laboratorio per la ricerca contro il virus T, a nord della citta', passo” \n");
            			slowPrint("[Tu]:“In quanti siet-” \n");
            			out.println("Tutto d’un tratto rumori di ingranaggi e di apparecchi elettronici, un fumo grigiastro riempie la stanza:"+
            					"\n"+"è la radio…è andata!");
            			out.println("Sembra proprio che quelle persone abbiano bisogno del tuo aiuto.");
            			radioComunication=true;
            		} else
            			out.println("La radio sembra non funzionare");
            	} if(p.getObject().getId() == 24) {
           			if(radioComunication == true) {
           				if(getCurrentRoom().objectById(24).getSpecificState().equals("con benzina")) {
           					if(weapon == true) {
           						roomById(8).getObjects().add(getCurrentRoom().objectById(24));
           						getCurrentRoom().getObjects().remove(getCurrentRoom().objectById(24));
           						setCurrentRoom(roomById(8));
           						slowPrint("Con non poche difficolta' sei arrivato al laboratorio , durante il tragitto hai dovuto esplodere diversi "
           								+ "\n"+"colpi di arma da fuoco verso gli zombie per aprirti il passaggio ... alla fine eccoti qui dinanzi all'imponente struttura.");
           						move = true;
           					}else 
           						out.println("Non ti consiglio di uscire disarmato con tutti quei non morti nei dintorni ! ");
            			
           				} else 
           					out.println("Dove vorresti andare senza benzina??");
           			} else 
           				out.println("Non hai dove andare");
            	}else 
            		out.println("Impossibile utilizzare l'oggetto");
            }
            /*else if (p.getCommand().getType() == CommandType.USE && p.getObject().getId() == -2) {
            	out.println("Non mi hai detto cosa usare");}*/
            
            else if (p.getCommand().getType() == CommandType.INVENTORY) {
            	if(getInventory().getList().isEmpty())
            		out.println("Il inventario e' vuoto ");
            	else
            	{
	                out.println("Nel tuo inventario ci sono: ");
	                for (AdvObject o : getInventory().getList()) {
	                	out.println();
	                    out.print(o.getName());
	                    if(o.getSpecificState() != null)
	                    	out.print(" "+o.getSpecificState());
	                }
	                out.println();
                }
            } else if (p.getCommand().getType() == CommandType.LOOK_AT && p.getObject().getId() == -2) {
            	formattedString(getCurrentRoom().getDescription());
        		System.out.println();
            	for (AdvObject obj : getCurrentRoom().interactiveObjects()) 
        		{
        			if(obj.getSpecificState()!=null) 
        				out.println("Vedo "+obj.getArticle()+" "+obj.getName()+" "+obj.getSpecificState());
        			else
        				out.println("Vedo "+obj.getArticle()+" "+obj.getName());
        		}
            } else if(p.getCommand().getType() == CommandType.LOOK_AT && p.getObject().getId() == -1) { 
            	if(p.getInvObject().getId() > 0) 
            		formattedString(p.getInvObject().getDescription());
            	else
            		out.println("Non vedo questo oggetto");
            } else if (p.getCommand().getType() == CommandType.LOOK_AT && p.getObject().getId() > 0) {
            	if (getCurrentRoom().objectInRoom(p.getObject())) {
            		formattedString(p.getObject().getDescription());
            	} else if(getCurrentRoom().objectContainer(p.getObject()) != null) {
            		if(getCurrentRoom().objectContainer(p.getObject()).isOpen()) {
            			formattedString(p.getObject().getDescription());
            		}else 
            			out.println("Non vedo questo oggetto");
            	} else
            		out.println("Non vedo questo oggetto");    		
            }else if (p.getCommand().getType() == CommandType.POUR && p.getObject().getId() == -1) {
            	if(p.getInvObject().getId() > 0) {
            		if(p.getInvObject().getId() == 25 ) {
            			if(getCurrentRoom().objectById(24).getSpecificState().equals("senza benzina")) {
            				if (p.getInvObject().getSpecificState().equals("piena")) {
            					p.getInvObject().setSpecificState("vuota");
            					getCurrentRoom().getObjects().add(p.getInvObject());
            					getInventory().remove(getCurrentRoom().objectById(25));	
            					getCurrentRoom().objectById(24).setSpecificState("con benzina");
            					out.println("Hai fatto il pieno al tuo Pick-up");
            				} else
            					out.println("La tanica e' vuota");
            			} else 
            				out.println("Hai gia' fatto il pieno");
            		}
            	} else 
            		out.println("EHH ??");
            }/*else if (p.getCommand().getType() == CommandType.POUR && p.getObject().getId() != -1 ) {
            	out.println("L'unica cosa che puoi versare sono lacrime");}*/
            	
            else if (p.getCommand().getType() == CommandType.PULL && p.getObject().getId() >0) {
            	if(p.getObject().getId() == 26) {
            		if(getCurrentRoom().objectById(23).getSpecificState().equals("non in moto")) {
            			getCurrentRoom().objectById(23).setSpecificState("in moto");
            			slowPrint("Ecco qua, il generatore ha ripreso a funzionare (non che prima andasse perfettamente) ed il neon"+
            					  "\n"+ "ha ripreso ad illuminare la stanza. Meglio che tu finisca quello che hai lasciato in sospeso!\n");
            		} else
            			out.println("Il generatore e' gia' in moto");
            		
            	}
            	
            }/*else if (p.getCommand().getType() == CommandType.PULL && p.getObject().getId() < 0) {
            	out.println("Non c'e' niente da tirare qui"); }*/         	
            else if (p.getCommand().getType() == CommandType.PICK_UP && p.getObject().getId() >0) {
                if (getCurrentRoom().objectInRoom(p.getObject())||getCurrentRoom().objectContainer(p.getObject()).isOpen()){ 
                    if (p.getObject().isPickupable()) {
                		if(p.getObject().getId()==10) {
                			slowPrint("Rimuovi l'asse e un non morto ti affera dalla gola scaravendatoti fuori dalla finestra.\n" + 
                							  "In un men che non si dica vieni circondato da un'orda di zombi che ti riduce a brandelli.\n");
                			end(out);
                		}
                		if(getCurrentRoom().objectContainer(p.getObject())!=null) {
                			if(p.getObject().getId()==2 || p.getObject().getId()==3 || p.getObject().getId()==4 ) {
                				if(weapon == false) {
                					getInventory().add(p.getObject());
                        			out.println("Hai raccolto "+p.getObject().getArticle()+" " + p.getObject().getName()+" da"+" "+getCurrentRoom().objectContainer(p.getObject()).getName());
                        			getCurrentRoom().objectContainer(p.getObject()).remove(p.getObject());
                        			weapon = true;
                				} else {
                					out.println("Non hai spazio per un'altra arma");
                				}
                			} else {
                				getInventory().add(p.getObject());
                				out.println("Hai raccolto "+p.getObject().getArticle()+" " + p.getObject().getName()+" da"+" "+getCurrentRoom().objectContainer(p.getObject()).getName());
                				getCurrentRoom().objectContainer(p.getObject()).remove(p.getObject());  
                			}
                		}
                		else {
                            getInventory().add(p.getObject());
                            getCurrentRoom().getObjects().remove(p.getObject());
                            out.println("Hai raccolto "+p.getObject().getArticle()+" " + p.getObject().getName());
                		}
                    } else 
                    	out.println("Ma sei matto ?!");
                } else if (getCurrentRoom().objectContainer(p.getObject()) != null) {
                	if(getCurrentRoom().objectContainer(p.getObject()).isOpen()) {
                		if (p.getObject().isPickupable()) {
                            getInventory().add(p.getObject());
                            getCurrentRoom().getObjects().remove(p.getObject());
                            out.println("Hai raccolto "+p.getObject().getArticle()+" " + p.getObject().getName());
                		}
                	}
                } else {
                    out.println("Non c'e' niente da raccogliere qui.");
                }
            }/* else if (p.getCommand().getType() == CommandType.PICK_UP && p.getObject().getId() == -2) {
            	out.println("Non mi hai detto cosa però, sii più preciso");
            } else if (p.getCommand().getType() == CommandType.PICK_UP && p.getObject().getId() == -1) {
            	out.println("Non vedo questo oggetto");}*/
            else if (p.getCommand().getType() == CommandType.OPEN) {     
                if ((p.getObject().getId() == -1||p.getObject().getId() == -2)) {
                    out.println("Non c'e' niente da aprire qui.");
                } else {
                    if (p.getObject().getId() >0) {
                        if (p.getObject().isOpenable() && p.getObject().isOpen() == false) {
                            if (p.getObject() instanceof AdvObjectContainer) {
                                p.getObject().setOpen(true);
                                out.println("Hai aperto: "+p.getObject().getArticle()+" "  + p.getObject().getName());
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
                            	//apri porte con chiavi
                            	if(p.getObject().getId()==5 && getInventory().objectInInventory(getInventory().objectById(18)))
                            	{
                            		out.println("Hai aperto: "+p.getObject().getArticle()+" "  + p.getObject().getName());
                            	    p.getObject().setOpen(true);
                            	    p.getObject().setSpecificState("aperta");
                            	}
                            	else if(p.getObject().getId()==5 && !getInventory().objectInInventory(getInventory().objectById(18))){
                            		out.println("Non disponi delle chiavi per aprire questa porta");
                            	}
                            	else {
                            		if(p.getObject().getId()==20) {
                            			slowPrint("Apri la porta lentamente ma un non morto ti affera dal braccio scaravendatoti fuori dalla porta.\n" + 
                  							  "In un men che non si dica vieni circondato da un'orda di zombi che ti riduce a brandelli.\n");
                            			end(out);
                            		}
                            		//porte senza chiavi
                                    p.getObject().setOpen(true);
                                    p.getObject().setSpecificState("aperta");
                            	}
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
                if (p.getObject().getId() >0 && p.getObject().isPushable() && getCurrentRoom().objectInRoom(p.getObject())) {
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
                    out.println("EHH ?? ");
                }
            if (noroom) {
                out.println("Da quella parte non si puo' andare !!");
            } else if (move) {
            	if (getCurrentRoom().getVisited() >1) {
            		if(getCurrentRoom().interactiveObjects().isEmpty()) {
                        out.println("Sei in " + getCurrentRoom().getName());
            		} else {
                		out.print("Sei in ");
                        out.println(getCurrentRoom().getName()+"\n");
                        for (AdvObject obj : getCurrentRoom().interactiveObjects()) 
                		{
                			if(obj.getSpecificState()!=null) 
                				out.println("Vedo "+obj.getArticle()+" "+obj.getName()+" "+obj.getSpecificState());
                			else
                				out.println("Vedo "+obj.getArticle()+" "+obj.getName());
                		}
            		}


            	}else {
            		if( getCurrentRoom().interactiveObjects().isEmpty()) {
            			formattedString(getCurrentRoom().getDescription());
            		} else {
                		formattedString(getCurrentRoom().getDescription());
                		System.out.println();
                		for (AdvObject obj : getCurrentRoom().interactiveObjects()) 
                		{
                			if(obj.getSpecificState()!=null) 
                				out.println("Vedo "+obj.getArticle()+" "+obj.getName()+" "+obj.getSpecificState());
                			else
                				out.println("Vedo "+obj.getArticle()+" "+obj.getName());
                		}
            		}
            	}
            }
        }
    }

    private void end(PrintStream out) {
        out.println("\r\n\n" +
                "             @\r\n" +
                "             @\r\n" +
                "           @@@@@\r\n" +
                "             @\r\n" +
                "             @\r\n" +
                "         ____#_____\r\n" +
                "        /         /\r\n" +
                "       /   ~~~   /\r\n" +
                "      /   ~~~   /\n\n");
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
    			"Nell’anno 2050 una terribile malattia infettiva si e' scatenata sull’intero Globo causando negli umani "+"\n"+
    			"atteggiamenti quali stati di collera e desiderio incessante di nutrirsi di carne umana. \r\n" + 
    			"Con il passare degli anni più e più persone sono morte e intere città andate in rovina. " +"\n"+ 
    			"Il tuo addestramento nelle forze speciali ti ha consentito di rimanere in vita per tutto questo tempo "+"\n"
    			+ "e ora sei alla ricerca di altri superstiti. \r\n" + 
    			"Con gli operatori telefonici e Internet ormai fuori uso da anni  il tuo unico mezzo di comunicazione e'" +"\n"
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
    	System.out.println("Il tuo obbiettivo principale e' quello di aiutare un gruppo di ricercatori alle prese con un antidoto.\r\n" + 
    			"Il mondo e' stato colpito da una terribile piaga, per farcela dovrai affrontare i suoi effetti…e non solo.\r\n" + 
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
    			"Ricorda che hai a disposizione un inventario e quando prenderai un oggetto quest’ultimo finirà lì dentro. Stai attento il tuo inventario non e' infinito.\r\n" + 
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

