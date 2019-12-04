package strategybots.bots;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

import strategybots.games.ConnectFour;
import strategybots.games.base.Game.Player;

public class TipC4_Adv implements Player<ConnectFour> {
    
    private ConnectFour game;
    private long time = 2000;
    private int turn = 1;
    
    private static int b = 7; // Branching
    private static float learningRate = 1.41f;  // Learning rate
    private static int globalSims = 0;
    private static Random rand = new Random();
    
    private Node root = null;
    
    public TipC4_Adv() {}
    
    public TipC4_Adv(long time) { this.time = time; }
        
    @Override
    public void init(ConnectFour game, int playerId) {
        this.game = game;
        b = game.getWidth();
    }

    @Override
    public void takeTurn(ConnectFour game, int playerId) {
        
        long start = System.currentTimeMillis();
                        
        int move = bestMove(getBoard(), playerId);
        
        root = root.getChildren()[move];
        
        game.placeStone(move);
        
        System.out.println("=======================");
        System.out.println("TipC4_Adv Statistics:");
        System.out.println("Player:      " + playerId
                + " ("+(playerId==1?"Yellow":"Red")+")");
        System.out.println("Turn:        " + turn++);
        //System.out.println("Expectation: " + move[0]);
        System.out.println("Column:      " + (move+1));
        //System.out.println("Depth:       " + move[2]);
        System.out.println("Global Sims: " + globalSims);
        System.out.println("Time:        "
                + (System.currentTimeMillis() - start) + "ms");
    }
    
    private int bestMove(int[][] board, int playerId) {
        
        long start = System.currentTimeMillis();
        
        globalSims = 0;
        
        if (root == null) {
        	root = new Node(board, 3-playerId, 0, 0);
        } else {
        	System.out.println(root);
        	rebaseTree(root, board, 3-playerId);
        	globalSims = root.sims;
        	System.out.println(root);
        }
        
        //root = new Node(board, 3-playerId, 0, 0);
       
        root.populateChildren();
        //System.out.println(root.children[0]);
        
        while (System.currentTimeMillis()-start < time) { 
        	// Do one iteration
        	MCTS_rec(root, 0);
        }

        for (Node child : root.children) {
        	
        	if (child == null) {
        		System.out.println("Root's Child is null");
        	} else {
        		System.out.println("Root's Child has [" + child.wins[1] + " / " + child.wins[2] + "] of " + child.sims);
        	}
        	
        }
        
        int move = Math.round(bestChild(root)[0]);
        
        return move;
    }
    
    // Get the bets child based on Wins / Sims
    private float[] bestChild(Node root) {
    	
    	int maxChildId = 0; 
    	float maxChildValue = -1.f;
    	int player = 3-root.player;
    	
    	for (int ii = 0 ; ii < b ; ii++) {
    		if (root.getChildren()[ii] != null) { 
    			float myWins = root.getChildren()[ii].wins[player] + 0.0001f;
    			float mySims = root.getChildren()[ii].sims + 0.0001f;
    			float finalScore = (float)myWins / (float)mySims;
    			
    			if (finalScore > maxChildValue) {
    				maxChildId = ii;
    				maxChildValue = finalScore;
    			}
    		}
    	}
    	
    	return new float[] {maxChildId, maxChildValue};
    }
    
    private void rebaseTree(Node root, int[][] newBoard, int player) {
    	
    	long[] boardBitmaps = getBitmaps(newBoard, player);
    	
    	for (Node child : root.children) {
    		
    		if (child != null) {
	    		
				if (child.mask == boardBitmaps[1]) {
	    			this.root = child;
	    			break;
	    		}
	    		
    		}
    		
    	}
    	
    }
    
    private int getStackSize(long mask, int col) {
        
        for(int y = 0; y < game.getHeight(); y++) {
            if((mask & ( 1l << (col*7 + y))) == 0) return y;
        }
        return game.getHeight();
    }
    
    private int[][] getBoard() {
        
        int[][] board = new int[game.getWidth()][game.getHeight()];
        for(int x = 0; x < game.getWidth(); x++) {
            for(int y = 0; y < game.getHeight(); y++) {
                board[x][y] = game.getStone(x, y);
            }
        }
        
        return board;
        
    }
     
    private boolean isFull(long mask) {
    	
    	return getValidMoves(mask).size() == 0;
    	
    }
    
	private int getRandomMove(long mask) {
		
		ArrayList<Integer> moves = getValidMoves(mask);
		
		int index = rand.nextInt(moves.size());
		return moves.get(index);
		
	}
	
