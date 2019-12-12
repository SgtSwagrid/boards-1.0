package strategybots.bots;

/* Author: Adrian Shedley with help from Alec Dorrington
 * Date: 8 Dec 2019
 * 
 * A MCTS based Othello bot. Currently still in the debug and dev phase, however still plays well against a moderate human.
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import strategybots.games.DotsAndBoxes;
import strategybots.games.DotsAndBoxes.Side;
import strategybots.games.Reversi;
import strategybots.games.base.Game.Player;

public class TipDots implements Player<DotsAndBoxes>{

	private long time = 2000l;
	private Random rand;
	private int globalSims = 0;
	private static float learningRate = 1.41f;
	private static int branch = 5*5;
	
    private Node root = null;
    
    private int[][] captures;
	
	public TipDots() {
		rand = new Random();
		System.out.println("Tip's Dots and Boxes Bot Loaded");
	}
	
	public TipDots(long millis) {
		this();
		this.time = millis;
	}
	
	@Override 
	public void init(DotsAndBoxes game, int playerId) {
		captures = new int[game.getWidth()][game.getHeight()];
	}
	
	@Override
	public void takeTurn(DotsAndBoxes game, int playerId) {

		long start = System.currentTimeMillis();
		int[][] board = getBoard(game);
        
        globalSims = 0;
        Node move = UCTSearch(board, playerId); //bestMove(board, playerId);
        
        for (Node child : root.children) {
        	if (child != null) {
        		System.out.println("Root's Child has [" + child.wins[1] + " / " + child.wins[2] + "] of " + child.sims + " @(" + child.move.x + "," + child.move.y +"); pieces[" + child.pieces[0] + ", " + child.pieces[1] + "] Player:" + child.player + " P:" + child.wins[playerId]/(float)child.sims);
        	}
        }
	    
        Side side = null;
        
        if (move.move.orien == 1) side = Side.TOP;
        if (move.move.orien == 2) side = Side.RIGHT;
        if (move.move.orien == 4) side = Side.BOTTOM;
        if (move.move.orien == 8) side = Side.LEFT;
        
        game.drawLine(side, move.move.x, move.move.y);
        
        helperBoard(root.board);
        printTree(root, 3, 2, 0);
        root = move;
        capture(root.getBoard(), playerId);
        
		printStats(playerId, move.move, start, move);
		
	}
	
	private int capture(int[][] board, int playerId) {
		int newCaptures = 0;
		
		int width = board.length;
	    int height = board[0].length;
				        
	    for (int y = height - 1 ; y >= 0; y--) {
	    	for (int x = 0 ; x < width; x++) {
	    		if (captures[x][y] == 0 && board[x][y] == 15) {
	    			newCaptures++;
	    			captures[x][y] = playerId;
	    		}
	    	}
	    }
	    
		return newCaptures;
	}
	
	private void helperBoard(int[][] board) {
		
		int width = board.length;
	    int height = board[0].length;
				        
	    for (int y = height - 1 ; y >= 0; y--) {
	    	for (int x = 0 ; x < width; x++) {
	    		System.out.print(board[x][y] + ", ");
	    	}
	    	System.out.println("; ");
	    }
	}
	
	//Done
	private Node UCTSearch(int[][] board, int playerId) {
		long start = System.currentTimeMillis();
		int capsBefore = 0;
		
		root = null; //rebase(root, board);
		if (root == null) {
			root = new Node(board, playerId, 0, 0, 0);
			System.out.println("root rebase FAILED");
		} else {
			System.out.println("Root rebased " + root);
		}
		
		if (root.getChildren().size() == 0) {
			root.populateChildren();
		}

		// do enemy captures
		int newCaps = capture(root.getBoard(), 3-playerId);
		System.out.println("There were " + newCaps + " new captures for enemy");
		if (root.pieces[playerId-1] == 0) {
			int myCaps = countCaptures(playerId);
			root.updatePieces(playerId, myCaps);
			System.out.println("updating my caps by " + myCaps);
		}
		if (root.pieces[3-playerId - 1] == 0) {
			int theirCaps = countCaptures(3-playerId);
			root.updatePieces(3-playerId, theirCaps);
			System.out.println("updating their caps by " + theirCaps);
		}
		
		// use up all the time
		while (System.currentTimeMillis() - start < time) {

			Node leaf = treePolicy(root);
			
			int[] result = defaultPolicyMulti(leaf);
			leaf.visited = true;
			
			backup(leaf, result);
		}
		
		return bestChild(root);
	}
	
	//Done
	private Node treePolicy(Node nodeIn) {
		
		Node node = nodeIn;
		
		if (node != null) {
			while (!isTerminal(node)) {
							
				if (node.hasUnexploredChildren()) {
					
					Node temp = expand(node);
					if (temp == null) System.out.println("we got him boys");
					return temp;
					
				} else {

					Node temp = bestChildUTC(node);
					if (temp == null) {
						System.out.println("Still some NULL leaf's :hmmm:");
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
		
		if (!isTerminal(child)) child.populateChildren();
		
		return child;
		
	}
	
	//Done
	private Node bestChildUTC(Node parent) {
		
		Node bestChild = null;
		double bestChildValue = -1.0f;
				
		for (Node child : parent.children) {
			
			int parentSims = (child.parent == null ? globalSims : parent.sims) ;
			double value = (child.wins[parent.player] / (double)child.sims) + learningRate * Math.sqrt(Math.log(parentSims) / (double)child.sims);
			
			if (value > bestChildValue) {
				bestChildValue = value;
				bestChild = child;
			}
		}
	
		return bestChild;
	}
	
	private int[] defaultPolicyMulti(Node leafIn) {
		int[] reward = new int[4];
		
		ArrayList<Move> moves = getMoves(leafIn.getBoard());
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
	private int defaultPolicy(Node leafIn, Move move) {
		
		Node leaf = new Node(leafIn.getBoard(), leafIn.player, leafIn.move);
		
		int reward = 0;

		int terminal = getMargin(leaf) ;
		
		while (!isTerminal(leaf)) {
			
			int caps = placeLine(leaf.getBoard(), move);
			
			if (caps > 0) {
				leaf.updatePieces(leaf.player, caps);
			} else {
				leaf.player = 3 - leaf.player;
			}
			
	    	terminal = getMargin(leaf);
	    	
	    	if (!isTerminal(leaf)) {
	    		ArrayList<Move> moves = getMoves(leaf.getBoard());
	    		if (moves.size() > 0) move = moves.get(rand.nextInt(moves.size())); else break;
	    	}
		}
		
    	if (terminal > 0) reward += 1;
    	if (terminal < 0) reward += (branch+1);

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
    	
    	for (Node child : root.getChildren()) {
    		
    		float myWins = child.wins[root.player] + 0.0001f;
			float mySims = child.sims + 0.0001f;
			float finalScore = (float)myWins / (float)mySims;
			
			if (finalScore > maxChildValue) {
				maxChild = child;
				maxChildValue = finalScore;
			}
    	}
    	
    	return maxChild;
    }
    
    private int placeLine(int[][] board, Move move) {
    	
    	int width = board.length, height = board[0].length;
    	int captured = 0;
    	Move move2 = new Move(move.x, move.y, move.orien, move.prio);
    	
    	// right
    	if (move.orien == 2) {
    		if (move.x + 1 < height) { move2.x++; move2.orien = 8; } else move2 = null;
    	}
    	
    	// top
    	if (move.orien == 1) {
    		if (move.y + 1 < width) { move2.y++; move2.orien = 4; } else move2 = null;
    	}
    	
    	// left
    	if (move.orien == 8) {
    		if (move.x - 1 >= 0) { move2.x--; move2.orien = 2; } else move2 = null;
    	}
    	
    	// bottom
    	if (move.orien == 4) {
    		if (move.y - 1 >= 0) { move2.y--; move2.orien = 1; }else move2 = null;
    	}
    	
    	//System.out.println("Before " + board[move.x][move.y]+ " + " + move.orien );
    	board[move.x][move.y] += move.orien; 
    	//System.out.println("Board after " + board[move.x][move.y]+ " on (" + move.x + ", " + move.y + ")");
    	if (board[move.x][move.y] == 15) {
    		captured += 1;
    	}
    	
    	if (move2 != null) {
        	board[move2.x][move2.y] += move2.orien; 
        	if (board[move2.x][move2.y] == 15) {
        		captured += 1;
        	}    	
        }
    	
    	return captured;
    }
    
    private Node rebase(Node root, int[][] board) {
        int width = board.length,  height = board[0].length;
        Node newRoot = null;
        boolean alreadyEqual = true;
        
        if (root == null || root.getChildren().size() == 0) return newRoot;
        
    	for (int x = 0 ; x < width; x++) {
			for (int y = 0 ; y < height; y++) {
				if (root.getBoard()[x][y] != board[x][y]) {
					alreadyEqual = false;
					break;
				}
			}
			if (!alreadyEqual) break;
    	}
        
        if (alreadyEqual) return root;
        
        for (Node child : root.getChildren()) {
	    	boolean equal = true;
	    	boolean partial = true;
        	
        	for (int x = 0 ; x < width; x++) {
				for (int y = 0 ; y < height; y++) {
					if (child.getBoard()[x][y] > board[x][y]) {
						equal = false;
						partial = false;
					}
					if (child.getBoard()[x][y] < board[x][y]) {
						equal = false;
					}
					
					if (equal == false && partial == false) break;
				}
				if (equal == false && partial == false) break;
	    	}
        	
        	if (equal) return child;
        	if (partial) {
        		System.out.println("attempting recursive rebase");
        		Node next = rebase(child, board);
        		System.out.println("Recusrisve return " + next);
        		return next;
        	}
        }
        
        return newRoot;
    }
    
    private ArrayList<Move> getMoves(int[][] board) {
    	
		ArrayList<Move> valid = new ArrayList<Move>();
        int width = board.length;
        int height = board[0].length;
		
        int[] sides = new int[] {8, 4, 2, 1};
        int sideLimit = 4;
        
    	for (int x = 0 ; x < width; x++) {
			for (int y = 0 ; y < height; y++) {
				int sideSum = 15-board[x][y];
				//if (x == width - 1 && y == height -1) sideLimit +=2 ;
				
				for (int ii = 0 ; ii < sideLimit; ii++) {

					if (sideSum - sides[ii] >= 0) {
						if ((x == width - 1 && sides[ii] == 2) ||
								(y == height - 1 && sides[ii] == 1) ||
								sides[ii] == 4 || sides[ii] == 8 ) valid.add(new Move(x, y, sides[ii], board[x][y]));
						sideSum -= sides[ii];
					}
				}
			}
    	}
    	
		return valid;
    }
 
	// Play move
	public Node generateChild(Node parent, Move move) {
		
		// Make a new move, with the same owner for now. IF we dont make a box, swap player
		Node newChild = new Node(parent, parent.getBoard(), parent.player, move);
		
		// Board will now have been deepCopied, we can edit in place.
		int caps = placeLine(newChild.getBoard(), move);
		if (caps > 0) {
			newChild.updatePieces(newChild.player, caps);
		} else {
			newChild.player = 3 - newChild.player;
		}		

		newChild.terminal = isTerminal(newChild) ? -1 : 0;
		
		return newChild;
	}
	
	private int getWinner2(Node leaf) {
		int terminal = -1;

		if (isTerminal(leaf)) {
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
	
	private int getMargin(Node leaf) {
		int terminal = 0;

		if (isTerminal(leaf)) {
			terminal = leaf.pieces[0] - leaf.pieces[1];
		}
				
		return terminal;
	}
	
	private boolean isTerminal(Node leaf) {
		
		int runSum = 0;
		int[][] board = leaf.getBoard();
		int width = board.length;
	    int height = board[0].length;
				        
	    if (leaf.pieces[0] >= 13 || leaf.pieces[1] >= 13) return true;
	    
	    for (int x = 0 ; x < width; x++) {
	    	for (int y = 0 ; y < height; y++) {
	    		runSum += board[x][y];	    	
	    	}
	    }

		return runSum == width*height*15;
	}
	
	private int countCaptures(int playerId) {
		int sum = 0;
		int width = captures.length;
	    int height = captures[0].length;
		
	    for (int x = 0 ; x < width; x++) {
	    	for (int y = 0 ; y < height; y++) {
	    		if (captures[x][y] == playerId) sum++;
	    	}
	    }
	    
	    return sum;
	}
	
	private int countCaptures(Node leaf) {
		int sum = 0;
		int[][] board = leaf.getBoard();
		int width = board.length;
	    int height = board[0].length;
		
	    for (int x = 0 ; x < width; x++) {
	    	for (int y = 0 ; y < height; y++) {
	    		if (board[x][y] == 15) sum++;
	    	}
	    }
	    
	    return sum;
	}
	
	private int[][] getBoard(DotsAndBoxes game) {

		int[][] board = new int[game.getWidth()][game.getHeight()];
		
		for (int yy = game.getHeight()-1 ; yy >= 0; yy--) {
			for (int xx = 0 ; xx < game.getWidth(); xx++) {
				board[xx][yy] += game.hasLine(Side.TOP, xx, yy) ? 1 : 0;
				board[xx][yy] += game.hasLine(Side.RIGHT, xx, yy) ? 2 : 0;
				board[xx][yy] += game.hasLine(Side.BOTTOM, xx, yy) ? 4 : 0;
				board[xx][yy] += game.hasLine(Side.LEFT, xx, yy) ? 8 : 0;
				System.out.print(board[xx][yy] + ", ");
			}
			System.out.println("; ");
		}
		
		return board;
		
	}
	
    @Override
    public String getName() { return "TipTacos's Dots and Boxes MCTS"; }
	
	private void printStats(int playerId, Move move, long start, Node best) {
        System.out.println("=======================");
        System.out.println("TipMCTS Statistics:");
        System.out.println("Player:      " + playerId
                + " ("+(playerId==1?"Yellow":"Red")+")");
        //System.out.println("Turn:        " + turn++);
        //System.out.println("Expectation: " + move[0]);
        System.out.println("Move:        [" + move.x + ", " + move.y + "] side " + move.orien); 
        System.out.println("Win Probab.  " + Math.round((best.wins[playerId] / (double)best.sims) * 1000.0) / 10.0 + "%"); 
        //System.out.println("Depth:       " + move[2]);
        System.out.println("Global Sims: " + globalSims);
        System.out.println("Time:        "
                + (System.currentTimeMillis() - start) + "ms");
	}
	
	private void printTree(Node root, int breadth, int depthMax, int depth) {
		
		String spacer = "";
		
		for (int i = 0 ; i <= depth; i++) {
			spacer += "  ";
		}
		
		System.out.println(spacer + root.toString());
		
		if (depthMax - depth > 0) {
			int iters = 0;
			for (Node child : root.children) {
				printTree(child, breadth, depthMax, depth + 1);
				iters++;
				
				if (iters > breadth) break;
			}
		}
		
	}
	
    class Move {
    	
    	int x = 0, y = 0, orien = 0;
    	int prio = 0;
    	
    	public Move() {}
    	
    	public Move (int x, int y, int orien) {
    		this.x = x;
    		this.y = y;
    		this.orien = orien;
    	}
    	
    	public Move (int x, int y, int orien, int prio) {
    		this(x, y, orien);
    		this.prio = prio;
    	}
    }
    
    // NODE CLASS
	class Node {
		
		private Node parent;
		private ArrayList<Node> children = new ArrayList<Node>();
		private boolean visited = false;
		private int[][] board;
		private Move move;
		private int wins[] = new int[3], sims;
		private int player;
		private int[] pieces = new int[] {0, 0};
		private int terminal = -1;
		
		public Node(Node parent, int[][] board, int player, int x, int y, int orien) {
			
			this(board, player, x, y, orien);
			this.pieces[0] = parent.pieces[0];
			this.pieces[1] = parent.pieces[1];
		}
		
		public Node(Node parent, int[][] board, int player, Move move) {
			
			this(board, player, move);
			this.parent = parent;
			this.pieces[0] = parent.pieces[0];
			this.pieces[1] = parent.pieces[1];
		}
		
		public Node(int[][] board, int player, int x, int y, int orien) {
			
			this(board, player, new Move(x, y, orien));
			
		}
		
		public Node(int[][] board, int player, Move move) {
			
			this.board = deepCopy(board);
			this.player = player;
			this.move = move;
			this.pieces = new int[] {0, 0};
			
		}
		
		// Return the score including the learning rate parameter
		public float score() {
			
			int parentSims = 0;
			if (parent == null) parentSims = globalSims; else parentSims = parent.sims;
			
			return (float) (((float)(wins[player]) / (float)sims) + learningRate * Math.sqrt(Math.log(parentSims) / (float)sims));
		}
		
		// If there is at least one unexplored child.
		public boolean hasUnexploredChildren() {
			
			return getUnexploredChildren().size() > 0;
		}
		
		public void updateWins(int[] newWins) {
			
			for (int ii = 0; ii < 3; ii++) {
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
			
			ArrayList<Move> moves = getMoves(board);
			
			if (moves.size() == 0) {
				System.out.println("Warning therminal child " + this);
			}
			
			for (Move move : moves) {
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
		
		public Node getChildFromMove(Move move) {
			
			for (Node child : children) {
				
				if (child.move.x == move.x && child.move.y == move.y && child.move.orien == move.orien) {
					System.out.println("One child rebased");
					return child;
				} else {
					System.out.println("Failed rebase child " + child.move.x + " " + child.move.y + " " + child.move.orien);
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
		
		public void updatePieces(int player, int change) {
			
			int index = player - 1;
			
			pieces[index] += change;
			
			for (Node child: children) {
				child.updatePieces(player, change);
			}
			
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
		
		public String toString() {
			
			String out = "";
			out += "Node (" + move.x + ", " + move.y + ") SIDE " + move.orien;
			out += ". Player " + player + " and pieces [" + pieces[0] + ", " + pieces[1] + "]";
			
			return out;
		}
		
	}
	
}
