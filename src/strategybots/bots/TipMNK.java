package strategybots.bots;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.Random;

import strategybots.games.ConnectFour;
import strategybots.games.TicTacToe;
import strategybots.games.base.Game.Player;

public class TipMNK implements Player<TicTacToe> {
    
    private TicTacToe game;
    private long time = 2000;
    private int turn = 1;
    
    private static int b = 7; // Branching
    private static float learningRate = 1.41f;  // Learning rate
    private static int bottomIterations = Runtime.getRuntime().availableProcessors();
    private static int globalSims = 0;
    private static Random rand = new Random();
    
    private Node root = null;
    
    public TipMNK() {}
    
    public TipMNK(long time) { this.time = time; }
        
    @Override
    public void init(TicTacToe game, int playerId) {
        this.game = game;
        b = game.getWidth() * game.getHeight();
    }

    @Override
    public void takeTurn(TicTacToe game, int playerId) {
        
        long start = System.currentTimeMillis();
        
        Vec2 move = bestMove(getBoard(), playerId);
        
        root = root.getChildFromMove(move);
        
        game.placeStone(move.x, move.y);
        
        System.out.println("=======================");
        System.out.println("TipMCTS Statistics:");
        System.out.println("Player:      " + playerId
                + " ("+(playerId==1?"Yellow":"Red")+")");
        System.out.println("Turn:        " + turn++);
        //System.out.println("Expectation: " + move[0]);
        System.out.println("Move:        [" + move.x + ", " + move.y + "]");
        //System.out.println("Depth:       " + move[2]);
        System.out.println("Global Sims: " + globalSims);
        System.out.println("Time:        "
                + (System.currentTimeMillis() - start) + "ms");
    }
    
