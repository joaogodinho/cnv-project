package pt.ulisboa.tecnico.cnv.test;

import org.junit.Test;

import java.math.BigInteger;

import pt.ulisboa.tecnico.cnv.factorization.IntFactorization;

public class IntFactorizationTest {
    @Test
    public void simpleCall() {
        IntFactorization obj = new IntFactorization();
        System.out.println(obj.calcPrimeFactors(new BigInteger("74513")));
    }
}
