package bots.zobrist;

import java.io.Serializable;
import java.util.Random;

public class Zobrist implements Serializable {

	private int seed; 

	private int[][][] zobrist;
	
	private int tableSize, currentSize;
	private int keys[], visits[], score[];
	
	private Random rand;
	
	/**
	 * Create a new Zobrist with a given seed 
	 * @param seed Seed for Zobrist keys
	 */
	public Zobrist(int tableSize, int[][] board, int noPlayers, int seed)
	{
		this.seed = seed;
		this.tableSize = tableSize;
		
		rand = new Random(seed);
		zobrist = generateZobrist(board, noPlayers);
		
		keys = new int[tableSize];
		visits = new int[tableSize];
		score = new int[tableSize];
	}
	
	public void put(int[][] board, int newVisits, int newScore)
	{
		int hash = getHash(board);
		
		int key = hash % tableSize;
		
		if (keys[key] == 0)
		{
			keys[key] = hash;
			score[key] = 0;
			visits[key] = 0;
			
			currentSize++;
		}
		
		if (keys[key] == hash)
		{
			visits[key] += newVisits;
			score[key] += newScore;
		}
	}
	
	public boolean isEntry(int[][] board)
	{
		int hash = getHash(board);
		
		int key = hash % tableSize;
		
		if (keys[key] == 0)
		{
			return false;
		}

		if (keys[key] != hash)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public int getZobrist(int[][] board)
	{
		int hash = getHash(board);
		
		return keys[hash % tableSize];
	}
	
	public int getVisits(int[][] board)
	{
		int hash = getHash(board);
		
		return visits[hash % tableSize];
	}
	
	public int getScore(int[][] board)
	{
		int hash = getHash(board);
		
		return score[hash % tableSize];
	}
	
	public int size()
	{
		return currentSize;
	}
	
	/**
	 * Returns the seed used for this Zobrist
	 * @return 
	 */
	public Integer getSeed()
	{
		return seed;
	}
	
	/**
	 * Returns the next key from the Zobrist set
	 * @return
	 */
	public Integer getRandomKey()
	{
		return rand.nextInt();
	}
	
	/**
	 * Hashes the key into the hash
	 * @param hash the current Zobrist hash
	 * @param key the key to be added or subtracted
	 * @return
	 */
	public int hash(Integer hash, Integer key)
	{
		return (hash ^ key);
	}
	
	public int getHash(int[][] board)
	{
		int hash = 0; 
		
		for(int x = 0; x < board.length; x++) {
			for(int y = 0; y < board[x].length; y++) {
				if (board[x][y] != 0)
				{
					hash ^= zobrist[x][y][board[x][y] - 1];
				}
			}
		}
		
		return hash;
	}
	
	private int[][][] generateZobrist(int[][] board, int numPieces) {
		
		int[][][] zobrist = new int[board.length][board[0].length][numPieces];
		
		for(int x = 0; x < board.length; x++) {
			for(int y = 0; y < board[x].length; y++) {
				for(int i = 0; i < numPieces; i++) {
					
					zobrist[x][y][i] = (int) (rand.nextInt(Integer.MAX_VALUE));
				}
			}
		}
		
		return zobrist;
	}
	
}