    private Vec2 bestMove(int[][] board, int playerId) {
        
        long start = System.currentTimeMillis();
        
        globalSims = 0;
        
        if (root == null) {
        	root = new Node(board, 3-playerId, 0, 0);
        } else {
        	System.out.println(root);
        	rebaseTree(root, board);
        	globalSims = root.sims;
        	System.out.println(root);
        }
        
    	//root = new Node(board, 3-playerId, 0, 0);

        
       
        root.populateChildren();
        
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
    
    private void rebaseTree(Node root, int[][] newBoard) {
    	
    	for (Node child : root.children) {
    		
    		boolean equal = true;
    		
    		if (child != null) {
	    		
	    		int[][] oldBoard = child.getBoard();
	    		
	    		for (int xx = 0; xx < game.getWidth(); xx++) {
	    			
	    			for (int yy = 0 ; yy < game.getHeight(); yy++) {
	    				
	    				if (oldBoard[xx][yy] != newBoard[xx][yy]) {
	    					
	    					equal = false;
	    					break;
	    					
	    				}
	    				
	    				if (!equal) break;
	    			}
	    			
	    		}
	    		
	    		if (equal) {
	    			this.root = child;
	    			break;
	    		}
	    		
    		}
    		
    	}
    	
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
    
	private Vec2 getRandomMove(int[][] board) {
		
		ArrayList<Vec2> moves = getValidMoves(board);
		
		int index = rand.nextInt(moves.size());
		return moves.get(index);
		
	}
	
	private ArrayList<Vec2> getValidMoves(int[][] board) {
		
		ArrayList<Vec2> moves = new ArrayList<Vec2>();
		
		for (int xx = 0 ; xx < game.getWidth(); xx++) {
			for (int yy = 0; yy < game.getHeight(); yy++) {
				if (board[xx][yy] == 0) {
					moves.add(new Vec2(xx, yy));
				}
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
    
    // Returns 0 for draw, 1 for win 1, and b+1 for win 2
    private Integer moveResult(Node tempNode) {
    	Integer result = 0;
    	
    	int playerMove = tempNode.player;
    	Vec2 move = new Vec2();
    	// Check if the opposition has just placed a winning piece and the board is not Full
    	boolean isTerminal = isWin(tempNode.getBoard(), playerMove, tempNode.move.x, tempNode.move.y);
    	boolean isFull = isFull(tempNode.getBoard());
    	
    	// Continue until someone wins
    	while (!isTerminal && !isFull) {
    		
    		// Starting at the base child node, select a move and update the playerID and last position as well as board
    		// Get column move, randomly
    		move = getRandomMove(tempNode.getBoard());
    		// do move and swap PlayerID
    		playerMove = 3 - tempNode.player;
    		playMove(tempNode.getBoard(), playerMove, move.x, move.y);
    		tempNode.move.x = move.x;
    		tempNode.move.y = move.y;
    		tempNode.player = playerMove;

    		// Update the terminal condition
    		isTerminal = isWin(tempNode.getBoard(), playerMove, tempNode.move.x, tempNode.move.y);
        	isFull = isFull(tempNode.getBoard());
    	}
    	
    	// Check if the last valid move in the rollout was ours, and a win
    	if (isWin(tempNode.getBoard(), 1, tempNode.move.x, tempNode.move.y)) result += 1;
    	if (isWin(tempNode.getBoard(), 2, tempNode.move.x, tempNode.move.y)) result += (b+1);
    	
    	return result;
    }
    
    public int[] rollout(Node child) {
    	
    	// TODO add multiple rollouts
    	ArrayList<Vec2> moves = getValidMoves(child.getBoard());
    	
    	int[] results = new int[] {0, 0, 0, 0};
    	
    	results[3] = moves.size();
    	
    	// new code
    	ArrayList<Node> childTemps = new ArrayList<Node>();
    	
    	for (int ii = 0 ; ii < Math.min(moves.size(), bottomIterations); ii++) {
    		
    		Node tempChild = new Node(child.getBoard(), child.player, child.move.x, child.move.y);
    		Vec2 move = moves.remove(rand.nextInt(moves.size()));
    		
    		// do move and swap PlayerID
    		int playerMove = 3 - tempChild.player;
    		tempChild.board = playMove(tempChild.board, playerMove, move.x, move.y);
    		tempChild.move.x = move.x;
    		tempChild.move.y = move.y;
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
    
    @Override
    public String getName() { return "TipTacos's MCTS"; }
    
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
		private int terminal = -1;
		
		public Node(Node parent, int[][] board, int player, int x, int y) {
			
			this(board, player, x, y);
			this.parent = parent;
		
		}
		
		public Node(int[][] board, int player, int x, int y) {
			
			this(board, player, new Vec2(x, y));
			
		}
		
		public Node(int[][] board, int player, Vec2 move) {
			
			this.board = deepCopy(board);
			this.player = player;
			this.move = move;
			
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
			
			ArrayList<Vec2> validMoves = getValidMoves(board);
			
			int[][] prios = generatePriorities(board);
			
			for (Vec2 move : validMoves) {
				Node newChild = new Node(this, board, 3-player, move.x, move.y);
				newChild.move.prio = prios[move.x][move.y];
				playMove(newChild.getBoard(), 3-player, move.x, move.y);
				
				boolean full = isFull(newChild.getBoard());
				boolean win = isWin(newChild.getBoard(), 3-player, move.x, move.y);
				
				newChild.terminal = full ? 0 : -1;
				newChild.terminal = win ? 3-player : newChild.terminal;
				
				children.add(newChild);
			}
			
			children.sort(movePriorityComparator);
			
		}
		
		private int[][] generatePriorities(int[][] board) {
			
			int lims = 2;
			int[][] prios = new int[board.length][board[0].length];
			
			for (int x = 0 ; x < game.getWidth(); x++) {
				for (int y = 0 ; y < game.getHeight(); y++) {
					
					if (board[x][y] != 0) {
					
						for (int xx = x - lims; xx <= x + lims; xx++) {
							for (int yy = y - lims ; yy <= y + lims; yy++ ) {
								
								if(xx >= 0 && xx < game.getWidth() && yy >= 0 && yy < game.getHeight()) {
									
									prios[xx][yy]++;
									
								}
							}
						}
					}
				}
			}
			return prios;
			
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
