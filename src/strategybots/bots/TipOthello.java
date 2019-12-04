package strategybots.bots;

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
	private static int bottomIterations = Runtime.getRuntime().availableProcessors();
	
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
				
		 Vec2 move = bestMove(board, playerId);
	        
        root = root.getChildFromMove(move);
        
        game.placeDisc(move.x, move.y);
		
		printStats(playerId, move, start);
		
	}
	
	// Use MCTS to find the bets move
	private Vec2 bestMove(int[][] board, int playerId) {
        
        long start = System.currentTimeMillis();
        
        globalSims = 0;
        
       /* if (root == null) {
        	root = new Node(board, 3-playerId, 0, 0);
        } else {
        	System.out.println(root);
        	rebaseTree(root, board);
        	globalSims = root.sims;
        	System.out.println(root);
        }*/
        
    	root = new Node(board, 3-playerId, 0, 0);
        root.populateChildren();
        System.out.println(root.children.size());
        
        while (System.currentTimeMillis()-start < time) { 
        	// Do one iteration while we still have time
        	MCTS_rec(root, 0);
        }

        System.out.println("Generated " + root.children.size() + " root's children");
        for (Node child : root.children) {
        	if (child == null) {
        		System.out.println("Root's Child is null");
        	} else {
        		System.out.println("Root's Child has [" + child.wins[1] + " / " + child.wins[2] + "] of " + child.sims);
        	}
        }
        
        Node bestChild = bestChild(root);
        Vec2 move = bestChild.move;
        
        return move;
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
	
    // begin MCTS
    public int[] MCTS_rec(Node subroot, int depth) {
    	
    	int[] newWins = new int[] {0, 0, 0, 1}; 
    	
    	if (subroot == null) { return newWins; }
    	
    	if (subroot.terminal != -1) {
    		
    		newWins[0] += (subroot.terminal == 0 ? 1 : 0);
    		newWins[1] += (subroot.terminal == 1 ? 1 : 0);
    		newWins[2] += (subroot.terminal == 2 ? 1 : 0);
    		
    		subroot.sims += newWins[3];
        	subroot.updateWins(newWins);
    		
    		return newWins;
    	}
    	
    	if (subroot.hasUnexploredChildren()) {
    		// Select rand child
    		Node randChild = subroot.getRandomChild();
    		
    		if (randChild.terminal == -1) {
    		
	    		// Rollout child
	    		newWins = rollout(randChild);
	    		
	    		// Generate children of node
	    		randChild.populateChildren();
	    		
	    		randChild.visited = true;
	    		
	    		randChild.sims += newWins[3];
	    		randChild.updateWins(newWins);
	    		
    		} else {
    			
        		newWins[0] += (randChild.terminal == 0 ? 1 : 0);
        		newWins[1] += (randChild.terminal == 1 ? 1 : 0);
        		newWins[2] += (randChild.terminal == 2 ? 1 : 0);
        		
        		randChild.sims += newWins[3];
        		randChild.updateWins(newWins);
        		
    		}
    		
    	} else { // No unexpored, pick by value
    		Node selected = selectNode(subroot);
      		newWins = MCTS_rec(selected, depth + 1);
    	}
    	
    	subroot.sims += newWins[3];
    	subroot.updateWins(newWins);
    	
		return newWins;
		
    }
    
    // Select the best node from the subroot's list of children
    private Node selectNode(Node subroot) {
    	
    	Node maxChild = null; 
    	float maxChildValue = 0.0f;
    	
    	for (Node child : subroot.getChildren()) {
			if (child.score() > maxChildValue) {
				maxChild = child;
				maxChildValue = child.score();
			}
    	}

    	return maxChild;
    }
    
    public int[] rollout(Node child) {
    	
    	// TODO add multiple rollouts
    	ArrayList<Vec2> moves = getValidPositions(child.getBoard(), 3-child.player);
    	
    	int[] results = new int[] {0, 0, 0, 0};
    	
    	results[3] = moves.size();
    	
    	// new code
    	ArrayList<Node> childTemps = new ArrayList<Node>();
    	
    	for (int ii = 0 ; ii < Math.min(moves.size(), bottomIterations); ii++) {
    		
    		Vec2 move = moves.remove(rand.nextInt(moves.size()));
    		Node tempChild = generateChild(child, move);
    		childTemps.add(tempChild);
    		
    	}
    	
    	Integer result = childTemps
    		.parallelStream()
    		.map(this::moveResult)	
    		.reduce(0, (r1, r2) -> r1 + r2);
    	
    	results[1] = result % (branch+1);
    	results[2] = result / (branch+1);
    	
    	results[0] = results[3] - results[1] - results[2];
    	
    	globalSims += results[3];
    	
    	return results;
    	
    }
    
	private Vec2 getRandomMove(int[][] board, int player) {
		
		ArrayList<Vec2> moves = getValidPositions(board, player);
				
		if (moves.size() > 0) {
			int index = rand.nextInt(moves.size());
			return moves.get(index);
		} else {
			return null;
		}
	}
    
    // Returns 0 for draw, 1 for win 1, and b+1 for win 2
    private Integer moveResult(Node tempNode) {
    	Integer result = 0;
    	
    	int playerMove = tempNode.player;
    	Vec2 move = new Vec2();
    	// Check if the opposition has just placed a winning piece and the board is not Full
    	// 0 = full and draw, -1 = not terminal, 1 is player 1 win, 2 is player2
    	int terminal = generateTerminal(tempNode);
    	
    	
    	// Continue until someone wins
    	while (terminal == -1) {
    		
    		//System.out.println("Not terminal " + tempNode.pieces[0] + " , " + tempNode.pieces[1] + " , " + playerMove);
    		// Starting at the base child node, select a move and update the playerID and last position as well as board
    		playerMove = 3 - tempNode.player;
    		move = getRandomMove(tempNode.getBoard(), playerMove);
    		
    		// do move and swap PlayerID
    		if (move != null) {
	    		// Board will now have been deepCopied, we can edit in place.
	    		Set<Vec2> toFlip = getFlipped(tempNode.getBoard(), playerMove, move);
	    		
	    		tempNode.getBoard()[move.x][move.y] = playerMove;
	    		tempNode.pieces[playerMove-1]++;
	    		
	    		for (Vec2 stone : toFlip) {
	    			tempNode.getBoard()[stone.x][stone.y] = playerMove;
	    			tempNode.pieces[playerMove-1]++;
	    			tempNode.pieces[(3-playerMove)-1]--;
	    		}
	    		
    		}

    		tempNode.player = playerMove;
    		terminal = generateTerminal(tempNode);

    	}
    	//System.out.println("Terminal");
    	
    	// Check if the last valid move in the rollout was ours, and a win
    	if (terminal == 1) result += 1;
    	if (terminal == 2) result += (branch+1);
    	
    	return result;
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
		
		if (leaf.pieces[0] + leaf.pieces[1] == branch || 
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
	
	private void printStats(int playerId, Vec2 move, long start) {
        System.out.println("=======================");
        System.out.println("TipMCTS Statistics:");
        System.out.println("Player:      " + playerId
                + " ("+(playerId==1?"Yellow":"Red")+")");
        //System.out.println("Turn:        " + turn++);
        //System.out.println("Expectation: " + move[0]);
        System.out.println("Move:        [" + move.x + ", " + move.y + "]");        
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
		private int[] pieces = new int[2];
		private int terminal = -1;
		
		public Node(Node parent, int[][] board, int player, int x, int y) {
			
			this(board, player, x, y);
			this.pieces[0] = parent.pieces[0];
			this.pieces[1] = parent.pieces[1];
			//recount();
		}
		
		public Node(Node parent, int[][] board, int player, Vec2 move) {
			
			this(board, player, move);
			this.parent = parent;
			this.pieces[0] = parent.pieces[0];
			this.pieces[1] = parent.pieces[1];
			//recount();
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
			
			return (float) (((float)(wins[player]) / (float)sims) + learningRate * Math.sqrt(Math.log(parentSims) / (float)sims));
			
		}
		
		// If there is at least one unexplored child. TODO make this a stored value
		public boolean hasUnexploredChildren() {
			
			return getUnexploredChildren().size() > 0;
			
		}
		
		public void updateWins(int[] newWins) {
			
			for (int ii = 0; ii < 3; ii++) {
				//System.out.print("chnaged from " + wins[ii]);
				wins[ii] += newWins[ii];
				//System.out.println(" to " + wins[ii]);
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
