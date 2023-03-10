package classes;

import luigi.RunResult;

import java.util.ArrayList;

/**
 *  A class that represents a chromosome
 *
 */
public class Chromosome implements Cloneable, Comparable<Chromosome>{

    private ArrayList<Integer> inputs;
    private double fitnessValue;
    private RunResult results = null;

    /**
     * This method creates a new instance of the <i>Chromosome class</i>.
     */
    public Chromosome(){
        this.inputs = new ArrayList<>();
        this.fitnessValue = 0;
    }

    /**
     * This method returns an array of the inputted commands for the character to make.
     * @return an array of the commands inputted.
     */
    public ArrayList<Integer> getInputs() {
        return this.inputs;
    }

    /**
     * This method sets the value for the <i>Mario</i> character to make.
     * @param inputs, new array of values for the inputted commands list.
     */
    public void setInputs(ArrayList<Integer> inputs) {
        this.inputs = inputs;
    }

    /**
     * This method sets the fitness value.
     * @param fitnessValue, new fitness valued.
     */
    public void setFitnessValue(double fitnessValue) {
        this.fitnessValue = fitnessValue;
    }

    /**
     * This method returns the fitness value of a <i>Chromosome</i>.
     * @return fitness value of a <i>Chromosome</i>.
     */
    public double getFitnessValue() {
        return this.fitnessValue;
    }

    /**
     * This method returns the <i>results</i> after death or completion of the game.
     * @return the <i>results</i> of the game.
     */
    public RunResult getResults() {
        return this.results;
    }

    /**
     * This method
     * @param run
     * @return
     */
    public Chromosome assertResult(RunResult run){
        this.results = run;
        return this;
    }

    /**
     * This method clones this Chromosome
     * @return the cloned Chromosome
     */
    @Override
    public Chromosome clone() {
        try {
            return (Chromosome) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }


    /**
     * This method states if a chromosome is a clone
     * @param b the chromosome to compare
     * @return if this chromosome is a clone
     */
    public boolean isClone(Chromosome b){
        return this.getInputs().equals(b.getInputs());
    }

    /**
     * This method compares chromosomes
     * @param o the comparable chromosome
     * @return the offset value
     */
    @Override
    public int compareTo(Chromosome o) {
        return (int) (this.fitnessValue - o.getFitnessValue());
    }
}
