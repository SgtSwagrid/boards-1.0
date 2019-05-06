package bots.zobrist;

import java.util.Random;

public class Zobrist {

	private Long seed; 
	
	private Random rand;
	
	/**
	 * Create a new Zobrist with a given seed 
	 * @param seed Seed for Zobrist keys
	 */
	public Zobrist(Long seed)
	{
		this.seed = seed;
		rand = new Random(seed);
	}
	
	/**
	 * Returns the seed used for this Zobrist
	 * @return 
	 */
	public Long getSeed()
	{
		return seed;
	}
	
	/**
	 * Returns the next key from the Zobrist set
	 * @return
	 */
	public Long getRandomKey()
	{
		return rand.nextLong();
	}
	
	/**
	 * Hashes the key into the hash
	 * @param hash the current Zobrist hash
	 * @param key the key to be added or subtracted
	 * @return
	 */
	public Long hash(Long hash, Long key)
	{
		return (hash ^ key);
	}
	
	
}
