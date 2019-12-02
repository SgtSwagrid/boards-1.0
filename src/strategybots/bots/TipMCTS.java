package strategybots.bots;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

import strategybots.games.ConnectFour;
import strategybots.games.base.Game.Player;

public class TipMCTS implements Player<ConnectFour> {
    
    private ConnectFour game;
    private long time = 2000;
    private int playerId;
    private int turn = 1;
    
    private static int b = 7; // Branching
    private static int dep = 42;
    private static float learningRate = 1.41f;  // Learning rate
    private static int globalSims = 0;
    private static Random rand = new Random();
    
    public TipMCTS() {}
    
    public TipMCTS(long time) { this.time = time; }
        
    @Override
    public void init(ConnectFour game, int playerId) {
        this.game = game;
        this.playerId = playerId;
        b = game.getWidth();
    }

    @Override
    public void takeTurn(ConnectFour game, int playerId) {
        
        long start = System.currentTimeMillis();
        
        int move = bestMove(getBoard(), playerId);
        game.placeStone(move);
        
        System.out.println("=======================");
        System.out.println("TipMCTS Statistics:");
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
        
        int thresholdIters = 10000;
        int lastThreshold = 0;
        float threshold = 0.995f;
        
        Node root = new Node(board, 3-playerId, 0, 0);
        root.populateChildren();
        
        while (System.currentTimeMillis()-start < time) { 
        	// Do one iteration
        	MCTS_rec(root, 0);
        	
        	/*
        	if (globalSims - lastThreshold > thresholdIters ) {
        		lastThreshold = globalSims;
        		
        		if (bestChild(root)[1] >= threshold) {
        			System.out.println("TipMCTS Threshold of 99.5% confidence reached");
        			break;
        		} else {
        			System.out.println("Threshold too low, trying again after 10000");
        		}
        	}*/
        }

        for (Node child : root.children) {
        	
        	if (child == null) {
        		System.out.println("Root's Child is null");
        	} else {
        		System.out.println("Root's Child has [" + child.wins[1] + " / " + child.wins[2] + "] of " + child.sims);
        	}
        	
        }
        
        return (int)(bestChild(root)[0]);
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
    			
    			//System.out.println("Checking child " + ii + " " + (int)myWins + " / " + (int)mySims + " = " + finalScore);
    			if (finalScore > maxChildValue) {
    				maxChildId = ii;
    				maxChildValue = finalScore;
    			}
    		}
    	}
    	
    	return new float[] {maxChildId, maxChildValue};
    }
    
    private boolean isWin(int[][] board, int playerId, int x, int y) {
        
        int[][] dirs = new int[][] {{1, 0}, {1, 1}, {0, 1}, {-1, 1}};
        int[] signs = new int[] {-1, 1};
        
        if (board[x][y] != playerId) return false;
        
        for(int[] dir : dirs) {
        	
            int streak = 1;
            
            for(int sign : signs) {
                
                for(int i = 1; i < game.getTarget(); i++) {
                    
                    int xx = x+i*dir[0]*sign;
                    int yy = y+i*dir[1]*sign;
                    
                    if(xx < 0 || xx >= game.getWidth()) break;
                    if(yy < 0 || yy >= game.getHeight()) break;
                    
                    if(board[xx][yy] != playerId) break;
                    
                    streak++;
                }
            }
            if(streak >= game.getTarget()) return true;
        }
        return false;
    }
    
    private int getStackSize(int[][] board, int x) {
        
        for(int y = 0; y < game.getHeight(); y++) {
            if(board[x][y] == 0) return y;
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
     
    private boolean isFull(int[][] board) {
    	
    	return getValidMoves(board).size() == 0;
    	
    }
    
    private int[][] playMove(int[][] board, int player, int x, int y) {
    	
    	board[x][y] = player;
    	return board;
    	
    }
    
	private int getRandomMove(int[][] board) {
		
		ArrayList<Integer> moves = getValidMoves(board);
		
		int index = rand.nextInt(moves.size());
		return moves.get(index);
		
	}
	
	private ArrayList<Integer> getValidMoves(int[][] board) {
		
		ArrayList<Integer> moves = new ArrayList<Integer>();
		
		for (int ii = 0 ; ii < b; ii++) {
			if (board[ii][game.getHeight() - 1] == 0) {
				moves.add(ii);
			}
		}
		
		return moves;
		
	}
    
    // begin MCTS
    public int[] MCTS_rec(Node subroot, int depth) {
    	
    	int[] newWins = new int[] {0, 0, 0, 1}; 
    	
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
    	boolean isTerminal = isWin(tempNode.getBoard(), playerMove, tempNode.x, tempNode.y);
    	boolean isFull = isFull(tempNode.getBoard());
    	
    	// Continue until someone wins
    	while (!isTerminal && !isFull) {
    		
    		// Starting at the base child node, select a move and update the playerID and last position as well as board
    		// Get column move, randomly
    		xMove = getRandomMove(tempNode.getBoard());
    		// get Y of col
    		yMove = getStackSize(tempNode.getBoard(), xMove);
    		// do move and swap PlayerID
    		playerMove = 3 - tempNode.player;
    		playMove(tempNode.getBoard(), playerMove, xMove, yMove);
    		tempNode.x = xMove;
    		tempNode.y = yMove;
    		tempNode.player = playerMove;

    		// Update the terminal condition
    		isTerminal = isWin(tempNode.getBoard(), playerMove, tempNode.x, tempNode.y);
        	isFull = isFull(tempNode.getBoard());
    	}
    	
    	// Check if the last valid move in the rollout was ours, and a win
    	if (isWin(tempNode.getBoard(), 1, tempNode.x, tempNode.y)) result += 1;
    	if (isWin(tempNode.getBoard(), 2, tempNode.x, tempNode.y)) result += (b+1);
    	
    	return result;
    }
    
    public int[] rollout(Node child) {
    	
    	// TODO add multiple rollouts
    	ArrayList<Integer> moves = getValidMoves(child.getBoard());
    	
    	int[] results = new int[] {0, 0, 0, 0};
    	
    	results[3] = moves.size();
    	
    	// new code
    	ArrayList<Node> childTemps = new ArrayList<Node>();
    	for (Integer move : moves) {
    		Node tempChild = new Node(child.getBoard(), child.player, child.x, child.y);
    		
	    	int xMove = (int)move;
    		int yMove = getStackSize(tempChild.getBoard(), xMove);
    		// do move and swap PlayerID
    		int playerMove = 3 - tempChild.player;
    		tempChild.board = playMove(tempChild.board, playerMove, xMove, yMove);
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
    
    @Override
    public String getName() { return "TipTacos's MCTS"; }
    
    // NODE CLASS
	class Node {
		
		private Node parent;
		private Node[] children = new Node[b];
		private int[][] board;
		private int x, y;
		private int wins[] = new int[3], sims;
		private int player;
		private int terminal = -1;
		
		public Node(Node parent, int[][] board, int player, int x, int y) {
			
			this(board, player, x, y);
			this.parent = parent;
		
		}
		
		public Node(int[][] board, int player, int x, int y) {
			
			this.board = deepCopy(board);
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
			
			ArrayList<Integer> validMoves = getValidMoves(board);
			
			for (Integer xMove : validMoves) {
				int yMove = getStackSize(board, xMove);
				children[xMove] = new Node(this, board, 3-player, xMove, yMove);
				
				playMove(children[xMove].getBoard(), 3-player, xMove, yMove);
				
				boolean full = isFull(children[xMove].getBoard());
				boolean win = isWin(children[xMove].getBoard(), 3-player, xMove, yMove);
				
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
	}
}
