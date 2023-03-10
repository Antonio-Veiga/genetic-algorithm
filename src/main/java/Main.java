
import classes.Chromosome;
import classes.Generation;
import classes.GeneticAlgorithm;
import classes.PropertiesInfo;
import luigi.MarioUtils;
import luigi.Request;
import luigi.RunResult;

import java.util.*;
import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException {

        PropertiesInfo pI = new PropertiesInfo();

        String ip = pI.getIp();
        String importFile = pI.getImpFile();
        int maxGen = pI.getGenValue();

        // main variables Genetic Algorithm and the Mario Server
        GeneticAlgorithm gen = GeneticAlgorithm.getGeneticAlgorithm();

        // IP de casa 192.168.1.250, configure no ficheiro properties.
        MarioUtils server = new MarioUtils(ip);

        // verifies if there is a file with a solution to import
        if (!(importFile.equals("NoImport"))) {
            gen.readGenFromFile(importFile);
        }


        //Class rationale while the generation isn´t maxGen, continue upgrading
        while (gen.getNumGen() <= maxGen) {
            // get the current generation
            Generation currGen = gen.getCurrGen();

            for (Chromosome tmp : currGen.getGeneration()) {
                ArrayList<Integer> inputs = tmp.getInputs();

                // make the request to the Mario Server, and store the results
                Request request = new Request(convertToArray(inputs), gen.getLevel(), String.valueOf(gen.isRender()));
                RunResult res = server.goMarioGo(request, 8080);

                // changing this variable also changes gen.getCurrGen() variable
                tmp.assertResult(res);
            }

            // construct a new generation from the previous one
            gen.calculateFitness();
            System.out.println("\nAverage X_POS: " + gen.getCurrGen().calculateGenAveragePos());
            System.out.println("Average Speed: " + gen.getCurrGen().calculateGenAverageSpeed(pI.getWorld(), pI.getStage()));
            System.out.println("Average Fitness: " + gen.getCurrGen().calculateGenAverageFitnessScore());
            System.out.println("\n\n");

            Chromosome bestFit = gen.getBestFit();

            Request request = new Request(convertToArray(bestFit.getInputs()), gen.getLevel(), "true");
            server.goMarioGo(request, 8080);

            gen.breedNewGen();
        }

        /*GeneticAlgorithm gen = GeneticAlgorithm.getGeneticAlgorithm();
        String importFile = "1653355585364";
        gen.readGenFromFile(importFile);
        MarioUtils server = new MarioUtils("192.168.1.98");


        //Class rationale while the generation isn´t 500, continue upgrading
        while (gen.getNumGen() < 50) {
            // get the current generation
            Generation currGen = gen.getCurrGen();

            for (Chromosome tmp : currGen.getGeneration()) {
                ArrayList<Integer> inputs = tmp.getInputs();

                // make the request to the Mario Server, and store the results
                Request request = new Request(convertToArray(inputs), gen.getLevel(), String.valueOf(gen.isRender()));
                RunResult res = server.goMarioGo(request,8080);

                // changing this variable also changes gen.getCurrGen() variable
                tmp.assertResult(res);
            }

            // construct a new generation from the previous one
            gen.calculateFitness();
            System.out.println("Average X_POS: " + gen.getCurrGen().calculateGenAveragePos());
            System.out.println("Average Speed: " + gen.getCurrGen().calculateGenAverageSpeed(1,1));
            System.out.println("Average Fitness: " + gen.getCurrGen().calculateGenAverageFitnessScore());

            Chromosome bestFit = gen.getBestFit();

            System.out.println("\nBest Speed: " + bestFit.getResults().getTime_left());

            gen.breedNewGen();
        }*/
    }

    public static Integer[] convertToArray(ArrayList<Integer> inputs) {
        Integer[] arr = new Integer[inputs.size()];
        int index = 0;
        for (final Integer value : inputs) {
            arr[index++] = value;
        }
        return arr;
    }
}