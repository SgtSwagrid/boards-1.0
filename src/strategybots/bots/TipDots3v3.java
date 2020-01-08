package strategybots.bots;

/* Author: Adrian Shedley with help from Alec Dorrington
 * Date: 5 Jan 2019
 * 
 * A Minimax + Alpha beta pruning bot to play Dots and Boxes. Currently implemented with 
 * the help of domain specific knowledge and zobrist hashing
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import strategybots.games.DotsAndBoxes;
import strategybots.games.DotsAndBoxes.Side;
import strategybots.games.base.Game.Player;

public class TipDots3v3 implements Player<DotsAndBoxes>{

	private long time = 2000l;
	private int maxDepth = 7;
	private int turn = 0;
	private int beamFactor = 60;
		
	private int width, height;
	private int topMoves, topDepth;
	
	// Zobrist Variables
	Zobrist zobrist;
	
	public TipDots3v3() {
		System.out.println("Tip's Dots and Boxes Bot 3 v3 Loaded");
	}
	
	public TipDots3v3(long millis) {
		this();
		this.time = millis;
	}
	
	public TipDots3v3(long millis, int beam) {
		this(millis);
		this.beamFactor = beam;
	}
	
	@Override 
	public void init(DotsAndBoxes game, int playerId) {

		this.width = game.getWidth();
		this.height = game.getHeight();
		zobrist = new Zobrist(1000000);
	}
	
	@Override
	public void takeTurn(DotsAndBoxes game, int playerId) {
		getBestMove(game, playerId);
		System.out.println("Zobrist R=" + zobrist.getFillRatio());
	}
	
	/**
	 * Method to get the best move from the current board state and player
	 * @param game The game object
	 * @param playerId The playerId to maximize for
	 */
	private void getBestMove(DotsAndBoxes game, int playerId) {
		
		long start = System.currentTimeMillis();
		Board board = getGraph(game, playerId);
		int[] scores = getScores(game);
		
		List<Edge> bestEdges = null;
		int depth = 1, score = 0;
		maxDepth = board.getEdges().size() + 1;
		
		// Use iterative deepening to run negaMax until the board is searched or we run out of time.
        for(; depth <= maxDepth; depth++) {
        	
        	//System.out.println("running depth " + depth);
        	zobrist.resetTable();
        	topMoves = board.edges.size();
        	topDepth = depth;
        	Triple result = negamax(board.verts, board.edges, scores, playerId, depth, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        	score = result.score;
        	bestEdges = result.edges;
        	
        	if(System.currentTimeMillis()-start > time) break;
        }

        // Execute the best move and then display stats about this turn
		playMove(game, bestEdges);
		printStats(score, playerId, bestEdges, depth, start);
	}
	
	/**
	 * Take an edge and play an appropriate move according to the current game state
	 * @param game The game object	
	 * @param edge The edge that is to be played
	 */
	private void playMove(DotsAndBoxes game, List<Edge> bestEdges) {
		
		Side side = Side.TOP;
		Vertex v0 = null;
		
		for (Edge edge : bestEdges) {
		
			v0 = edge.getV0();
			side = edge.getSide();
			
			game.drawLine(side, v0.x, v0.y);
		}
	}
	
	/**
	 * Take in a game object and return a board representation as a graph of nodes with connected edges
	 * Each of the edges in the list represents a valid move.
	 * @param game The game object
	 * @param playerId The playerId that is this bot
	 * @return Board object
	 */
	private Board getGraph(DotsAndBoxes game, int playerId) {
		
		Board board = new Board();
		
		int width = game.getWidth(), height = game.getHeight();
		
		for (int x = 0 ; x < width ; x++) {
			for (int y = 0 ; y < height ; y++) {
				board.getVerts().add(new Vertex(x, y));
			}
		}
		
		for (int x = 0 ; x < width ; x++) {
			for (int y = 0 ; y < height ; y++) {
				// Check for right col
				if (x == width - 1 && !game.hasLine(Side.RIGHT, x, y)) 
					board.getEdges().add(makeSide(board.getVerts(), x, y, x, y, Side.RIGHT));
				// Check for Top row
				if (y == height - 1 && !game.hasLine(Side.TOP, x, y)) 
					board.getEdges().add(makeSide(board.getVerts(), x, y, x, y, Side.TOP)); 
				// check bottom, with special condition bottom row
				if (!game.hasLine(Side.BOTTOM, x, y))
					board.getEdges().add(makeSide(board.getVerts(), x, y, x, y == 0 ? 0 : (y-1), Side.BOTTOM));
				// Left check, with special condition for col 0
				if (!game.hasLine(Side.LEFT, x, y))
					board.getEdges().add(makeSide(board.getVerts(), x, y, x == 0 ? 0 : (x-1), y, Side.LEFT));
			}
		}
		
		board.edges.sort(prioritySort);
		return board;
	}
	
	/**
	 * Return a new Side object that links the two vertexes as specified by the two pairs of coordinates
	 * @param verts The complete set of vertexes in the graph.
	 * @param x0 
	 * @param y0
	 * @param x1
	 * @param y1
	 * @return A new edge object.
	 */
	private Edge makeSide(Set<Vertex> verts, int x0, int y0, int x1, int y1, Side side) {
		Vertex v0 = getVertex(verts, x0, y0), v1 = getVertex(verts, x1, y1);
		Edge edge = new Edge(v0, v1, side);
		v0.addEdge(edge); 
		v1.addEdge(edge);
		return edge;
	}
	
	/**
	 * Return a vertex with the matching x and y coordinates.
	 * @param verts The complete set of vertexes in the graph.
	 * @param x
	 * @param y
	 * @return Vertex matching the (x,y) or null if none is found.
	 */
	private Vertex getVertex(Set<Vertex> verts, int x, int y) {
		
		Vertex found = null;
		
		for (Vertex vv : verts) {
			if (vv.match(x, y)) {
				found = vv;
				break;
			}
		}
		
		return found;
	}
	
	/**
	 * Negamax implementation that has alpha-beta pruning, and the option to take multiple turns as the same player.
	 * This is a recursive function with a depth limit as specified in the parameter 
	 * @param verts The set of vertexes in graph.
	 * @param edges The list of edges in the current state.
	 * @param captures The current square captured.
	 * @param playerId The player to maximize for.
	 * @param depth Depth limit from here onward.
	 * @param alpha
	 * @param beta
	 * @return A set of values corresponding to Score, Best Move, and depth.
	 */
	private Triple negamax(Set<Vertex> verts, List<Edge> edges, int[] captures, int playerId, int depth, int alpha, int beta) {
		
		int score = 0, iters = 0;
		List<Edge> bestMove = null;
		
		long zobHash = zobrist.getHash(edges);
		// Lookup this position
		ZobristEntry zobExist = zobrist.get(zobHash);
		
		if (zobExist != null && zobExist.getKey() == zobHash && zobExist.depth >= depth) {
			if (zobExist.getAlpha() <= alpha) {
				//System.out.println("Alpha terminal");
				return new Triple(zobExist.getAlpha(), zobExist.getBestMove());
			} else if (zobExist.getBeta() >= beta) {
				//System.out.println("Beta terminal");
				return new Triple(zobExist.getBeta(), zobExist.getBestMove());
			}
		}
		
		if (edges.size() == 1) {
			
			List<Edge> container = new ArrayList<Edge>();
			container.add(edges.get(0));
			
			successor(container, captures, playerId);
			int finalScore = heuristic(captures, playerId);
			predecessor(container, captures, playerId);
			
			// Depth 0 (terminal) hashtable put
			zobrist.putElement(zobHash, container, depth, finalScore, finalScore);
			
			return new Triple(finalScore, container, depth);
		}
	
		// Negamax as requried
		edges.sort(prioritySort);
		List<List<Edge>> compMoves = generateMoves(verts, edges, beamFactor);
		
		for (List<Edge> move : compMoves ) {
			
			// apply the edge removal
			boolean hasCaptured = successor(move, captures, playerId);
						
			List<Edge> nextEdges = new ArrayList<Edge>();
			nextEdges.addAll(edges);
			for (Edge edge : move) {
				nextEdges.remove(edge);
			}
			
			// Get the game score or margin at this layer
			int heur = heuristic(captures, playerId);
			
			// Do negamax
			int nextPlayer = (hasCaptured) ? playerId : (3-playerId);
			
			int s = heur;
			if (depth > 0 && nextEdges.size() != 0) {
				s = (hasCaptured ? negamax(verts, nextEdges, captures, nextPlayer, depth-move.size(), alpha, beta).score : 
            		-negamax(verts, nextEdges, captures, nextPlayer, depth-move.size(), -beta, -alpha).score);
			}
			
			// Update scores or set the score to the first element when first run
            if(s > score || bestMove == null ) {
                score = s;
                bestMove = move;
                
                if (score > alpha) {
                	alpha = score;
                	
        			zobrist.putElement(zobHash, bestMove, depth-1, alpha, beta);
                }
            }
            
			// predecessor
			predecessor(move, captures, playerId);
			
			// Alpha Beta check
            if(alpha >= beta) {
            	break;
            }

			// Beam check
            iters++;
            if (iters >= beamFactor) break;
		}
		
		//zobrist.putElement(zobHash, bestMove, depth, alpha, beta);
		
		return new Triple(score, bestMove, depth);
	}
	
	/**
	 * Generate the compound moves from a given set of vertexes and edges, up to a limit to save time.
	 * @param verts
	 * @param edges
	 * @param limit
	 * @return
	 */
	private List<List<Edge>> generateMoves(Set<Vertex> verts, List<Edge> edges, int limit) {
		List<List<Edge>> output = new ArrayList<>();
		
		// Reset the visits
		for (Edge ee : edges) { ee.setVisited(false); }
		
		//System.out.println("Begin Generating Moves");
		for (Edge edge : edges) {
		
			// If this edge is a start of a chain
			if (edge.minDegree() == 1) {
				List<Edge> moves = new ArrayList<Edge>();
				
				Vertex nextNode = edge.minDegreeVertex();;
				Edge nextEdge = edge;
				
				do {
					nextNode = nextEdge.getOther(nextNode);
					
					nextEdge.setVisited(true);
					moves.add(nextEdge);
					
					if (nextNode.getDegree() == 2) {
						nextEdge = nextNode.getUnvisitedEdge();
						if (nextEdge == null) break;
					} else {
						break;
					}
					
				} while (true);
				
				output.add(moves);
				
				// Terminal conditions
				if (nextNode.getDegree() == 1 && moves.size() >= 3) {
					List<Edge> altMoves = new ArrayList<Edge>();
					altMoves.addAll(moves);
					altMoves.remove(altMoves.size() - 3);
					altMoves.remove(altMoves.size() - 1);
					output.add(altMoves);
				}
				
				if (nextNode.getDegree() != 1 && moves.size() >= 2) {
					List<Edge> altMoves = new ArrayList<Edge>();
					altMoves.addAll(moves);
					altMoves.remove(altMoves.size() - 2);
					output.add(altMoves);
				}
				
				if (output.size() > limit) break;
			}
		}
				
		// For each of the edges that were not compound moves, add them singularly
		for (Edge edge : edges) {
			if (!edge.isVisited()) {
				List<Edge> moves = new ArrayList<Edge>();
				moves.add(edge);
				edge.setVisited(true);
				output.add(moves);
				if (output.size() > limit) break;
			}
		}
	
		output.sort(prioritySortList);
		return output;
	}
	
	/**
	 * Successor function 
	 * @param edge A move to make and to update.
	 * @param captures An array for the two players' scores.
	 * @param playerId The player who is capturing. 
	 * @return
	 */
	private boolean successor(List<Edge> edges, int[] captures, int playerId) {
		
		Vertex v0, v1;
		boolean capture = false;
		
		for (Edge edge : edges) {
		
			v0 = edge.getV0();
			v1 = edge.getV1();
			capture = false;
			edge.setEnabled(false);
			
			v0.removeEdge(edge);
			if (v0.getDegree() == 0) {
				capture = true;
				captures[playerId-1]++;
				v0.setOwner(playerId);
			}
			
			if (v0 != v1) {
				v1.removeEdge(edge);
				if (v1.getDegree() == 0) {
					capture = true;
					captures[playerId-1]++;
					v1.setOwner(playerId);
				}
			}
		}
		
		return capture;
	}
	
	/**
	 * Predecessor function 
	 * @param edge A move to make and to update.
	 * @param captures An array for the two players' scores.
	 * @param playerId The player who is capturing. 
	 * @return
	 */
	private void predecessor(List<Edge> edges, int[] captures, int playerId) {
		
		Vertex v0, v1;
		Collections.reverse(edges);
		
		for ( Edge edge : edges) {
			
			v0 = edge.getV0();
			v1 = edge.getV1();
			edge.setEnabled(true);
			
			if (v0.getDegree() == 0) {
				v0.setOwner(0);
				captures[playerId-1]--;
			}
			v0.addEdge(edge);
			
			if (v0 != v1) {
				if (v1.getDegree() == 0) {
					v1.setOwner(0);
					captures[playerId-1]--;
				}
				v1.addEdge(edge);
			}
		}
		
		Collections.reverse(edges);
	}
	
	/**
	 * Heuristic function that calculates the win margin corresponding to the given player
	 * @param captures The array of scores for both players
	 * @param playerId The player to calculate the margin for.
	 * @return An integer margin value. A positive value indicates that the player specified is winning.
	 */
	private int heuristic(int[] captures, int playerId) {	
		return captures[playerId-1] - captures[(3-playerId) - 1];
	}
	
	
	/**
	 * Prints a neat summary after each completed bot move.
	 * @param score
	 * @param playerId
	 * @param moveX
	 * @param moveY
	 * @param moveSide
	 * @param depth
	 * @param start
	 */
    private void printStats(int score, int playerId, List<Edge> ee, int depth, long start) {
        
        System.out.println("=======================");
        System.out.println("Tiptaco's Dots Bot 3 v3 Statistics:");
        System.out.println("Player:      " + playerId + " ("+(playerId==1?"Blue":"Red")+")");
        System.out.println("Turn:        " + turn++);
        System.out.println("Expectation: " + score);
        System.out.println("Edge:    	(" + ee.get(0));
        System.out.println("Depth:       " + depth);
        System.out.println("Time:        "  + (System.currentTimeMillis() - start) + "ms");
    }

	private int[] getScores(DotsAndBoxes game) {
		return new int[] { game.getScore(1), game.getScore(2) };
	}

	public final static Comparator<Edge> prioritySort = new Comparator<Edge>() {         
		@Override         
		public int compare(Edge e1, Edge e2) {  
			int compare = 0;
			
			if (e1.isEnabled() != e2.isEnabled()) {
				compare = e1.isEnabled() ? -1 : 1;
			} else {
				int p1 = e1.getPriority(), p2 = e2.getPriority();
				compare = (p2 < p1 ? -1 : (p2 == p1 ? 0 : 1));
			}
			
			return compare;
		}     
	}; 
	
	public final static Comparator<List<Edge>> prioritySortList = new Comparator<List<Edge>>() {         
		@Override         
		public int compare(List<Edge> e1, List<Edge> e2) {  
			return prioritySort.compare(e1.get(0), e2.get(0));      
		}     
	}; 
	
	@Override
	public String getName() { return "TipTacos's Dots and Boxes 3 v3 Bot"; }
    
    class Board {
    	List<Edge> edges;
    	Set<Vertex> verts;
    	
    	public Board() {
    		edges = new ArrayList<Edge>();
    		verts = new HashSet<Vertex>();
    	}
    	
    	public void addEdge(Edge ee) { edges.add(ee); }
    	
    	public void addVertex(Vertex vv) { verts.add(vv); }
    	
    	public List<Edge> getEdges() { return edges; }
    	
    	public Set<Vertex> getVerts() { return verts; }
    }
    
    class Triple {
    	List<Edge> edges;
    	int score, depth;
    	
    	public Triple(int score, List<Edge> edges) {
    		this(score, edges, -1);
    	}
    	
    	public Triple(int score, List<Edge> container, int depth) {
    		this.score = score;
    		this.edges = container;
    		this.depth = depth;
    	}
    }
    
    
    class Vertex {
    	private int x, y, owner, pPrio;
    	Set<Edge> edges;
    	
    	public Vertex() {
    		edges = new HashSet<Edge>();
    	}
    	
    	public Vertex(int x, int y) {
    		this();
    		this.x = x; 
    		this.y = y;
    		this.owner = 0;
    		calculatePosPrio();
    	}
    	
    	private void calculatePosPrio() {
    		float sigX = (width - 1) / 2.0f, sigY = (height - 1) / 2.0f;
    		int pX = Math.round(-Math.abs(x-sigX) + sigX);
    		int pY = Math.round(-Math.abs(y-sigY) + sigY);
    		pPrio = pX + pY;
    	}
    	
    	public int getPosPrio() {
    		return pPrio;
    	}
    	
    	public void addEdge(Edge ee) {
    		edges.add(ee);
    	}
    	
    	public Set<Edge> getEdges() {
    		return edges;
    	}
    	
    	public void removeEdge(Edge ee) {
    		edges.remove(ee);
    	}
    	
    	public int getDegree() {
    		return edges.size();
    	}
    	
    	public boolean match(int x, int y) {
    		return (this.x == x && this.y == y);
    	}
    	
    	public void setOwner(int playerId) {
    		this.owner = playerId;
    	}
    	
    	public int getOwner() {
    		return owner;
    	}
    	
    	public Edge getUnvisitedEdge() {
    		
    		for (Edge ee : edges) {
    			if (!ee.isVisited()) {
    				return ee;
    			}
    		}
    		
    		return null;
    	}
    	
    	public String toString() {
    		return "Vert (" + x + ", " + y + ")"; 
    	}
    }
    
    
    class Edge {
    	Vertex v0, v1;
    	private boolean visited = false, enabled = true;
    	private DotsAndBoxes.Side side;
    	private int UID = -1;
    	
    	public Edge() {}
    	
    	public Edge(Vertex v0, Vertex v1, DotsAndBoxes.Side side) {
    		this.v0 = v0;
    		this.v1 = v1;
    		if (v0 == null || v1 == null) { System.out.println("Empty Node Warning"); }
    		this.side = side;
    		generateUID();
    	}
    	
    	public Vertex getOther(Vertex vIn) {
    		return vIn.equals(v0) ? v1 : v0;
    	}
    	
    	public String toString() {
    		return "Edge (" + v0 + " to " + v1 + ") p=" + getPriority() + " e=" + enabled;
    	}
    	
    	public int minDegree() {
    		return Math.min(v0.getDegree(), v1.getDegree());
    	}
    	
    	public Vertex minDegreeVertex() {
    		if (v0.getDegree() < v1.getDegree()) {
    			return v0;
    		} else {
    			return v1;
    		}
    	}
    	
    	public int getPriority() {
    		int d0 = v0.getDegree(), d1 = v1.getDegree();
    		int prio = 0;
    		
    		prio += (d0 == 1 ? 100 : 0) + (d1 == 1 ? 100 : 0);
    		prio += (d0 == 2 ? (d1 == 1 ? 90 : -90) : 0) + (d1 == 2 ? (d0 == 1 ? 90 : -90) : 0);
    		//prio += (d0 == 3 ? 2 : 0) + (d1 == 3 ? 2 : 0);
    		prio += v0.getPosPrio() + v1.getPosPrio();
    		
    		return prio;
    	}
    	
    	private void generateUID() {
    		if (side == Side.BOTTOM || side == Side.TOP) {
    			UID = (v0.x + v0.y * width) + (side == Side.BOTTOM ? 0 : width);
    		} else {
    			UID = (width * (height+1)) + (v0.y + v0.x * height) + (side == Side.LEFT ? 0 : height);
    		}
    	}
    	
    	public int getUID() { return UID; }
    	public Side getSide() { return side; }
    	
    	public Vertex getV0() { return v0; }
    	public Vertex getV1() { return v1; }
    	
    	public boolean isVisited() { return visited; }
    	public void setVisited(boolean visited) { this.visited = visited; }
    	
    	public boolean isEnabled() { return enabled; }
    	public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
    
    class Zobrist {
  	
    	private long zobrist[];
    	
    	private int tableSize, currentSize;
    	private ZobristEntry table[];
    	
    	public Zobrist(int tableSize) {
    		Random rand = new Random(System.currentTimeMillis());
    		
    		int maxEntries = width * (height + 1) + height * (width + 1);
    		zobrist = new long[maxEntries];
    		for (int ii = 0 ; ii < maxEntries; ii++) {
    			zobrist[ii] = rand.nextLong();
    		}
    		
    		this.tableSize = tableSize;
    		resetTable();
    	}
    	
    	public void resetTable() {
    		table = new ZobristEntry[tableSize];
    		currentSize = 0;
    	}
    	
    	public void putElement(long hash, List<Edge> bestMove, int depth, int alpha, int beta) {
    		putElement(new ZobristEntry(hash, bestMove, depth, alpha, beta));
    	}
    	
    	public void putElement(ZobristEntry zobE) {
    		int index = (int) Math.floorMod(zobE.getKey(), tableSize);
    		    		
    		if (table[index] == null) {
    			table[index] = zobE;
    			currentSize++;
    		} else {
    			// Replace deeper
    			if (zobE.depth > table[index].depth) {
    				table[index] = zobE;
    			}
    		}
    	}
    	
    	public ZobristEntry get(long hash) {
    		//System.out.println(hash + " MOD " + tableSize + " = " + (int) Math.floorMod(hash, tableSize));
    		return table[(int) Math.floorMod(hash, tableSize)];
    	}
    	
    	public long updateHash(long inputHash, List<Edge> edges) {
    		long hash = inputHash;
    		for (Edge edge : edges) {
    			hash = updateHash(hash, edge);
    		}
    		return hash;
    	}
    	
    	public long updateHash(long inputHash, Edge edge) {
    		return inputHash ^ zobrist[edge.getUID()];
    	}
    	
    	public float getFillRatio() {
    		return (float)((float)currentSize / tableSize);
    	}
    	
    	public long getHash(List<Edge> edges) {
    		
    		long newKey = 0l;
    		
    		for (Edge edge : edges) {
    			newKey ^= zobrist[edge.getUID()];
    		}
    		
    		return newKey;
    	}
    	
    }
    
    class ZobristEntry {
    	private long key;
    	private int depth;
		private int alpha, beta;
    	List<Edge> bestMove;
    	
    	public ZobristEntry(long key, List<Edge> bestMove, int depth, int alpha, int beta) {
    		this.key = key;
    		this.bestMove = bestMove;
    		this.depth = depth;
    		this.alpha = alpha;
    		this.beta = beta;
    	}
    	
    	public ZobristEntry(Zobrist zob, List<Edge> edges, List<Edge> bestMove, int depth, int alpha, int beta) {
    		this.key = zob.getHash(edges);
    		this.bestMove = bestMove;
    		this.depth = depth;
    		this.alpha = alpha;
    		this.beta = beta;
    	}
    	
    	public List<Edge> getBestMove() {
			return bestMove;
		}    	
    	
		public void setBestMove(List<Edge> bestMove) {
			this.bestMove = bestMove;
		}

		public long getKey() {
			return key;
		}

		public int getDepth() {
			return depth;
		}

		public int getAlpha() {
			return alpha;
		}
		
		public void setAlpha(int alpha) {
			this.alpha = alpha;
		}

		public int getBeta() {
			return beta;
		}
		
		public void setBeta(int beta) {
			this.beta = beta;
		}
    }
}