	private ArrayList<Integer> getValidMoves(long mask) {
		
		ArrayList<Integer> moves = new ArrayList<Integer>();
		
		for (int ii = 0 ; ii < b; ii++) {
			if ((mask & (1l << (ii*7 + 5))) == 0) {
				moves.add(ii);
			}
		}
		
		return moves;
		
	}
    
    // begin MCTS
    public int[] MCTS_rec(Node subroot, int depth) {
    	
    	int[] newWins = new int[] {0, 0, 0, 1}; 
    	
    	//System.out.println("Recurse: " + depth);
    	
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
    		//System.out.println("Recursing depth " + depth);
    		newWins = MCTS_rec(selected, depth + 1);
    	}
    	
    	subroot.sims += newWins[3];
    	subroot.updateWins(newWins);
    	
		return newWins;
		
    }
    
    // Returns 0 for draw, 1 for win 1, and b+1 for win 2
    private Integer moveResult(Node tempNode) {
    	Integer result = 0;
    	
    	int playerMove = tempNode.player;
    	int xMove = 0, yMove = 0;
    	// Check if the opposition has just placed a winning piece and the board is not Full
    	int winner = getWinner(tempNode.position, tempNode.mask, tempNode.player);
    	boolean isFull = isFull(tempNode.mask);
    	
    	//System.out.println("Entered");
    	// Continue until someone wins
    	while (winner == 0 && !isFull) {
    		
    		// Starting at the base child node, select a move and update the playerID and last position as well as board
    		// Get column move, randomly
    		xMove = getRandomMove(tempNode.mask);
    		
    		yMove = getStackSize(tempNode.mask, xMove);
    		
    		//System.out.println("No winner " + xMove + " " + yMove);
    		// do move and swap PlayerID
    		playerMove = 3 - tempNode.player;
    		tempNode.position ^= tempNode.mask;
    		tempNode.mask |= (tempNode.mask + (1l << (xMove * 7)));
    		tempNode.x = xMove;
    		tempNode.y = yMove;
    		tempNode.player = playerMove;

    		// Update the terminal condition
    		winner = getWinner(tempNode.position, tempNode.mask, tempNode.player);
        	isFull = isFull(tempNode.mask);
    	}
    	
    	// Check if the last valid move in the rollout was ours, and a win
    	if (winner == 1) { result += 1; }
    	if (winner == 2) { result += (b+1); }
    	
    	return result;
    }
    
    public int[] rollout(Node child) {
    	
    	// TODO add multiple rollouts
    	ArrayList<Integer> moves = getValidMoves(child.mask);
    	
    	int[] results = new int[] {0, 0, 0, 0};
    	
    	results[3] = moves.size();
    	
    	// new code
    	ArrayList<Node> childTemps = new ArrayList<Node>();
    	for (Integer move : moves) {
    		Node tempChild = new Node(child.position, child.mask, child.player, child.x, child.y);
    		
	    	int xMove = (int)move;
    		int yMove = getStackSize(tempChild.mask, xMove);
    		// do move and swap PlayerID
    		int playerMove = 3 - tempChild.player;
    		tempChild.position ^= tempChild.mask;
    		tempChild.mask |= (tempChild.mask + (1l << (xMove * 7)));
    		tempChild.x = xMove;
    		tempChild.y = yMove;
    		tempChild.player = playerMove;
    		
    		childTemps.add(tempChild);
    	}
    	
    	Integer result = childTemps
    		.parallelStream()
    		.map(this::moveResult)	
    		.reduce(0, (r1, r2) -> r1 + r2);
    	
    	results[1] = result % (b+1);
    	results[2] = result / (b+1);
    	
    	results[0] = results[3] - results[1] - results[2];
    	
    	globalSims += results[3];
    	
    	return results;
    	
    }
    
    // Select the best node from the subroot's list of children
    private Node selectNode(Node subroot) {
    	
    	int maxChild = 0; 
    	float maxChildValue = 0.0f;
    	    	
    	for (int ii = 0 ; ii < b ; ii++) {
    		if (subroot.getChildren()[ii] != null) { 
    			if (subroot.getChildren()[ii].score() > maxChildValue) {
    				maxChild = ii;
    				maxChildValue = subroot.getChildren()[ii].score();
    			}
    		}
    	}
    	
    	return subroot.getChildren()[maxChild];
    }
    
    public long[] getBitmaps(int[][] board, int playerId) {
		
		String position = "", mask = "";
		//System.out.println("You called?");
		
		for (int xx = 6 ; xx >= 0; xx-- ) {
			
			position += "0";
			mask += "0"; 
			
			for (int yy = 5; yy >= 0; yy--) {
								
				position += board[xx][yy] == playerId ? "1" : "0" ;
				mask += board[xx][yy] != 0 ? "1" : "0" ;
				
			}
			
		}
		
		long pos = Long.parseUnsignedLong(position, 2);
		long mas = Long.parseUnsignedLong(mask, 2);
		
		System.out.println(position + " " + pos);
		System.out.println(mask + " " + mas);
		
		return new long[] {pos, mas};
		
	}
	
    // return 0 no win, 1 for player 1, 2 for player 2
    public int getWinner(long pos, long mask, int playerPosition) {
    	
    	long opPos = pos ^ mask;
    	
    	if (isWin(pos)) { return playerPosition; }
    	if (isWin(opPos)) { return 3-playerPosition; }
    	
    	return 0;

    }
    
	public boolean isWin(long pos) {
		
		// Horiz
		long m = pos & (pos >> 7);
		if ((m & (m >> 14)) >= 1) { 
			return true;
		}

		// Diag \
		m = pos & (pos >> 6);
		if ((m & (m >> 12)) >= 1) { 
			return true;
		}
		
		// Diag / 
		m = pos & (pos >> 8);
		if ((m & (m >> 16)) >= 1) { 
			return true;
		}
		
		// Vert
		m = pos & (pos >> 1);
		if ((m & (m >> 2)) >= 1) { 
			return true;
		}
		
		return false;
	}
	
	public long[] placePiece(long pos, long mas, int col) {
		
		long new_pos = pos & mas;
		long new_mas = mas | (mas + (1l << (col * 7))) ;
		
		return new long[] {new_pos, new_mas};
		
	}
    
    @Override
    public String getName() { return "TipTacos's Advanced MCTS"; }
    
    // NODE CLASS
	class Node {
		
		private Node parent;
		private Node[] children = new Node[b];
		private long position, mask;
		private int x, y;
		private int wins[] = new int[3], sims;
		private int player;
		private int terminal = -1;
		
		public Node(Node parent, long pos, long mask, int player, int x, int y) {
			
			this(pos, mask, player, x, y);
			this.parent = parent;
		
		}
		
		public Node(long pos, long mask, int player, int x, int y) {
			
			this.position = pos;
			this.mask = mask;
			this.player = player;
			this.x = x;
			this.y = y;
			
		}
		
		public Node(int[][] board, int player, int x, int y) {
			
			long result[] = getBitmaps(board, player);
			this.position = result[0];
			this.mask = result[1];
			this.player = player;
			this.x = x;
			this.y = y;
			
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
		private ArrayList<Integer> getUnexploredChildren() {
			
			ArrayList<Integer> unexplored = new ArrayList<Integer>();
						
			for (int ii = 0 ; ii < b; ii++) {
				if (children[ii] != null) {
					if (children[ii].sims == 0) {
						unexplored.add(ii);
					}
				}
			}
			
			return unexplored;
			
		}
		
		// Populate children
		public void populateChildren() {
			
			ArrayList<Integer> validMoves = getValidMoves(mask);
			
			for (Integer xMove : validMoves) {
				int yMove = getStackSize(mask, xMove);
				children[xMove] = new Node(this, position, mask, 3-player, xMove, yMove);
				
				children[xMove].position ^= children[xMove].mask;
				children[xMove].mask |= (children[xMove].mask + (1l << (xMove * 7)));
								
				boolean full = isFull(children[xMove].mask);
				boolean win = isWin(children[xMove].position);
				
				children[xMove].terminal = full ? 0 : -1;
				children[xMove].terminal = win ? 3-player : children[xMove].terminal;
			}
			
		}
		
		// Return a random child that has been unexpored
		public Node getRandomChild() {
			
			ArrayList<Integer> unexplored = getUnexploredChildren();
			int index = rand.nextInt(unexplored.size());
			
			return children[(int)unexplored.get(index)];
			
		}
		
		public Node[] getChildren() {
			
			return children;
			
		}
		
		public long getMask() {
			return mask;
		}
		
		public long getPosition() {
			return position;
		}
		
		public long getPlayerPosition(int playerId) {
			if (playerId == player) {
				return position;
			} else {
				return position ^ mask;
			}
		}

	}
}
