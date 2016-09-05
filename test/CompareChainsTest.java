package test;

import util.BlockExplorer;
import util.Filename;

/**
 * Compares two blockchain files to determine if hashes are the same.
 * Runs from the command line and takes exactly two arguments.
 */
public class CompareChainsTest {

	public static void main(String[] args) {
		
		if (args.length != 2) {
			System.err.println("Arguments: [blockchain file 1] [blockchain file 2]");
			System.exit(0);
		}
		
		Filename filename1 = new Filename(args[0]);
		Filename filename2 = new Filename(args[1]);
		
		BlockExplorer explorer1 = new BlockExplorer(filename1);
		BlockExplorer explorer2 = new BlockExplorer(filename2);
		
		int height1 = explorer1.getBlockchainHeight();
		int height2 = explorer2.getBlockchainHeight();
		int end = Math.max(height1, height2);
		
		System.out.println("Height of " + args[0] + ": " + height1);
		System.out.println("Height of " + args[1] + ": " + height2);
		System.out.println();
		
		String height;
		String pow1, pow2;
		
		for (int i = 1; i <= end; i++) {
			
			height = String.valueOf(i);
			pow1 = explorer1.getPoWByHeight(height);
			pow2 = explorer2.getPoWByHeight(height);
			
			if (pow1.compareTo(pow2) == 0) {
				System.out.println("Block " + height + ": match");
			} else {
				System.out.println("Block " + height + ": do not match");
				System.out.println("Hash for " + args[0] + ": " + pow1);
				System.out.println("Hash for " + args[1] + ": " + pow2);
			}
			
			System.out.println("------------------------------------------------");
		}

	}

}
