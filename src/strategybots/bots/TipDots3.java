package strategybots.bots;

/* Author: Adrian Shedley with help from Alec Dorrington
 * Date: 8 Dec 2019
 * 
 * A MCTS based Othello bot. Currently still in the debug and dev phase, however still plays well against a moderate human.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import strategybots.bots.TipDots2.Move;
import strategybots.games.DotsAndBoxes;
import strategybots.games.DotsAndBoxes.Side;
import strategybots.games.base.Game.Player;

public class TipDots3 implements Player<DotsAndBoxes>{

	private long time = 2000l;
	private int width, height;
	private int turn = 0;
	private int beamFactor = 10;
	
	public TipDots3() {
		System.out.println("Tip's Dots and Boxes Bot 3 Loaded");
	}
	
	public TipDots3(long millis) {
		this();
		this.time = millis;
	}
	
	@Override 
	public void init(DotsAndBoxes game, int playerId) {
		this.width = game.getWidth();
		this.height = game.getHeight();
	}
	
	@Override
	public void takeTurn(DotsAndBoxes game, int playerId) {

		getBestMove(game, playerId);
		
	}
	
	private Triple getBestMove(DotsAndBoxes game, int playerId) {
		
		// get board as graph
		Board board = getGraph(game, playerId);
		System.out.println(board.getVerts().size());
		System.out.println(board.getEdges().size());
		// get minimax
		// return the edge
		// print stats
		
		
		return null;
	}
	
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
				// Check Top for top Row
				if (x == width - 1 && !game.hasLine(Side.TOP, x, y)) 
					board.getEdges().add(makeSide(board.getVerts(), x, y, x, y));
				// Check Right for right col
				if (y == height - 1 && !game.hasLine(Side.RIGHT, x, y)) 
					board.getEdges().add(makeSide(board.getVerts(), x, y, x, y));
				// check bottom, with special condition bottom row
				if (!game.hasLine(Side.BOTTOM, x, y))
					board.getEdges().add(makeSide(board.getVerts(), x, y, x, y == 0 ? 0 : (y-1)));
				// Left check, with special condition for col 0
				if (!game.hasLine(Side.LEFT, x, y))
					board.getEdges().add(makeSide(board.getVerts(), x, y, x == 0 ? 0 : x-1, y));
			}
		}
		
		return board;
	}
	
	private Edge makeSide(Set<Vertex> verts, int x0, int y0, int x1, int y1) {
		return new Edge(getVertex(verts, x0, y0), getVertex(verts, x1, y1));
	}
	
	private Vertex getVertex(Set<Vertex> verts, int x, int y) {
		
		for (Vertex vv : verts) {
			if (vv.match(x, y)) {
				return vv;
			}
		}
		
		return null;
	}
	
	private Triple negamax(Set<Vertex> verts, Set<Edge> edges, int playerId, int alpha, int beta, int depth) {
		
		int score = 0;
		Edge bestEdge = null;
		
		Set<Edge> copyEdges = new HashSet<Edge>(); 
		copyEdges.addAll(edges);
		
		for (Edge ee : edges ) {
			
			// apply the edge removal
			boolean hasCaptured = successor(ee, playerId);
			copyEdges.remove(ee);
			
			// get the score (captures or Verts with degree == 0)
			int heur = heuristic(verts, playerId);
			
			if (edges.size() == 0) {
				predecessor(ee, playerId);
				copyEdges.add(ee);
				return new Triple(heur, ee, depth);
			}
			
			// do minimax
			int nextPlayer = (hasCaptured) ? playerId : (3-playerId);
			int s = (depth<1) ? heur : (hasCaptured ? 
            		negamax(verts, copyEdges, nextPlayer, depth-1, alpha, beta).score : 
            		-negamax(verts, copyEdges, nextPlayer, depth-1, -beta, -alpha).score);
			
            if(s > score || bestEdge == null ) {
                score = s;
                bestEdge = ee;
                alpha = score > alpha ? score : alpha;
            }
            
			// predecessor
			predecessor(ee, playerId);
			copyEdges.add(ee);
            
			// AB check
            if(alpha >= beta) break;

			// beam check TODO
		}
		
		return new Triple(score, bestEdge, depth);
	}
    
	private boolean successor(Edge edge, int playerId) {
		Vertex v0 = edge.getV0();
		Vertex v1 = edge.getV1();
		
		boolean capture = false;
		
		// V0 first
		v0.removeEdge(edge);
		if (v0.getDegree() == 0) {
			capture = true;
			v0.setOwner(playerId);
		}
		
		if (v0 != v1) {
			v1.removeEdge(edge);
			if (v1.getDegree() == 0) {
				capture = true;
				v1.setOwner(playerId);
			}
		}
		
		return capture;
	}
	
	private void predecessor(Edge edge, int playerId) {
		Vertex v0 = edge.getV0();
		Vertex v1 = edge.getV1();
		
		if (v0.getDegree() == 0) {
			v0.setOwner(0);
		}
		v0.addEdge(edge);
		
		if (v0 != v1) {
			if (v1.getDegree() == 0) {
				v1.setOwner(0);
			}
			v1.addEdge(edge);
		}
	}
	
	private int heuristic(Set<Vertex> verts, int playerId) {
		
		int margin = 0;
		
		for (Vertex vv : verts) {
			int owner = vv.getOwner();
			if (owner == playerId) margin++;
			else if (owner == (3-playerId)) margin--;
		}
		
		return margin;
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
    private void printStats(int score, int playerId, int moveX, int moveY, int moveSide, int depth, long start) {
        
        System.out.println("=======================");
        System.out.println("Tiptaco's Bots Bot 3 Statistics:");
        System.out.println("Player:      " + playerId
                + " ("+(playerId==1?"Blue":"Red")+")");
        System.out.println("Turn:        " + turn++);
        System.out.println("Expectation: " + score);
        System.out.println("Position:    (" + (moveX+1) + ", " + (moveY+1) + ", " + moveSide + ")");
        System.out.println("Depth:       " + depth);
        System.out.println("Time:        "
                + (System.currentTimeMillis() - start) + "ms");
    }

	private int[] getScores(DotsAndBoxes game) {
		return new int[] { game.getScore(1), game.getScore(2) };
	}
    
    @Override
    public String getName() { return "TipTacos's Dots and Boxes 3 Bot"; }

    
    
    class Board {
    	Set<Edge> edges;
    	Set<Vertex> verts;
    	
    	public Board() {
    		edges = new HashSet<Edge>();
    		verts = new HashSet<Vertex>();
    	}
    	
    	public void addEdge(Edge ee) { edges.add(ee); }
    	
    	public void addVertex(Vertex vv) { verts.add(vv); }
    	
    	public Set<Edge> getEdges() { return edges; }
    	
    	public Set<Vertex> getVerts() { return verts; }

    }
    
    class Triple {
    	Edge edge;
    	int score;
    	int depth;
    	
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
    	}
    	
    	public Vertex getOther(Vertex vIn) {
    		return vIn.equals(v0) ? v1 : v0;
    	}
    	
    	public String toString() {
    		return "Edge (" + v0 + " to " + v1 + ")";
    	}
    	
    	public Vertex getV0() { return v0; }
    	public Vertex getV1() { return v1; }
    }
}
