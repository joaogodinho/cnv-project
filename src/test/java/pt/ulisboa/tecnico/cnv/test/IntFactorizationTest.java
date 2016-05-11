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
    private int limit = 1000000;
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

    // Returns a list of semiprimes with the given size
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

    // Get 100 000 random primes from the primes list,
    // print the number of bits of each one
    // and pass them to IntFactorization, which
    // returns the number of instructions
    //@Test
    public void primesInstr() {
        ArrayList<BigInteger> primes = new ArrayList<BigInteger>();
        Random rand = new Random(new Date().getTime());
        int size = 100000;
        while (primes.size() < size) {
            BigInteger numb = numbers.get(rand.nextInt(limit));
            primes.add(numb);
            System.out.println(numb.bitLength());
        }
        assert primes.size() == size;
        IntFactorization intFact;
        for (BigInteger bi: primes) {
            intFact = new IntFactorization();
            intFact.calcPrimeFactors(bi);
        }
    }

    // Get 100 000 random semiprimes, print the
    // number of bits of each one and pass them
    // to IntFactorization, which returns the number
    // of instructions
    //@Test
    public void semiPrimesInstr() {
        ArrayList<BigInteger> semiprimes = generateSemiPrimes(100000);
        IntFactorization intFact;
        for (BigInteger bi: semiprimes) {
            intFact = new IntFactorization();
            intFact.calcPrimeFactors(bi);
        }
    }
}
