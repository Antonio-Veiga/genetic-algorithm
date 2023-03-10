package classes;

import java.util.ArrayList;

/**
 * This class represents a generation of chromosomes
 */
public class Generation implements Cloneable {
    private final int population;
    private ArrayList<Chromosome> generation;
    private double genAvgPos;
    private double genAvgSpeed;

    /**
     * This method creates a new instance of the <i>Generation class</i>.
     *
     * @param population, size of the population.
     */
    public Generation(int population) {
        this.population = population;
        this.generation = new ArrayList<>(population);
    }

    public ArrayList<Chromosome> getGeneration() {
        return this.generation;
    }

    public void setGeneration(ArrayList<Chromosome> generation) {
        this.generation = generation;
    }

    /**
     * This method calculates a generation average x_pos value in the game stage.
     *
     * @return the generation average x_pos
     * @throws NullPointerException in case of a NullPointerException it will return 0.
     */
    public double calculateGenAveragePos() {
        try {
            double pos = 0.0;

            for (Chromosome tmp : generation) {
                pos += tmp.getResults().getX_pos();
            }

            return pos / population;
        } catch (NullPointerException n) {
            return 0;
        }
    }

    /**
     * This method calculates a generation average completion time in the game stage.
     *
     * @return the generation average time.
     * @throws NullPointerException in case of a NullPointerException it will return 0.
     */
    public double calculateGenAverageTime() {
        try {
            double time = 0.0;

            for (Chromosome tmp : generation) {
                time += tmp.getResults().getTime_left();
            }

            return time / population;
        } catch (NullPointerException n) {
            return 0;
        }
    }

    /**
     * This method calculates a generation average coins collected in the game stage.
     *
     * @return the generation average coins collected.
     * @throws NullPointerException in case of a NullPointerException it will return 0.
     */
    public double calculateGenAverageCoins() {
        try {
            double coins = 0.0;

            for (Chromosome tmp : generation) {
                coins += tmp.getResults().getCoins();
            }

            return coins / population;
        } catch (NullPointerException n) {
            return 0;
        }
    }

    /**
     * This method calculates a generation average score in the game stage.
     *
     * @return the generation average score.
     * @throws NullPointerException in case of a NullPointerException it will return 0.
     */
    public double calculateGenAverageScore() {
        try {
            double score = 0.0;

            for (Chromosome tmp : generation) {
                score += tmp.getResults().getScore();
            }

            return score / population;
        } catch (NullPointerException n) {
            return 0;
        }
    }

    /**
     * This method calculates a generation average speed score in the game stage.
     *
     * @param world represents the world.
     * @param level represents the level.
     * @return the generation average speed.
     * @throws NullPointerException in case NullPointerException it will return 0.
     */
    public double calculateGenAverageSpeed(int world, int level) {
        try {
            double speed = 0.0;

            for (Chromosome tmp : generation) {
                double pos = tmp.getResults().getX_pos();
                double time = tmp.getResults().getTime_left();

                speed += pos / (GeneticAlgorithm.levelTimeMapping[world - 1][level - 1] - time);
            }

            return speed / population;
        } catch (NullPointerException n) {
            return 0;
        }
    }

    /**
     * This method calculates a generation average fitness score in the game stage.
     * In order to determine the best solution.
     *
     * @return the generation average fitness score.
     * @throws NullPointerException in case of a NullPointerException it will return 0.
     */
    public double calculateGenAverageFitnessScore() {
        try {
            double fitness = 0.0;

            for (Chromosome tmp : generation) {
                fitness += tmp.getFitnessValue();
            }
            return fitness / population;
        } catch (NullPointerException n) {
            return 0;
        }
    }

    /**
     * This method returns the Generation average x_pos.
     *
     * @return the average x_pos.
     */
    public double getGenAvgPos() {
        return genAvgPos;
    }

    /**
     * This method sets the Generation average x_pos.
     *
     * @param genAvgPos the average x_pos value to be inserted.
     */
    public void setGenAvgPos(double genAvgPos) {
        this.genAvgPos = genAvgPos;
    }

    /**
     * This method returns the Generation average speed.
     *
     * @return the average speed.
     */
    public double getGenAvgSpeed() {
        return genAvgSpeed;
    }

    /**
     * This method sets the Generation average speed.
     *
     * @param genAvgSpeed the average speed value to be inserted.
     */
    public void setGenAvgSpeed(double genAvgSpeed) {
        this.genAvgSpeed = genAvgSpeed;
    }

    @Override
    public Generation clone() {
        try {
            Generation clone = (Generation) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
