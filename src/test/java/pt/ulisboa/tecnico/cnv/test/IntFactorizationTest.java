package pt.ulisboa.tecnico.cnv.test;

import org.junit.Test;
import org.junit.Before;

import java.math.BigInteger;
import java.lang.Math;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Date;

import java.math.BigInteger;

import pt.ulisboa.tecnico.cnv.factorization.IntFactorization;

public class IntFactorizationTest {
    private int limit = 100000;
    private ArrayList<BigInteger> numbers;

    @Before
    public void loadFile() {
        ClassLoader classLoader = getClass().getClassLoader();
        numbers = new ArrayList<BigInteger>();
        try (BufferedReader br = new BufferedReader(
                    new FileReader(classLoader.getResource("primes1.txt").getFile()))) {

            // Skip file header
            br.readLine();
            br.readLine();
            br.readLine();
            br.readLine();

            String currLine;
            int limitCheck = 0;
limitReached:
            while ((currLine = br.readLine()) != null) {
                for (String s: currLine.split(" ")) {
                    String trimmed = s.trim();
                    if (trimmed.length() > 0) {
                        numbers.add(new BigInteger(trimmed));
                        if (++limitCheck > limit) {
                            break limitReached;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void primes1() {
        if (numbers.size() > 0) {
            IntFactorization intFact;
            for (BigInteger bi: numbers) {
                //System.out.println(bi.bitLength());
                intFact = new IntFactorization();
                intFact.calcPrimeFactors(bi);
            }
        } else {
            System.out.println("No numbers to test.");
        }
    }

    private ArrayList<BigInteger> generateSemiPrimes(int size) {
        ArrayList<BigInteger> semiprimes = new ArrayList<BigInteger>();
        Random rand = new Random(new Date().getTime());
        int tempLimit = size;
        if (numbers.size() > 0) {
            while (semiprimes.size() < tempLimit) {
                BigInteger semi = numbers.get(rand.nextInt(limit))
                    .multiply(numbers.get(rand.nextInt(limit)));
                semiprimes.add(semi);
                System.out.println(semi.bitLength());
            }
        }
        return semiprimes;
    }

    @Test
    public void primes2() {
        ArrayList<BigInteger> semiprimes = generateSemiPrimes(limit);
        IntFactorization intFact;
        for (BigInteger bi: semiprimes) {
            intFact = new IntFactorization();
            intFact.calcPrimeFactors(bi);
        }
    }

    //@Test
    public void testRegression() {
        ArrayList<BigInteger> semiprimes = generateSemiPrimes(100000);
        IntFactorization intFact;
        for (BigInteger bi: semiprimes) {
            intFact = new IntFactorization();
            intFact.calcPrimeFactors(bi);
        }
    }

    private long predictNumbInstr(int bits) {
        double CUBIC = 535.75,
               SQUARE= -13187,
               LINEAR= 136270,
               ZERO  = -79587;
        return (long) Math.floor(CUBIC * Math.pow(bits, 3) +
                          SQUARE * Math.pow(bits, 2) +
                          LINEAR * bits +
                          ZERO);
    }
}
