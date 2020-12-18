import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.*;
import java.text.DecimalFormat;

public class MainAgent extends Agent {

    private GUI gui;
    private AID[] playerAgents;
    private GameParametersStruct parameters = new GameParametersStruct();
    
    private boolean stop;

    @Override
    protected void setup() {
        gui = new GUI(this);
        System.setOut(new PrintStream(gui.getLoggingOutputStream()));

        updatePlayers();
        gui.logLine("Agent " + getAID().getName() + " is ready.");
    }

    public void updatePlayers() {
        gui.logLine("Updating player list");
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Player");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0) {
                gui.logLine("Found " + result.length + " players");
            }
            playerAgents = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                playerAgents[i] = result[i].getName();
            }
        } catch (FIPAException fe) {
            gui.logLine(fe.getMessage());
        }
        //Provisional
        String[] playerNames = new String[playerAgents.length];
        for (int i = 0; i < playerAgents.length; i++) {
            playerNames[i] = playerAgents[i].getLocalName();
        }
        // reseting table
        gui.resetTable();
        gui.setPlayersUI(playerNames);
        
        // reseting parameters and setting gui labels (N, R, games played)
        parameters.N = playerNames.length;
        gui.setPlayersPlaying(String.valueOf(parameters.N));
        gui.setRoundsMaximum(String.valueOf(parameters.R));
        gui.setGamesPlayed("0");
        return;
    }
    
    public void deletePlayer(String name) {
    	for (int i = 0; i < playerAgents.length; i++) {
    		String playerName = playerAgents[i].getLocalName();
    		if (playerName.equals(name.trim())) {
    			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    			msg.addReceiver(playerAgents[i]);
    			msg.setContent("Delete");
    			send(msg);
    			gui.logLine("Agent " + playerName + " deleted");
    			gui.removePlayerRow(playerName);
    			return;
    		}
    	}
    }

    public int newGame() {
    	gui.setGamesPlayed("0");
    	gui.enableGameButtons(false);
    	gui.enableResetButtons(false);
    	if (playerAgents.length < 2) {
    		gui.logLine("Only one agent available, we need at least two to play");
    	}
        addBehaviour(new GameManager());
        return 0;
    }
    
    public void setRounds(int R) {
    	parameters.R = R;
    }
    
    public void setNumberOfPlayers(int N) {
    	parameters.N = N;
    }
    
    public void stopGame() {
    	stop = true;
    }
    
    public void resumeGame() {
    	stop = false;
    	doWake();
    }

    /**
     * In this behavior this agent manages the course of a match during all the
     * rounds.
     */
    private class GameManager extends SimpleBehaviour {

        @Override
        public void action() {
            //Assign the IDs
            ArrayList<PlayerInformation> players = new ArrayList<>();
            int lastId = 0;
            for (AID a : playerAgents) {
                players.add(new PlayerInformation(a, lastId++));
            }
            

            //Initialize (inform ID)
            for (PlayerInformation player : players) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent("Id#" + player.id + "#" + parameters.N + "," + parameters.R);
                msg.addReceiver(player.aid);
                send(msg);
            }
            //Organize the matches
            int gamesPlayed=0;
            for (int i = 0; i < players.size(); i++) {
                for (int j = i + 1; j < players.size(); j++) {
                	int numRounds = (int) Math.round(((float) Math.random()/10) +  0.9*parameters.R);
                    for (int r = 0; r < numRounds; r++) {
                    	if (!stop) {
                    		playGame(players.get(i), players.get(j));
                    	} else {
                    		doWait();
                    	}
                    }
                    endGame(players.get(i), players.get(j));
                    gamesPlayed++;
                    gui.setGamesPlayed(String.valueOf(gamesPlayed));
                }
            }
            tournamentEnd(players);
        }

        private void playGame(PlayerInformation player1, PlayerInformation player2) {
            //Assuming player1.id < player2.id
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(player1.aid);
            msg.addReceiver(player2.aid);
            msg.setContent("NewGame#" + player1.id + "," + player2.id);
            send(msg);
            

            msg = new ACLMessage(ACLMessage.REQUEST);
            msg.setContent("Action");
            msg.addReceiver(player1.aid);
            send(msg);

            ACLMessage move1 = blockingReceive();
            gui.logLine("Main Received " + move1.getContent() + " from " + move1.getSender().getName());
            String action1 = move1.getContent().split("#")[1];

            msg = new ACLMessage(ACLMessage.REQUEST);
            msg.setContent("Action");
            msg.addReceiver(player2.aid);
            send(msg);

            ACLMessage move2 = blockingReceive();
            gui.logLine("Main Received " + move1.getContent() + " from " + move1.getSender().getName());
            String action2 = move2.getContent().split("#")[1];
            
            String payoff = calcPayoffs(action1, action2, player1, player2);

            msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(player1.aid);
            msg.addReceiver(player2.aid);
            msg.setContent("Results#" + player1.id + "," + player2.id + "#" + payoff);
            send(msg);
        }
        
        private void endGame(PlayerInformation player1, PlayerInformation player2) {
        	String endAvgPayoffs = calcEndAvgPayoffs(player1, player2);
        	ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(player1.aid);
			msg.addReceiver(player2.aid);
			msg.setContent("GameOver#" + player1.id + "," + player2.id + "#" + endAvgPayoffs);
			send(msg);
			
			gui.updatePlayerTable(player1);
			gui.updatePlayerTable(player2);
        }
        
        private void tournamentEnd(ArrayList<PlayerInformation> players) {
        	gui.logLine(" TOURNAMENT OVER\n ");
        	for (int i=0; i < players.size(); i++) {
        		players.get(i).calcFinalPayoff();
        		gui.logLine(players.get(i).aid.getName() + " GAMES WON : " + String.valueOf(players.get(i).gamesWon));
        	}
        	Collections.sort(players, new Comparator<PlayerInformation>() {
        		  public int compare(PlayerInformation c1, PlayerInformation c2) {
        			  int comp = Double.compare(c1.finalPayoff, c2.finalPayoff);
        			  if (comp < 0) return 1;
        			  else if (comp > 0) return -1;
        			  return 0;
        		  }});
        	boolean winnerExists = checkForWinner(players);
        	String winner = players.get(0).aid.getLocalName();
        	gui.showWinner(winner, winnerExists);
        }

        @Override
        public boolean done() {
        	gui.enableGameButtons(true);
        	gui.enableResetButtons(true);
            return true;
        }
    }
    
    private String calcPayoffs(String action1, String action2, PlayerInformation player1, PlayerInformation player2) {
    	String actions = action1 + action2;
    	String ret="";
		if (actions.equals("CC")){
			player1.playerEqual("C");
			player2.playerEqual("C");
			ret = "C,C#3,3";
		} else if(actions.equals("DD")) {
			player1.playerEqual("D");
			player2.playerEqual("D");
			ret = "D,D#1,1";
		} else if(actions.equals("CD")) {
			player2.playerWon();
			player1.playerLost();
			ret = "C,D#0,5";
		} else if (actions.equals("DC")) {
			player1.playerWon();
			player2.playerLost();
			ret = "D,C#5,0";
		}
		player1.round++;
		player2.round++;
    	return ret;
    }
    
    private String calcEndAvgPayoffs(PlayerInformation player1, PlayerInformation player2) {
    	DecimalFormat format = new DecimalFormat("#.###");
    	float payoff1 = (float) player1.payoff / player1.round;
    	float payoff2 = (float) player2.payoff / player2.round;
    	
    	player1.addPayoff(payoff1);
    	player2.addPayoff(payoff2);
    	
    	player1.gamesPlayed++;
    	player2.gamesPlayed++;
    	if (payoff1 > payoff2) {
    		player1.gamesWon++;
    	} else if(payoff2 > payoff1) {
    		player2.gamesWon++;
    	}
    	
    	String result = format.format(payoff1) + "," + format.format(payoff2);
    	
    	player1.resetPayoff();
    	player2.resetPayoff();
    	
    	return result;
    }
    
    private boolean checkForWinner(ArrayList<PlayerInformation> players) {
    	boolean winnerExists = false;
    	double first = players.get(0).finalPayoff;
    	for(int i = 1; i < players.size() && !winnerExists; i++)
    	{
    	  if (players.get(i).finalPayoff != first) winnerExists = true;
    	}
    	if (winnerExists) return true;
    	return false;
    }

    public class PlayerInformation {
    	/* class has information about :
    	 * - player's current payoff and round
    	 * - player's games played
    	 * - player's average payoff in each game
    	 * - players cumulative number of cooperations and defections
    	*/ 
        AID aid;
        int id;
        int payoff, round;
        
        int gamesPlayed, gamesWon;
        int defections, cooperations;
        
        ArrayList<Float> avgPayoff;
        double finalPayoff;
        
        public PlayerInformation(AID a, int i) {
            aid = a;
            id = i;
            payoff=0;
            gamesPlayed=0;
            gamesWon=0;
            cooperations=0;
            defections=0;
            round=0;
            avgPayoff = new ArrayList<Float>();
            finalPayoff=0;
            
        }

        @Override
        public boolean equals(Object o) {
            return aid.equals(o);
        }
        
        public void resetPayoff() {
        	payoff=0;
        	round=0;
        }
        
        public void playerWon() {
        	defections++;
        	payoff+=5;
        }
        
        public void playerLost() {
        	cooperations++;
        }
        
        public void playerEqual(String move) {
        	if (move.equals("C")) {
        		payoff+=3;
        		cooperations++;
        	} else {
        		payoff+=1;
        		defections++;
        	}
        }
        
        public void addPayoff(float payoff) {
        	avgPayoff.add(payoff);
        }
        
        public void calcFinalPayoff() {
        	OptionalDouble average = avgPayoff
                    .stream()
                    .mapToDouble(a -> a)
                    .average();
        	finalPayoff = average.isPresent() ? average.getAsDouble() : 0;
        }
    }

    public class GameParametersStruct {

        int N;
        int R;

        public GameParametersStruct() {
            N = 2;
            R = 5;
        }
    }
}
