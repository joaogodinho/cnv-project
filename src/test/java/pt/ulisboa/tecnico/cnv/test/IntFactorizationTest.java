package pt.ulisboa.tecnico.cnv.test;

import org.junit.Test;

import java.math.BigInteger;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import java.math.BigInteger;

import pt.ulisboa.tecnico.cnv.factorization.IntFactorization;

public class IntFactorizationTest {

    //@Test
    public void primes1() {
        int limit = 100;
        ClassLoader classLoader = getClass().getClassLoader();
        ArrayList<BigInteger> numbers = new ArrayList<BigInteger>();
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
        if (numbers.size() > 0) {
            IntFactorization intFact;
            for (BigInteger bi: numbers) {
                intFact = new IntFactorization();
                intFact.calcPrimeFactors(bi);
            }
        } else {
            System.out.println("No numbers to test.");
        }
    }
}
