package pt.ulisboa.tecnico.cnv.test;

import org.junit.Test;
import org.junit.Before;

import java.math.BigInteger;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Date;

import java.math.BigInteger;

import pt.ulisboa.tecnico.cnv.factorization.IntFactorization;

public class IntFactorizationTest {
    private int limit = 10000;
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

    @Test
    public void primes2() {
        Random rand = new Random(new Date().getTime());
        int tempLimit = limit;
        if (numbers.size() > 0) {
            ArrayList<BigInteger> semiprimes = new ArrayList<BigInteger>();
            while (semiprimes.size() < limit) {
                BigInteger semi = numbers.get(rand.nextInt(limit))
                    .multiply(numbers.get(rand.nextInt(limit)));
                //if (semi.bitLength() <= 17) {
                    semiprimes.add(semi);
                    System.out.println(semi.bitLength());
                //}
            }
            assert semiprimes.size() == limit;
            IntFactorization intFact;
            for (BigInteger bi: semiprimes) {
                intFact = new IntFactorization();
                intFact.calcPrimeFactors(bi);
            }
        } else {
            System.out.println("No numbers to test.");
        }
    }
}
