package strategybots.bots;

/* Author: Adrian Shedley with help from Alec Dorrington
 * Date: 4 Dec 2019
 * 
 * A MCTS based Othello bot. Currently still in the debug and dev phase, however still plays well against a moderate human.
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import strategybots.games.Reversi;
import strategybots.games.base.Game.Player;

public class TipOthello implements Player<Reversi>{

	private long time = 2000l;
	private Random rand;
	private int globalSims = 0;
	private static float learningRate = 1.41f;
	private static int branch = 64;
	
    private Node root = null;
	
	public TipOthello() {
		rand = new Random();
		System.out.println("Tip's Othello Bot Loaded");
	}
	
	public TipOthello(long millis) {
		this();
		this.time = millis;
	}
	
	@Override
	public void takeTurn(Reversi game, int playerId) {

		long start = System.currentTimeMillis();
		int[][] board = getBoard(game);
        
        globalSims = 0;
        
        Vec2 move = UCTSearch(board, playerId); //bestMove(board, playerId);
        
        for (Node child : root.children) {
        	if (child == null) {
        		System.out.println("Root's Child is null");
        	} else {
        		System.out.println("Root's Child has [" + child.wins[1] + " / " + child.wins[2] + "] of " + child.sims);
        	}
        }
	            
        game.placeDisc(move.x, move.y);
		printStats(playerId, move, start, bestChild(root));
		
	}
	
	//Done
	private Vec2 UCTSearch(int[][] board, int playerId) {
		long start = System.currentTimeMillis();
		int opp = 3-playerId;
		root = new Node(board, opp, 0, 0);
		root.populateChildren();
		
		// use up all the time
		while (System.currentTimeMillis() - start < time) {

			Node leaf = treePolicy(root);
			
			int[] result = defaultPolicyMulti(leaf);
			leaf.visited = true;
			
			backup(leaf, result);
		}
		
		return bestChild(root).move;
	}
	
	//Done
	private Node treePolicy(Node nodeIn) {
		
		Node node = nodeIn;
		
		if (node != null) {
			while (generateTerminal(node) == -1) {
							
				if (node.hasUnexploredChildren()) {
					
					//System.out.println(node.pieces[0] + ", " + node.pieces[1]);
					Node temp = expand(node);
					if (temp == null) System.out.println("we got him boys");
					return temp;
					
				} else {
					
					// This is some hacky workaround for a problem I dont understand
					if (node.children.size() == 0) {
						
						// Swap Players
						if (!node.subTerminal) {
							Node flipChild = new Node(node.getBoard(), 3-node.player, node.move);
							flipChild.pieces[0] = node.pieces[0];
							flipChild.pieces[1] = node.pieces[1];
							flipChild.subTerminal = true;
							node.children.add(flipChild);
						} else { 
							node.terminal = node.pieces[0] > node.pieces[1] ? 1 : (node.pieces[0] == node.pieces[1] ? 0 : 2);
						}

					}
					
					Node temp = bestChildUTC(node);
					if (temp == null) {
						//System.out.println("better luck next time");
						//System.out.println(node.pieces[0] + " , " + node.pieces[1]);
						return node;
					}
					node = temp;
				}
							
			}
		}
		
		return node;
	}
	
	//Done
	private Node expand(Node parent) {
		
		ArrayList<Node> children = parent.getUnexploredChildren();
		int rando = rand.nextInt(children.size());
		
		Node child = children.get(rando);
		child.visited = true;
		
		if (generateTerminal(child) == -1) { child.populateChildren(); }
		
		return child;
		
	}
	
	//Done
	private Node bestChildUTC(Node parent) {
		
		Node bestChild = null;
		double bestChildValue = -1.0f;
		
		if (parent.children.size() == 0) System.out.println("There are " + parent.children.size() + " childs");
		
		for (Node child : parent.children) {
			
			int parentSims = (child.parent == null ? globalSims : parent.sims) ;
			double value = (child.wins[parent.player] / (double)child.sims) + learningRate * Math.sqrt(2.0 * Math.log(parentSims) / (double)child.sims);
			
			if (value > bestChildValue) {
				bestChildValue = value;
				bestChild = child;
			}
		}
	
		return bestChild;
	}
	
	private int[] defaultPolicyMulti(Node leafIn) {
		int[] reward = new int[4];
		
		int player = 3-leafIn.player;
		ArrayList<Vec2> moves = getValidPositions(leafIn.getBoard(), player);
		reward[3] = moves.size();
		
		Integer multiResult = moves
	    		.parallelStream()
	    		.map(x -> defaultPolicy(leafIn, x))	
	    		.reduce(0, (r1, r2) -> r1 + r2);
	    	
    	reward[1] = multiResult % (branch+1);
    	reward[2] = multiResult / (branch+1);
    	
    	reward[0] = reward[3] - reward[1] - reward[2];
    	
    	globalSims += reward[3];
		
		return reward;
		
	}
	
	// Done
	private int defaultPolicy(Node leafIn, Vec2 move) {
		
		Node leaf = new Node(leafIn.getBoard(), leafIn.player, leafIn.move);
		
		int reward = 0;

		int terminal = generateTerminal(leaf);
				
		while (terminal == -1) {
						
			int player = 3-leaf.player;
		
			if (move != null) {
				
				Set<Vec2> toFlip = getFlipped(leaf.getBoard(), player, move);
				
				leaf.getBoard()[move.x][move.y] = player;
	    		leaf.pieces[player-1]++;
	    		
	    		for (Vec2 stone : toFlip) {
	    			leaf.getBoard()[stone.x][stone.y] = player;
	    			leaf.pieces[player-1]++;
	    			leaf.pieces[(3-player)-1]--;
	    		}	

			}

			leaf.player = player;
	    	terminal = generateTerminal(leaf);
	    	
		}
		
    	if (terminal == 1) reward += 1;
    	if (terminal == 2) reward += (branch+1);

		return reward;
	}
	
	// Done
	private void backup(Node childIn, int[] result) {
		
		Node child = childIn;
		
		do {
			child.sims += result[3];
			child.updateWins(result);
			
			child = child.parent;
		} while (child != null);
	}
	
    // Get the bets child based on Wins / Sims
    private Node bestChild(Node root) {
    	
    	Node maxChild = null; 
    	float maxChildValue = -1.f;
    	int player = 3-root.player;
    	
    	for (Node child : root.getChildren()) {
    		
    		float myWins = child.wins[player] + 0.0001f;
			float mySims = child.sims + 0.0001f;
			float finalScore = (float)myWins / (float)mySims;
			
			if (finalScore > maxChildValue) {
				maxChild = child;
				maxChildValue = finalScore;
			}
    	}
    	
    	return maxChild;
    }
    
    
	private ArrayList<Vec2> getValidPositions(int[][] board, int player) {
		ArrayList<Vec2> adj = new ArrayList<Vec2>();
		
        int[][] dirs = new int[][] {{1, 0}, {1, 1}, {0, 1}, {-1, 1}};
        int[] signs = new int[] {-1, 1};
		
        int width = board.length;
        int height = board[0].length;
        int opp = 3-player;
        
		for (int x = 0 ; x < width; x++) {
			for (int y = 0 ; y < height; y++) {
				
				if (board[x][y] != 0) continue;
				
				int streak = 0;
				
		        for(int[] dir : dirs) {
		        	
		            for(int sign : signs) {
		                
		            	int substreak = 0;
		            	
		                for(int i = 1; i < width; i++) {
		                    
		                    int xx = x+i*dir[0]*sign;
		                    int yy = y+i*dir[1]*sign;
		                    
		                    if(xx < 0 || xx >= width) break;
		                    if(yy < 0 || yy >= height) break;
		                    
		                    if(board[xx][yy] != opp) {
		                    	if (board[xx][yy] == player && substreak > 0) {
				                	streak += substreak;
				                }
		                    	break;
		                    }
		                    
		                    substreak++;
		                }
		            }
		        }
		        
		        if (streak > 0) {
		        	adj.add(new Vec2(x, y, streak));
		        }
			}
		}
		
		return adj;
	}
	
    /**
     * Determines which discs are to be captured if a disc
     * is placed at the given position by the given player.
     * @param board the int array board
     * @param playerId the ID of the player making the move.
     * @param x the x position the disc is placed at.
     * @param y the y position the disc is placed at.
     * @return a set of all the opponent discs which are captured, if any.
     */
    private Set<Vec2> getFlipped(int[][] board, int playerId, Vec2 move) {
        
        Set<Vec2> flipped = new HashSet<>(), pending = new HashSet<>();
        
        //For each direction in which to check for captures.
        for(int i = -1; i <= 1; i++) {
            j_loop: for(int j = -1; j <= 1; j++) {
                
                if(i == 0 && j == 0) continue;
                
                pending.clear();
                
                //Continue searching in this direction.
                for(int dist = 1;; dist++) {
                    
                    //Determine the current position to check for a disc.
                    //Position = Placement + Direction * Distance.
                    int xx = move.x + i * dist;
                    int yy = move.y + j * dist;
                    
                    //If this tile is in bounds and not empty.
                    if(xx >= 0 && xx < board.length && yy >= 0 && yy < board[0].length
                            && board[xx][yy] != 0) {
                        
                        //If this tile is occupied by the opponent player.
                        if(board[xx][yy] != playerId) {
                            //Add the disc to the set of discs which MIGHT be added.
                            pending.add(new Vec2(xx, yy));
                            
                        //If this tile is occupied by the current player.
                        } else {
                            //An enclosed chain has been found.
                            flipped.addAll(pending);
                            continue j_loop;
                        }
                    //Empty tile/edge of board has been reached before a friendly piece.
                    } else continue j_loop;
                }
            }
        }
        return flipped;
    }
    
	// Play move
	public Node generateChild(Node parent, Vec2 move) {
		
		// encode new child with its ID, a Move and a tempBoard.
		Node newChild = new Node(parent, parent.getBoard(), 3-parent.player, move);
		
		// Board will now have been deepCopied, we can edit in place.
		Set<Vec2> toFlip = getFlipped(newChild.getBoard(), newChild.player, move);
		
		newChild.getBoard()[move.x][move.y] = newChild.player;
		newChild.pieces[newChild.player-1]++;
		
		for (Vec2 stone : toFlip) {
			newChild.getBoard()[stone.x][stone.y] = newChild.player;
			newChild.pieces[newChild.player-1]++;
			newChild.pieces[(3-newChild.player)-1]--;
		}
		
		newChild.terminal = generateTerminal(newChild);
		
		return newChild;
	}
	
	private int generateTerminal(Node leaf) {
		int terminal = -1;

		if (leaf.pieces[0] + leaf.pieces[1] >= branch || 
				(getValidPositions(leaf.getBoard(), 1).size() == 0) &&
				(getValidPositions(leaf.getBoard(), 2).size() == 0)) {
			if (leaf.pieces[0] > leaf.pieces[1]) {
				terminal = 1;
			} else if (leaf.pieces[0] == leaf.pieces[1]) {
				terminal = 0;
			} else {
				terminal = 2;
			}
		}
				
		return terminal;
		
	}
	
	private int[][] getBoard(Reversi game) {

		int[][] board = new int[game.getWidth()][game.getHeight()];
		
		for (int xx = 0 ; xx < game.getWidth(); xx++) {
			for (int yy = 0 ; yy < game.getHeight(); yy++) {
				board[xx][yy] = game.getDisc(xx, yy);
			}
			
		}
		
		return board;
		
	}
	
    @Override
    public String getName() { return "TipTacos's Othello MCTS"; }
	
	private void printStats(int playerId, Vec2 move, long start, Node best) {
        System.out.println("=======================");
        System.out.println("TipMCTS Statistics:");
        System.out.println("Player:      " + playerId
                + " ("+(playerId==1?"Yellow":"Red")+")");
        //System.out.println("Turn:        " + turn++);
        //System.out.println("Expectation: " + move[0]);
        System.out.println("Move:        [" + move.x + ", " + move.y + "]"); 
        System.out.println("Win Probab.  " + Math.round((best.wins[playerId] / (double)best.sims) * 1000.0) / 10.0 + "%"); 
        //System.out.println("Depth:       " + move[2]);
        System.out.println("Global Sims: " + globalSims);
        System.out.println("Time:        "
                + (System.currentTimeMillis() - start) + "ms");
	}
	
    class Vec2 {
    	
    	int x = 0, y = 0;
    	int prio = 0;
    	
    	public Vec2() {}
    	
    	public Vec2 (int x, int y) {
    		this.x = x;
    		this.y = y;
    	}
    	
    	public Vec2 (int x, int y, int prio) {
    		this(x, y);
    		this.prio = prio;
    	}
    }
    
    // NODE CLASS
	class Node {
		
		private Node parent;
		private ArrayList<Node> children = new ArrayList<Node>();
		private boolean visited = false;
		private int[][] board;
		private Vec2 move;
		private int wins[] = new int[3], sims;
		private int player;
		private int[] pieces = new int[] {2, 2};
		private int terminal = -1;
		private boolean subTerminal = false;
		
		public Node(Node parent, int[][] board, int player, int x, int y) {
			
			this(board, player, x, y);
			recount();
		}
		
		public Node(Node parent, int[][] board, int player, Vec2 move) {
			
			this(board, player, move);
			this.parent = parent;
			recount();
		}
		
		public Node(int[][] board, int player, int x, int y) {
			
			this(board, player, new Vec2(x, y));
			
		}
		
		public Node(int[][] board, int player, Vec2 move) {
			
			this.board = deepCopy(board);
			this.player = player;
			this.move = move;
			this.pieces = new int[] {2, 2};
			
		}
		
		private void recount() {
			pieces = new int[2];
			
			for (int xx = 0 ; xx < board.length; xx++ ) {
				for (int yy = 0; yy < board[xx].length; yy++) {
					if (board[xx][yy] == 1) pieces[0]++;
					if (board[xx][yy] == 2) pieces[1]++;
				}
			}
		}
		
		// Return the score including the learning rate parameter
		public float score() {
			
			int parentSims = 0;
			if (parent == null) {
				parentSims = globalSims;
			} else {
				parentSims = parent.sims;
			}
			
			return (float) (((float)(wins[player]) / (float)sims) + learningRate * Math.sqrt(2.0 * Math.log(parentSims) / (float)sims));
			
		}
		
		// If there is at least one unexplored child.
		public boolean hasUnexploredChildren() {
			
			return getUnexploredChildren().size() > 0;
			
		}
		
		public void updateWins(int[] newWins) {
			
			for (int ii = 0; ii < 3; ii++) {
				//System.out.print("chnaged from " + wins[ii]);
				wins[ii] += newWins[ii];
			}
			
		}
		
		// Get the integer for index of each of the non-null children
		private ArrayList<Node> getUnexploredChildren() {
			
			ArrayList<Node> unexplored = new ArrayList<Node>();
								
			for (Node child : children) {
								
				if (!child.visited) { 
					unexplored.add(child);
				}

			}
						
			return unexplored;
			
		}
		
		// Populate children
		public void populateChildren() {
			
			ArrayList<Vec2> validMoves = getValidPositions(board, 3-player);
			
			for (Vec2 move : validMoves) {
				Node newChild = generateChild(this, move);
				children.add(newChild);
			}
			
			// TODO
			children.sort(movePriorityComparator);
						
		}
		
		// Return a random child that has been unexpored
		public Node getRandomChild() {
			
			ArrayList<Node> unexplored = getUnexploredChildren();
			int index = rand.nextInt(unexplored.size());
			
			return unexplored.get(index);
			
		}
		
		public Node getChildFromMove(Vec2 move) {
			
			for (Node child : children) {
				
				if (child.move.x == move.x && child.move.y == move.y) {
					return child;
				}

			}
			return null;
		}
		
		public ArrayList<Node> getChildren() {
			
			return children;
			
		}
		
		public int[][] getBoard() {
			
			return board;
			
		}
		
		private int[][] deepCopy(int[][] board) {
			
			int[][] newBoard = new int[board.length][board[0].length];
			
			for (int xx = 0 ; xx < board.length; xx++ ) {
				for (int yy = 0; yy < board[xx].length; yy++) {
					newBoard[xx][yy] = board[xx][yy];
				}
			}
			
			return newBoard;
			
		}
		
		public final Comparator<Node> movePriorityComparator = new Comparator<Node>() {         
			@Override         
			public int compare(Node n1, Node n2) {             
				return (n2.move.prio < n1.move.prio ? -1 :                     
					(n2.move.prio == n1.move.prio ? 0 : 1));           
			}     
		}; 
		
	}
	
}
