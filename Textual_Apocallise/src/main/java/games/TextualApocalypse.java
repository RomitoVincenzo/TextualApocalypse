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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

public class TextualApocalypse extends GameDescription implements Serializable{
	
	private boolean radioComunication = false;
	private boolean doctorComunication = false;
	private boolean weapon = false;
	private boolean testTube=false;
	
    @Override
    public void init() throws Exception {    	
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
	           // setCurrentRoom(roomById(1));
	            /////MODIFICHE PER FAST RUN
	            setCurrentRoom(roomById(5));
	    		setDead(false);
	            weapon = true;
	            AdvObjectContainer c = (AdvObjectContainer) roomById(5).objectById(1);
	            getInventory().add(roomById(5).getContainedObjects().get(0));
	            ///////////
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
                		} else if (getCurrentRoom().objectById(29).getSpecificState().equals("elettrificato")) {
                			Scanner scanner = new Scanner(System.in);
                			String command = "";
                			slowPrint("[SRAC] : Qui sistema automatico di riconoscimento attivita' celebrale umana !\r\n" + 
                					"         per accedere all'interno della struttura bisogna superare il seguente test :\r\n" + 
                					"         D I E C I : cinque\r\n" + 
                					"         D O D I C I : sei\r\n" + 
                					"         Q U A T T R O : ");
                			command = scanner.nextLine();
                			if (command.equals("7") || command.equals("sette")){
                				slowPrint("[SRAC] : Benvenuto all'interno del laboratorio ! \n");
                				slowPrint("Il grande Tesla Gate si spegne dinanzi ai tuoi occhi liberando cosi' il passaggio \n");
                				getCurrentRoom().objectById(29).setSpecificState("spento");
                				move = true;
                        		setCurrentRoom(roomById(getCurrentRoom().getNorth()));
                        		getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                        		out.println();
                			} else 
                			{
                				out.println("\n[SRAC] : ERRATO");
                				slowPrint("Cerchi di attraversare il cancello elettrico nonostante il fallimento .. 2000 V attraversano il tuo corpo \nbruciandoti vivo.");
                				end(out);
                					
                			}
                		} else 
                		{
                    		setCurrentRoom(roomById(getCurrentRoom().getNorth()));
                    		getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                    		move = true;
                		}
                	} else if (getCurrentRoom().getId() == 10){
                		if(!doctorComunication)
                			out.println("Faresti meglio a parlare con il dottore");
                		else 
                		{
                    		setCurrentRoom(roomById(getCurrentRoom().getNorth()));
                    		getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                    		move = true;
                		}
                	} else if (getCurrentRoom().getId() == 20){
                		if(!getCurrentRoom().objectById(44).isOpen())
                			out.println("Non puoi andare di la', la porta e' chiusa");
                		else 
                		{
                    		setCurrentRoom(roomById(getCurrentRoom().getNorth()));
                    		getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                    		move = true;
                		}
                	}else 
                	{
                		setCurrentRoom(roomById(getCurrentRoom().getNorth()));
                		getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                		move = true;
                	}
                } else {
                    noroom = true;
                }
            } else if (p.getCommand().getType() == CommandType.SOUTH) {
                if (getCurrentRoom().getSouth() != 0) {
                	if(getCurrentRoom().objectByName("Zombie") != null) {
                		if(getCurrentRoom().objectByName("Zombie").getSpecificState().equals("vivo")) {
                			slowPrint("Il dottore ti aveva avvisato, un'orda di zombie ti riduce a brandelli dopo aver masticato la tua carne");
                			end(out);
                		}else
                		{
                        	setCurrentRoom(roomById(getCurrentRoom().getSouth()));
                        	getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                            move = true;
                		}
                	} else 
                	{
                		setCurrentRoom(roomById(getCurrentRoom().getSouth()));
                		getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                		move = true;
                	}
                } else {
                    noroom = true;
                }
            } else if (p.getCommand().getType() == CommandType.EAST) {
                if (getCurrentRoom().getEast() != 0) {
                	if(getCurrentRoom().objectByName("Zombie") != null) {
                		if(getCurrentRoom().objectByName("Zombie").getSpecificState().equals("vivo")) {
                			slowPrint("Il dottore ti aveva avvisato, un'orda di zombie ti riduce a brandelli dopo aver masticato la tua carne");
                			end(out);
                		}else
                		{
                        	setCurrentRoom(roomById(getCurrentRoom().getEast()));
                        	getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                            move = true;
                		}
                	} else 
                	{
                		setCurrentRoom(roomById(getCurrentRoom().getEast()));
                		getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                		move = true;
                	}
                } else {
                    noroom = true;
                }
            } else if (p.getCommand().getType() == CommandType.WEST) {
                if (getCurrentRoom().getWest() != 0) {
                	if(getCurrentRoom().objectByName("Zombie") != null) {
                		if(getCurrentRoom().objectByName("Zombie").getSpecificState().equals("vivo")) {
                			slowPrint("Il dottore ti aveva avvisato, un'orda di zombie ti riduce a brandelli dopo aver masticato la tua carne");
                			end(out);
                		}else
                		{
                        	setCurrentRoom(roomById(getCurrentRoom().getWest()));
                        	getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                            move = true;
                		}
                	} else 
                	{
                		setCurrentRoom(roomById(getCurrentRoom().getWest()));
                		getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                		move = true;
                	}
                } else {
                    noroom = true;
                }    
            } else if (p.getCommand().getType() == CommandType.UP) {
                if (getCurrentRoom().getUp() != 0) {
                	if(getCurrentRoom().getId()==19) {
                		if(getCurrentRoom().objectById(48).getSpecificState() != null) {
                     		if(testTube) {
                            	setCurrentRoom(roomById(getCurrentRoom().getUp()));
                            	getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                                move = true;
                            	slowPrint("Appena metti piedi sul terrazzo la porta dietro di te si chiude investita dagli zombie che cercavano di raggiungerti");
                    		} else
                    			out.println("Prima dovresti procurarti la provetta");
                		} else 
                			out.println("Non vedo come potrei fare");
   
                	}else 
                	{
                    	setCurrentRoom(roomById(getCurrentRoom().getUp()));
                    	getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                        move = true;
                	}
                } else {
                    noroom = true;
                }    
            }else if (p.getCommand().getType() == CommandType.DOWN) {
                if (getCurrentRoom().getDown() != 0) {
                	if(getCurrentRoom().getId()==5 && getCurrentRoom().objectById(5).isOpen()==true) {
                    	setCurrentRoom(roomById(getCurrentRoom().getDown()));
                    	getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
                        move = true;
                	} else if(getCurrentRoom().getId()==22){
                		outro();
                	}
                	else 
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
            		} else if(p.getInvObject().getId() == 18 && getCurrentRoom().getId()==5 && getCurrentRoom().objectById(5).isOpen()==false) {
                    		out.println("Hai aperto la botola");
                    	    getCurrentRoom().objectById(5).setOpen(true);
                    	    p.getObject().setSpecificState("aperta");
            		} else if(p.getInvObject().getId()==43 && getCurrentRoom().getId()== 15 ) {
                		if(getCurrentRoom().objectById(36).getSpecificState().equals("non funzionante")) {
                			getInventory().remove(p.getInvObject());
                			getCurrentRoom().objectById(36).setSpecificState("funzionante");
                			roomById(13).objectById(32).setSpecificState("funzionante");
                			out.print("Complimenti sei riuscito ad aggiustare il quadro elettrico, ora e' tornata l'elettricità all'interno del piano!! \n");
                		} 
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
            	}else if(p.getObject().getId() == 24  && getCurrentRoom().getId()==7) {
           			if(radioComunication == true) {
           				if(getCurrentRoom().objectById(24).getSpecificState().equals("con benzina")) {
           					if(weapon == true) {
           						roomById(8).getObjects().add(getCurrentRoom().objectById(24));
           						getCurrentRoom().getObjects().remove(getCurrentRoom().objectById(24));
           						setCurrentRoom(roomById(8));
           						getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
           						slowPrint("Con non poche difficolta' sei arrivato al laboratorio , durante il tragitto hai dovuto esplodere diversi "
           								+ "\n"+"colpi di arma da fuoco verso gli zombie per aprirti il passaggio ... alla fine eccoti qui dinanzi all'imponente struttura.\n\n");
           						move = true;
           					}else 
           						out.println("Non ti consiglio di uscire disarmato con tutti quei non morti nei dintorni ! ");
            			
           				} else 
           					out.println("Dove vorresti andare senza benzina??");
           			} else 
           				out.println("Non hai dove andare");
            	} else if(p.getObject().getId() == 32  && getCurrentRoom().getId()==13 && p.getObject().getSpecificState().equals("funzionante")) {
            		slowPrint("[DOC] : “Hey!! Mi senti?? E' inutile che ti guardi intorno, ti sto parlando tramite la linea di emergenza dell'ascensore \r\n" + 
            				"        Ti porterà direttamente al piano con la sala contente le provette non ti preoccupare. \r\n"+
            				"        Ho visto quello che hai fatto la' fuori, bel lavoro, ora ti manca solo lo sprint finale!” .\r\n");
            		slowPrint("L'ascensore si ferma a quello che sembra essere l'ultimo piano dell'edificio,\n"+
            				  "Appena metti piedi fuori dalle porte metallica un frastuono micidiale proviene da dietro di te: \r\n\n"+
            				  "E' l'ascensore, è crollato!\n");
            		setCurrentRoom(roomById(18));
					getCurrentRoom().setVisited(getCurrentRoom().getVisited()+1);
					move=true;
            	} else if(p.getObject().getId() == 45  && getCurrentRoom().getId()==21) {
            		Scanner scanner = new Scanner(System.in);
            		String command ;
            		slowPrint("[Macchinario] : “Inserire numero di provetta desiderato”: ");
            		command = scanner.nextLine();
            		out.println();
            		if (command.equals("VFRR") || command.equals("vfrr")) {
            			if(!testTube)
            			{
	                		slowPrint("[Macchinario] : “Ecco a te la provetta” \n\n");
	                		out.print("Hai ricevuto la provetta del tipo VFRR richiesta dal dottore\n");
	                		AdvObject provetta = getCurrentRoom().getContainedObjects().get(0);
	                		getInventory().add(provetta);
	                		AdvObjectContainer macchinario = (AdvObjectContainer)p.getObject();
	                		macchinario.remove(provetta);
	                		testTube=true;
            			}
            			else
            				slowPrint("[Macchinario] : “Questo tipo di provetta e' stato gia' ritirato” \n");
            		}
            		else {
            			slowPrint("[Macchinario] : “Questo tipo di provetta non esiste” \n");
            		}		
            	}else 
            		out.println("Impossibile utilizzare l'oggetto");
            }else if (p.getCommand().getType() == CommandType.TALK_TO && p.getObject().getId()>0) {
            	if(p.getObject().getId() == 31) {
            		slowPrint("[DOC] : “Menomale che sei qui ,ti aspettavo!! \r\n" + 
            				"        Appena varcata quella porta troverai la terra di nessuno, i non morti sono riusciti ad entrare nella struttura e \r\n"+
            				"        hanno preso il controllo di molte stanze.\r\n" + 
            				"        Il tuo compito e' quello di arrivare nella sala provette, situata al piano superiore e di recuperare la seguente provetta : VFRR.\r\n" + 
            				"        La stanza è protetta dal nostro sistema SRAC, lo stesso che ti ha sottoposto il test all'ingresso. \r\n" + 
            				"        Arrivarci non sarà facile ma non mi resta che augurarti buona fortuna."+"\n"+
            				"        Un conisglio : nel caso dovessi entrare in contatto con uno zombie non esistare ad ucciderlo, \r\n" + 
            				"        uno zombie in allerta può causarti molti danni” \r\n " );
            		doctorComunication=true;
            	}
            	
        	}else if (p.getCommand().getType() == CommandType.INVENTORY) {
            	if(getInventory().getList().isEmpty())
            		out.println("L'inventario e' vuoto ");
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
            } else if (p.getCommand().getType() == CommandType.SHOOT && p.getObject().getId() >0 && weapon==true) {
            	if(getCurrentRoom().getId() == 9 && p.getObject().getSpecificState().equals("affamata")) {
            		slowPrint("Bella mossa marine ma mentre sei occupato a tener testa all'orda uno zombie sbuca da un tombino "
            				+ "\n"+"alle tue spalle e ti azzanna al polpaccio facendoti morire dissanguato.");
            		end(out);
            	} else if (getCurrentRoom().getId() == 9 && p.getObject().getSpecificState().equals("con attorno degli zombie")) {
            		getCurrentRoom().objectById(30).setSpecificState("esploso");
            		getCurrentRoom().objectById(28).setSpecificState("morta");
            		slowPrint("BOOOMM , che botto !!!" +"\n"+
            				"In un secondo l'aria si colora di rosso e una pioggia di sangue ricopre l'intera area ."+"\n"
            				+"Hai fatto salta in aria quei non morti facendo esplodere quel barile. \n");
            	} else if (p.getObject().getName().equals("Zombie")) {
            		if(p.getObject().getSpecificState().equals("vivo")) {
                		p.getObject().setSpecificState("morto");
                		out.println("Hai ucciso uno Zombie ");
            		} else
            			out.println("Conserva le munizioni soldato");
            	} 
        	} else if (p.getCommand().getType() == CommandType.PULL && p.getObject().getId() >0) {
            	if(p.getObject().getId() == 26) {
            		if(getCurrentRoom().objectById(23).getSpecificState().equals("non in moto")) {
            			getCurrentRoom().objectById(23).setSpecificState("in moto");
            			slowPrint("Ecco qua, il generatore ha ripreso a funzionare (non che prima andasse perfettamente) ed il neon"+
            					  "\n"+ "ha ripreso ad illuminare la stanza. Meglio che tu finisca quello che hai lasciato in sospeso!\n");
            		} else
            			out.println("Il generatore e' gia' in moto");
            	}
            	
            } else if (p.getCommand().getType() == CommandType.PICK_UP && p.getObject().getId() >0) {
                if (getCurrentRoom().objectInRoom(p.getObject())||getCurrentRoom().objectContainer(p.getObject()).isOpen()){ 
                    if (p.getObject().isPickupable()) {
                		if(p.getObject().getId()==10) {
                			slowPrint("Rimuovi l'asse e un non morto ti affera dalla gola scaraventandoti fuori dalla finestra.\n" + 
                							  "In un men che non si dica vieni circondato da un'orda di zombi che ti riduce a brandelli.\n");
                			end(out);
                		}else if(getCurrentRoom().objectContainer(p.getObject())!=null) {
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
            } else if (p.getCommand().getType() == CommandType.OPEN) {     
                if ((p.getObject().getId() == -1||p.getObject().getId() == -2)) {
                    out.println("Non c'e' niente da aprire qui.");
                } else {
                    if (p.getObject().getId() >0) {
                        if (p.getObject().isOpenable() && p.getObject().isOpen() == false) {
                            if (p.getObject() instanceof AdvObjectContainer) {
                            	if(p.getObject().getId()==42) {
                            		Scanner scanner = new Scanner(System.in);
                            		String command ;
                            		slowPrint("[SRAC] : “Qui sistema automatico di riconoscimento attivita' celebrale umana !\r\n" + 
                            				  "          per aprire l'armadietto bisogna superare il seguente test ”\r\n"); 
                            				
                            		stampaQuadrato();
                            		slowPrint("[SRAC] : “Inserire il numero di quadrati prensenti in figura”: ");
                            		command = scanner.nextLine();
                            		out.println();
                            		if (command.equals("quaranta") || command.equals("40")) {
                                        out.println("Hai aperto: "+p.getObject().getArticle()+" "  + p.getObject().getName());
                            			p.getObject().setOpen(true);
                            			p.getObject().setSpecificState("aperto");
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
                            			
                            		} else
                            			slowPrint("“[SRAC] : Accesso negato”\n");
                            	} else
                            	{
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
                            	}
                            } else {
                            	//apri porte con chiavi
                            	if(p.getObject().getId()==5 && getInventory().objectInInventory(getInventory().objectById(18))==true) {
                            		out.println("Hai aperto: "+p.getObject().getArticle()+" "  + p.getObject().getName());
                            	    p.getObject().setOpen(true);
                            	    p.getObject().setSpecificState("aperta");
                            	}
                            	else if(p.getObject().getId()==5 && !getInventory().objectInInventory(getInventory().objectById(18))){
                            		out.println("Non disponi delle chiavi per aprire questa porta");
                            	}
                            	else if(p.getObject().getId()==44){
                            		Scanner scanner = new Scanner(System.in);
                            		String command ;
                            		slowPrint("[SRAC] : “Qui sistema automatico di riconoscimento attivita' celebrale umana !\r\n" + 
                            				"         Prego inserire la password per accedere all'area”: ");
                            		command = scanner.nextLine();
                            		out.println();
                            		if(command.equals("ciaociao")) {
                                        p.getObject().setOpen(true);
                                        p.getObject().setSpecificState("aperta");
                                        slowPrint("[SRAC] : “Complimenti password corretta !”\r\n");
                            		}
                            		else {
                            			slowPrint("[SRAC] : “Password errata : ricordati, ci piace andare a destra in questo edificio !”\r\n");
                            		}
                            	}else {
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
                }
            } else if (p.getCommand().getType() == CommandType.PUSH && p.getObject().getId() >0) {
                //ricerca oggetti pushabili
                if (p.getObject().isPushable() && !p.getObject().isPush() && getCurrentRoom().objectInRoom(p.getObject())) {
                	if(getCurrentRoom().getId()==19 && p.getObject().getId()==47) {
                		out.println("Hai spostato lo scaffale e hai notato quelle che possano sembrare delle...");
                		getCurrentRoom().objectById(47).setSpecificState(null);
                		getCurrentRoom().objectById(48).setSpecificState("che conducono al terrazzo");
                		getCurrentRoom().objectById(47).setPush(true);
                	}
                } else
                	out.println("Scaffale già spostato, conserva le tue energie");
             } else if(p.getCommand().getType() == CommandType.SAVE){
            	 FileOutputStream outFile = null;
				try {
					outFile = new FileOutputStream("TA.dat");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	 ObjectOutputStream outStream = null;
				try {
					outStream = new ObjectOutputStream(outFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	 try {
					outStream.writeObject(this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
             }
             else      
            	 out.println("EHH ?? ");
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
                "             @ \r\n" +
                "             @ \r\n" +
                "           @@@@@ \r\n" +
                "             @ \r\n" +
                "             @ \r\n" +
                "         ____#_____\r\n" +
                "        /         /\r\n" +
                "       /   ~~~   /\r\n" +
                "      /   ~~~   /\n\n");
        setDead(true);
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
    
    public void outro() {
    	String message ="\n\n[ FINE ]\n\n" + 
    			"Scendi velocemente le scale al termine del quale sembra esserci il dottore che ti aspetta "+"\n"+
    			"pronto a creare un antidoto per il virus T ma.. \r\n" + 
    			"Senti un improvviso dolore allo stomaco , non sai cosa ti sta succedendo, chiudi le palpebre e le riapri ripetutamente," +"\n"+ 
    			"ad un certo punto il tuo improvviso dolore sparisce e su di te una bambina che gioca con la tua maglietta: "+"\n"
    			+ "Sei a casa, quella bambina e' tua figlia, e accanto a lei c'e' anche tua moglie \r\n" + 
    			"Penso prorprio che la tua TEXTUAL APOCALYPSE fosse semplicemente un sogno, magari un incubo ecco" +"\n"
    			+ "Spero tu ti sia divertito perche' noi sicuramente si!! \r\n"
    			+"Alla prossima !?!?! \n\n";
    	slowPrint(message);
    }
   
    public void prologue() {
    	String message ="\n\n[ PROLOGO ]\n\n" + 
    			"Nell’anno 2050 una terribile malattia infettiva si e' scatenata sull’intero Globo causando negli umani "+"\n"+
    			"atteggiamenti quali stati di collera e desiderio incessante di nutrirsi di carne umana. \r\n" + 
    			"Con il passare degli anni piu' e piu' persone sono morte e intere città andate in rovina. " +"\n"+ 
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
    			" - GUARDA qualcosa, ad esempio GUARDA LA PROVETTA\r\n\n" + 
    			"Non usare frasi troppo complesse.\r\n\n" + 
    			"Ricorda che hai a disposizione un inventario e quando prenderai un oggetto quest’ultimo finirà lì dentro. Stai attento il tuo inventario non e' infinito.\r\n" + 
    			"Altri comandi importanti sono:\r\n" + 
    			" - DOVE ti dice dove ti trovi\r\n" + 
    			" - COSA, INVENTARIO o I elencano il contenuto del tuo inventario\r\n" + 
    			" - SAVE serve a registrare la situazione su disco e\r\n" + 
    			" - LOAD a caricare la partita (comando disponibile nel menu')\r\n" + 
    			" - BASTA ti riporta al menu' principale e\r\n" + 
    			" - ISTRUZIONI ti mostra tutto ciò che hai appena visto (comando disponibile nel menu')\r\n\n" + 
    			"Divertiti con la nostra Apocalisse Testuale!\r\n" + 
    			"");
    }
    
    public void stampaQuadrato() {
    	System.out.println();
    	System.out.println("\r\n" + 
    			" ╔═══╦═══╦═══╦═══╗\r\n" + 
    			" ║   ║ ╔═╬═╗ ║   ║\r\n" + 
    			" ╠═══╬═╣═╬═╬═╬═══╣\r\n" + 
    			" ║   ║ ╚═╬═╝ ║   ║\r\n" + 
    			" ╠═══╬═══╬═══╬═══╣\r\n" + 
    			" ║   ║ ╔═╬═╗ ║   ║\r\n" + 
    			" ╠═══╬═╣═╬═╬═╬═══╣\r\n" + 
    			" ║   ║ ╚═╬═╝ ║   ║\r\n" + 
    			" ╚═══╩═══╩═══╩═══╝");
    	System.out.println();
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

