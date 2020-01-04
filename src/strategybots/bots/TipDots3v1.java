package strategybots.bots;

/* Author: Adrian Shedley with help from Alec Dorrington
 * Date: 8 Dec 2019
 * 
 * A MCTS based Othello bot. Currently still in the debug and dev phase, however still plays well against a moderate human.
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import strategybots.bots.TipDots3v2.Edge;
import strategybots.bots.TipDots3v2.Vertex;
import strategybots.games.DotsAndBoxes;
import strategybots.games.DotsAndBoxes.Side;
import strategybots.games.base.Game.Player;

public class TipDots3v1 implements Player<DotsAndBoxes>{

	private long time = 2000l;
	private int maxDepth = 7;
	private int turn = 0;
	private int beamFactor = 10;
	
	public TipDots3v1() {
		System.out.println("Tip's Dots and Boxes Bot 3 v1 Loaded");
	}
	
	public TipDots3v1(long millis) {
		this();
		this.time = millis;
	}
	
	public TipDots3v1(long millis, int beam) {
		this(millis);
		this.beamFactor = beam;
	}
	
	@Override
	public void takeTurn(DotsAndBoxes game, int playerId) {

		getBestMove(game, playerId);
	}
	
	/**
	 * Mehtod to get the best move from the current board state and player
	 * @param game The game object
	 * @param playerId The playerId to maximize for
	 */
	private void getBestMove(DotsAndBoxes game, int playerId) {
		
		long start = System.currentTimeMillis();
		Board board = getGraph(game, playerId);
		int[] scores = getScores(game);

		Edge bestEdge = null;
		int depth = 1, score = 0;
		maxDepth = board.getEdges().size();
		
		// Use iterative deepening to run negaMax until the board is searched or we run out of time.
        for(; depth <= maxDepth; depth++) {
        	
        	Triple result = negamax(board.verts, board.edges, scores, playerId, depth, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        	score = result.score;
        	bestEdge = result.edge;
        	
        	if(System.currentTimeMillis()-start > time) break;
        }

        // Execute the best move and then display stats about this turn
		playMove(game, bestEdge);
		printStats(score, playerId, bestEdge, depth, start);
	}
	
	/**
	 * Take an edge and play an appropriate move according to the current game state
	 * @param game The game object	
	 * @param edge The edge that is to be played
	 */
	private void playMove(DotsAndBoxes game, Edge edge) {
		
		int width = game.getWidth(), height = game.getHeight();
		Vertex v0 = edge.getV0(), v1 = edge.getV1();
		Side side = Side.TOP;
		
		if (v0 != v1) {  // Nodes aren't the same, can infer direction
			if (v0.x == v1.x) {
				if (v0.y < v1.y)  side = Side.TOP;  else  side = Side.BOTTOM; 
			} else if (v0.y == v1.y) {
				if (v0.x < v1.x)  side = Side.RIGHT;  else  side = Side.LEFT;
			}
		} else { // Nodes are the same, so this is an edge piece
			
			if (v0.x == 0 && v0.y == 0) {
				if (game.hasLine(Side.BOTTOM, v0.x, v0.y))  side = Side.LEFT;  else  side = Side.BOTTOM;
			} else if (v0.x == width-1 && v0.y == 0) {
				if (game.hasLine(Side.BOTTOM, v0.x, v0.y)) side = Side.RIGHT;  else side = Side.BOTTOM;
			} else if (v0.x == width-1 && v0.y == height-1) { 
				if (game.hasLine(Side.TOP, v0.x, v0.y)) side = Side.RIGHT; else side = Side.TOP;
			} else if (v0.x == 0 && v0.y == height-1) {
				if (game.hasLine(Side.TOP, v0.x, v0.y)) side = Side.LEFT; else side = Side.TOP;
			} else {
				if (v0.x == 0)  side = Side.LEFT;
				if (v0.x == width-1)  side = Side.RIGHT;
				if (v0.y == 0)  side = Side.BOTTOM;
				if (v0.y == height-1)  side = Side.TOP;
			}
		}
		
		game.drawLine(side, v0.x, v0.y);
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
					board.getEdges().add(makeSide(board.getVerts(), x, y, x, y));
				// Check for Top row
				if (y == height - 1 && !game.hasLine(Side.TOP, x, y)) 
					board.getEdges().add(makeSide(board.getVerts(), x, y, x, y)); 
				// check bottom, with special condition bottom row
				if (!game.hasLine(Side.BOTTOM, x, y))
					board.getEdges().add(makeSide(board.getVerts(), x, y, x, y == 0 ? 0 : (y-1)));
				// Left check, with special condition for col 0
				if (!game.hasLine(Side.LEFT, x, y))
					board.getEdges().add(makeSide(board.getVerts(), x, y, x == 0 ? 0 : (x-1), y));
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
	private Edge makeSide(Set<Vertex> verts, int x0, int y0, int x1, int y1) {
		Vertex v0 = getVertex(verts, x0, y0), v1 = getVertex(verts, x1, y1);
		Edge edge = new Edge(v0, v1);
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
		
		int score = 0, iters = 0, index = 0;
		Edge bestEdge = null;
		
		if (edges.size() == 1) {
			
			successor(edges.get(0), captures, playerId);
			int heur = heuristic(captures, playerId);
			predecessor(edges.get(0), captures, playerId);
			
			return new Triple(heur, edges.get(0), depth);
		}
		
		edges.sort(prioritySort);
		List<Edge> copyEdges = new ArrayList<Edge>(); 
		copyEdges.addAll(edges);
		
		for (Edge ee : edges ) {
			
			// apply the edge removal
			boolean hasCaptured = successor(ee, captures, playerId);
			index = copyEdges.indexOf(ee);
			copyEdges.remove(index);
			
			// Get the game score or margin at this layer
			int heur = heuristic(captures, playerId);
			
			// do minimax
			int nextPlayer = (hasCaptured) ? playerId : (3-playerId);
			
			int s = heur;
			if (depth != 0) {
				s = (hasCaptured ? negamax(verts, copyEdges, captures, nextPlayer, depth-1, alpha, beta).score : 
            		-negamax(verts, copyEdges, captures, nextPlayer, depth-1, -beta, -alpha).score);
			}
			
			// Update scores or set the score to the first element when first run
            if(s > score || bestEdge == null ) {
                score = s;
                bestEdge = ee;
                alpha = score > alpha ? score : alpha;
            }
            
			// predecessor
			predecessor(ee, captures, playerId);
			copyEdges.add(index, ee);
            
			// Alpha Beta check
            if(alpha >= beta) break;

			// Beam check
            iters++;
            if (iters >= beamFactor) break;
		}
		
		return new Triple(score, bestEdge, depth);
	}
    
	/**
	 * Successor function 
	 * @param edge A move to make and to update.
	 * @param captures An array for the two players' scores.
	 * @param playerId The player who is capturing. 
	 * @return
	 */
	private boolean successor(Edge edge, int[] captures, int playerId) {
		
		Vertex v0 = edge.getV0(), v1 = edge.getV1();
		boolean capture = false;
		
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
		
		return capture;
	}
	
	/**
	 * Predecessor function 
	 * @param edge A move to make and to update.
	 * @param captures An array for the two players' scores.
	 * @param playerId The player who is capturing. 
	 * @return
	 */
	private void predecessor(Edge edge, int[] captures, int playerId) {
		Vertex v0 = edge.getV0(), v1 = edge.getV1();
		
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
    private void printStats(int score, int playerId, Edge ee, int depth, long start) {
        
        System.out.println("=======================");
        System.out.println("Tiptaco's Dots Bot 3 v1 Statistics:");
        System.out.println("Player:      " + playerId + " ("+(playerId==1?"Blue":"Red")+")");
        System.out.println("Turn:        " + turn++);
        System.out.println("Expectation: " + score);
        System.out.println("Edge:    	(" + ee);
        System.out.println("Depth:       " + depth);
        System.out.println("Time:        "  + (System.currentTimeMillis() - start) + "ms");
    }

	private int[] getScores(DotsAndBoxes game) {
		return new int[] { game.getScore(1), game.getScore(2) };
	}

	public final static Comparator<Edge> prioritySort = new Comparator<Edge>() {         
		@Override         
		public int compare(Edge e1, Edge e2) {  
			int p1 = e1.getPriority(), p2 = e2.getPriority();
			return (p2 < p1 ? -1 : (p2 == p1 ? 0 : 1));           
		}     
	}; 
	
	@Override
	public String getName() { return "TipTacos's Dots and Boxes 3 v1 Bot"; }
    
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
    	Edge edge;
    	int score, depth;
    	
    	public Triple(int score, Edge edge) {
    		this(score, edge, -1);
    	}
    	
    	public Triple(int score, Edge edge, int depth) {
    		this.score = score;
    		this.edge = edge;
    		this.depth = depth;
    	}
    }
    
    
    class Vertex {
    	private int x, y, owner;
    	Set<Edge> edges;
    	
    	public Vertex() {
    		edges = new HashSet<Edge>();
    	}
    	
    	public Vertex(int x, int y) {
    		this();
    		this.x = x; 
    		this.y = y;
    		this.owner = 0;
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
    	
    	public String toString() {
    		return "Vert (" + x + ", " + y + ")"; 
    	}
    }
    
    
    class Edge {
    	Vertex v0, v1;
    	
    	public Edge() {}
    	
    	public Edge(Vertex v0, Vertex v1) {
    		this.v0 = v0;
    		this.v1 = v1;
    		if (v0 == null || v1 == null) { System.out.println("WRONG"); }
    	}
    	
    	public Vertex getOther(Vertex vIn) {
    		return vIn.equals(v0) ? v1 : v0;
    	}
    	
    	public String toString() {
    		return "Edge (" + v0 + " to " + v1 + ")";
    	}
    	
    	public int getPriority() {
    		int d0 = v0.getDegree(), d1 = v1.getDegree();
    		int prio = 0;
    		
    		prio += (d0 == 1 ? 10 : 0) + (d1 == 1 ? 10 : 0);
    		prio += (d0 == 2 ? (d1 == 1 ? 10 : -10) : 0) + (d1 == 2 ? (d0 == 1 ? 10 : -10) : 0);
    		prio += (d0 == 3 ? 2 : 0) + (d1 == 3 ? 2 : 0);
    		
    		return prio;
    	}
    	
    	public Vertex getV0() { return v0; }
    	public Vertex getV1() { return v1; }
    }
}