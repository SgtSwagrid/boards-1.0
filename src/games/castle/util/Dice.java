package games.castle.util;
import java.util.ArrayList;
import java.util.Random;

/**
 * Dice class used to both roll dice and manipulate arrays of rolls
 * @author Adrian
 *
 */

public class Dice {

	private int sides = 0;	
	private Random rand;
	
	public Dice(int sides)
	{
		this.sides = sides;
		rand = new Random();
	}
	
	/** 
	 * Roll this dice once.
	 * @return A single roll of this die
	 */
	public int roll()
	{
		return rand.nextInt(sides) + 1;
	}
	
	/**
	 * Roll this dice many times
	 * @param times number of rolls
	 * @return A list of rolls, in no particular order
	 */
	public int[] roll(int times)
	{
		int[] rolls = new int[times];
		
		for (int ii = 0; ii < times; ii++)
		{
			rolls[ii] = roll();
		}
		
		return rolls;
	}
	
	/**
	 * 
	 * @param rolls An array of Rolls
	 * @return The highest roll from this set
	 */
	public int max(int[] rolls)
	{
		int max = 0;
		
		for (int roll : rolls)
		{
			if (roll > max) max = roll;
		}
		
		return max;
	}
	

	
	/**
	 * Places the rolls into an array, [0] is for 1's, [1] is for 2's etc
	 * @param rolls array
	 * @param sides number sided dice
	 * @return
	 */
	public static int[] bin(int[] rolls, int sides)
	{
		int[] bins = new int[sides];
		
		for (int roll : rolls)
		{
			bins[roll-1]++;
		}
		
		return bins;
	}
	
	public int[] bin(int[] rolls)
	{
		return bin(rolls, sides);
	}
	
	
	public static void main(String[] args)
	{
		int[] arr = {1, 2, 2, 3, 4, 4, 4};
		
		int[] ordered = order(arr, 4);
		int[] bin = bin(arr, 4);
		
		for (int i : ordered)
		{
			System.out.print(i + ", ");
		}
		
		System.out.println("\n BIN 4 =" + bin[3]);
	}
	
	/**
	 * Order an input array of integers
	 * @param rolls The array of rolls
	 * @param sides the number of sides for this dice rollset
	 * @return a descending order dice set
	 */
	public static int[] order(int[] rolls, int sides)
	{
		int[] bins = bin(rolls, sides);
		int[] ordered = new int[rolls.length];
		
		int index = 0;
		
		for (int ii = bins.length - 1 ; ii >= 0; ii--)
		{
			while (bins[ii] > 0)
			{
				ordered[index] = (ii+1);
				bins[ii]--;
				index++;
			}
		}
		
		return ordered;
	}
	
	public int[] order(int[] rolls)
	{
		return order(rolls, sides);
	}
	
}
