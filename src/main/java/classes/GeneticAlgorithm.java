package classes;

import javafx.util.Pair;
import luigi.RunResult;
import org.apache.commons.lang3.EnumUtils;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Class that represents a genetic Algorithm, following the Singleton Design Pattern
 */
public final class GeneticAlgorithm {

    // the instance of this class
    private static volatile GeneticAlgorithm instance;

    //solution space
    private final int[] space = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

    // from https://www.mariowiki.com/Super_Mario_Bros.
    static int[][] levelTimeMapping = {{400, 400, 300, 300},
            {400, 400, 300, 300},
            {400, 300, 300, 300},
            {400, 400, 300, 400},
            {300, 400, 300, 300},
            {400, 400, 300, 300},
            {400, 400, 300, 400},
            {300, 400, 300, 400}};

    // button frequencies
    private int[] buttonFrequencies = new int[]{2, 10, 20, 30, 10, 10, 3, 5, 5, 3, 1, 1};

    // configuration variables
    private int scoreWeight; // between 0-1000
    private int coinWeight; // beetween 0-1000
    private int speedrunWeight; // between 0-1000

    private String level = "SuperMarioBros-1-1-v0";
    private int world = 1;
    private int lvl = 1;
    private boolean render = false;

    // selection types
    private boolean fitnessProportionate = true;
    private boolean stochastic = false;
    private boolean tournament = false;
    private boolean truncation = false;

    private boolean elitism = true;
    private int top = 5;

    private int populationSize = 100; // between 0-1000
    private String crossoverType = "UNIFORM"; // See CROSSOVER_TYPES
    private double crossoverOnePointRate = 0.50; // between 0.00-1.00
    private double crossoverMultiPointMin = 0.00; // between 0.00-1.00
    private double crossoverMultiPointMax = 0.00; // between 0.00-1.00
    private double crossoverUniformRate = 0.50; // between 0.00-1.00
    private double crossoverFrequency = 0.95; // between 0.00-1.00
    private double mutationFrequency = 0.015; // between 0.00-1.00

    // stored best values
    private Generation currGen;
    private int numGen;
    private Chromosome bestFit;
    private int chromosome = 0;

    private PropertiesInfo pI;
    // this algorithm session file
    private String fileName;

    private GeneticAlgorithm() {
        String propsPath = "/GeneticAlgorithm.properties";

        try (InputStream input = getClass().getResourceAsStream(propsPath)) {

            Properties props = new Properties();

            if (input == null) {
                System.out.println("Sorry, unable to find " + propsPath + ", starting with default values.");
                return;
            }

            props.load(input);

            try {
                this.render = Boolean.parseBoolean(props.getProperty("RENDER"));
            } catch (NullPointerException n) {
                System.out.println("A problem occurred getting render variable -> Switched to default values.");
            }

            try {
                int world = Integer.parseInt(props.getProperty("WORLD"));
                int level = Integer.parseInt(props.getProperty("LEVEL"));
                int version = Integer.parseInt(props.getProperty("VERSION"));

                if (world >= 1 && world <= 8) {
                    if (level >= 1 && level <= 4) {
                        if (version >= 0 && version <= 3) {
                            this.world = world;
                            this.lvl = level;
                            this.level = "SuperMarioBros-" + world + "-" + level + "-v" + version;

                        }
                    }
                }
            } catch (NullPointerException | NumberFormatException n) {
                System.out.println("A problem occurred getting world, level and version -> Switched to default values.");
            }

            try {
                int button0 = Integer.parseInt(props.getProperty("FREQUENCY_BUTTON_0"));
                int button1 = Integer.parseInt(props.getProperty("FREQUENCY_BUTTON_1"));
                int button2 = Integer.parseInt(props.getProperty("FREQUENCY_BUTTON_2"));
                int button3 = Integer.parseInt(props.getProperty("FREQUENCY_BUTTON_3"));
                int button4 = Integer.parseInt(props.getProperty("FREQUENCY_BUTTON_4"));
                int button5 = Integer.parseInt(props.getProperty("FREQUENCY_BUTTON_5"));
                int button6 = Integer.parseInt(props.getProperty("FREQUENCY_BUTTON_6"));
                int button7 = Integer.parseInt(props.getProperty("FREQUENCY_BUTTON_7"));
                int button8 = Integer.parseInt(props.getProperty("FREQUENCY_BUTTON_8"));
                int button9 = Integer.parseInt(props.getProperty("FREQUENCY_BUTTON_9"));
                int button10 = Integer.parseInt(props.getProperty("FREQUENCY_BUTTON_10"));
                int button11 = Integer.parseInt(props.getProperty("FREQUENCY_BUTTON_11"));

                if (button0 >= 0 && button1 >= 0 && button2 >= 0 && button3 >= 0 && button4 >= 0
                        && button5 >= 0 && button6 >= 0 && button7 >= 0 && button8 >= 0 && button9 >= 0
                        && button10 >= 0 && button11 >= 0 && (button0 + button1 + button2 + button3 + button4
                        + button5 + button6 + button7 + button8 + button9 + button10 + button11) == 100) {
                    this.buttonFrequencies = new int[]{button0, button1, button2, button3, button4, button5
                            , button6, button7, button8, button9, button10, button11};
                } else {
                    throw new NumberFormatException();
                }
            } catch (NullPointerException | NumberFormatException n) {
                System.out.println("A problem occurred getting button frequencies -> Switched to default values");
            }

            try {
                int scoreWeight = Integer.parseInt(props.getProperty("SCORE_WEIGHT"));
                int coinWeight = Integer.parseInt(props.getProperty("COIN_WEIGHT"));
                int speedrunWeight = Integer.parseInt(props.getProperty("SPEEDRUN_WEIGHT"));

                if (scoreWeight >= 0 && scoreWeight <= 10) {
                    this.scoreWeight = scoreWeight;
                }

                if (coinWeight >= 0 && coinWeight <= 10) {
                    this.coinWeight = coinWeight;
                }

                if (speedrunWeight >= 0 && speedrunWeight <= 10) {
                    this.speedrunWeight = speedrunWeight;
                }

            } catch (NullPointerException | NumberFormatException n) {
                this.scoreWeight = 10;
                this.coinWeight = 10;
                this.speedrunWeight = 10;
                System.out.println("A problem occurred getting weights values -> Switched to default values.");
            }

            try {
                int populationSize = Integer.parseInt(props.getProperty("POPULATION_SIZE"));

                if (populationSize >= 0 && populationSize <= 1000) {
                    this.populationSize = populationSize;
                }
            } catch (NullPointerException | NumberFormatException n) {
                populationSize = 1000;
                System.out.println("A problem occurred getting population size -> Switched to default values.");
            }

            try {
                if (EnumUtils.isValidEnum(CROSSOVER_TYPES.class, props.getProperty("CROSSOVER_TYPE"))) {
                    this.crossoverType = props.getProperty("CROSSOVER_TYPE");
                }
            } catch (NullPointerException n) {
                System.out.println("A problem occurred getting the crossover type -> Switched to default values.");
            }

            try {
                switch (this.crossoverType) {
                    case "ONE_POINT":
                        double crossoverOnePointRate = Double.parseDouble(props.getProperty("CROSSOVER_ONE_POINT_RATE"));

                        if (crossoverOnePointRate >= 0.00 && crossoverOnePointRate <= 1.00) {
                            this.crossoverOnePointRate = crossoverOnePointRate;
                        }
                        break;

                    case "MULTI_POINT":
                        double crossoverMultiPointMin = Double.parseDouble(props.getProperty("CROSSOVER_MULTI_POINT_MIN"));
                        double crossoverMultiPointMax = Double.parseDouble(props.getProperty("CROSSOVER_MULTI_POINT_MAX"));

                        if (crossoverMultiPointMin >= 0.00 &&
                                crossoverMultiPointMax >= 0.00 && crossoverMultiPointMax <= 1.00 &&
                                crossoverMultiPointMax > crossoverMultiPointMin) {
                            this.crossoverMultiPointMin = crossoverMultiPointMin;
                            this.crossoverMultiPointMax = crossoverMultiPointMax;
                        } else {
                            this.crossoverType = "ONE_POINT";
                        }
                        break;

                    case "UNIFORM":
                        double crossoverUniformRate = Double.parseDouble(props.getProperty("CROSSOVER_UNIFORM_RATE"));

                        if (crossoverUniformRate >= 0.00 && crossoverUniformRate <= 1.00) {
                            this.crossoverUniformRate = crossoverUniformRate;
                        } else {
                            this.crossoverType = "ONE_POINT";
                        }
                        break;
                }
            } catch (NullPointerException | NumberFormatException n) {
                this.crossoverType = "ONE_POINT";
                System.out.println("A problem occurred getting crossover type values -> Switched to default values.");
            }

            try {
                double crossoverFrequency = Double.parseDouble(props.getProperty("CROSSOVER_FREQUENCY"));
                double mutationFrequency = Double.parseDouble(props.getProperty("MUTATION_FREQUENCY"));

                if (crossoverFrequency >= 0.00 && crossoverFrequency <= 1.00) {
                    this.crossoverFrequency = crossoverFrequency;
                }

                if (mutationFrequency >= 0.00 && mutationFrequency <= 1.00) {
                    this.mutationFrequency = mutationFrequency;
                }
            } catch (NullPointerException | NumberFormatException n) {
                System.out.println("A problem occurred getting frequency values -> Switched to default values.");
            }

            try {
                boolean fitnessProportionate = Boolean.parseBoolean(props.getProperty("FITNESS_PROPORTIONATE_SELECTION"));
                boolean stochastic = Boolean.parseBoolean(props.getProperty("STOCHASTIC_SELECTION"));
                boolean tournament = Boolean.parseBoolean(props.getProperty("TOURNAMENT_SELECTION"));
                boolean truncation = Boolean.parseBoolean(props.getProperty("TRUNCATION_SELECTION"));
                boolean elitism = Boolean.parseBoolean(props.getProperty("ELITISM_SELECTION"));

                // hardcoded
                if ((fitnessProportionate && !stochastic && !tournament && !truncation) ||
                        (!fitnessProportionate && stochastic && !tournament && !truncation) ||
                        (!fitnessProportionate && !stochastic && tournament && !truncation) ||
                        (!fitnessProportionate && !stochastic && !tournament && truncation)) {

                    this.fitnessProportionate = fitnessProportionate;
                    this.stochastic = stochastic;
                    this.tournament = tournament;
                    this.truncation = truncation;
                } else {
                    throw new Exception();
                }

                if (elitism) {
                    int top = Integer.parseInt(props.getProperty("TOP"));

                    if (top >= 1 && top <= (populationSize / 2)) {
                        this.top = top;
                    } else {
                        throw new Exception();
                    }
                }
            } catch (Exception n) {
                this.populationSize = 100;
                this.fitnessProportionate = true;
                this.stochastic = false;
                this.tournament = false;
                this.truncation = false;
                this.elitism = true;
                this.top = 5;
                System.err.println("Multiple Selections -> Switched to default values");
            }

            // setting up the first population
            this.currGen = populateFistGen();
            this.numGen = 1;
            this.bestFit = null;

            // setting file dumper
            this.fileName = String.valueOf(System.currentTimeMillis());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Method which returns an instance of GeneticAlgorithm class, attending to the Singleton Design Pattern
     *
     * @return the instance of GeneticAlgorithm class
     */
    public static GeneticAlgorithm getGeneticAlgorithm() {
        GeneticAlgorithm result = instance;

        if (result == null) {
            synchronized (GeneticAlgorithm.class) {
                result = instance;
                if (result == null) {
                    instance = new GeneticAlgorithm();
                }
            }
        }
        return instance;
    }

    /**
     * A getter method that returns the current generation
     *
     * @return the current generation
     */
    public Generation getCurrGen() {
        return currGen;
    }

    /**
     * A method used to create the first generation
     *
     * @return the first generation
     */
    private Generation populateFistGen() {
        Generation firstGen = new Generation(this.populationSize);
        ArrayList<Chromosome> listChromos = new ArrayList<>(populationSize);

        // create an array representing button frequencies
        int[] buttons = new int[100];

        int pos = 0;
        for (int it = 0; it < buttonFrequencies.length; it++) {
            for (int freq = 0; freq < buttonFrequencies[it]; freq++) {
                buttons[pos] = it;
                pos++;
            }
        }

        for (int chromoNum = 0; chromoNum < this.populationSize; chromoNum++) {
            Chromosome newChromossome = new Chromosome();
            ArrayList<Integer> inputs = new ArrayList<>();

            if(chromoNum != 0) {
                // randomize 100-200 inputs
                for (int j = 0; j < (new Random().nextInt(100)) + 101; j++) {

                    // randomize inputs
                    int button = this.space[buttons[new Random().nextInt(buttons.length)]];

                    // making for button pressure
                    for (int k = 0; k < (new Random().nextInt(5)) + 6; k++) {
                        inputs.add(button);
                    }
                }
            }else{
                int[] inp = new int[]{1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 10, 10, 10, 10, 10, 10, 10, 10, 10, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 10, 10, 10, 10, 10, 10, 10, 10, 4, 4, 4, 4, 4, 4, 2, 2, 2, 2, 2, 2, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 4, 4, 4, 4, 4, 4, 4, 4, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 9, 9, 9, 9, 9, 9, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 10, 10, 10, 10, 10, 10, 10, 4, 4, 4, 4, 4, 4, 10, 10, 10, 10, 10, 10, 10, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 10, 10, 10, 10, 10, 10, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 10, 10, 10, 10, 10, 10, 10, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 3, 3, 3, 3, 3, 3, 3, 3, 3, 10, 10, 10, 10, 10, 10, 10, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 10, 10, 10, 10, 10, 10, 10, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4, 4, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 10, 10, 10, 10, 10, 10, 10, 10, 10, 8, 8, 8, 8, 8, 8, 8, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 10, 10, 10, 10, 10, 10, 10, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 8, 8, 8, 8, 8, 8, 8, 8, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 10, 10, 10, 10, 10, 10, 10, 4, 4, 4, 4, 4, 4, 4, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 10, 10, 10, 10, 10, 10, 10, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 3, 3, 3, 3, 3, 3, 3, 10, 10, 10, 10, 10, 10, 10, 10, 10, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 10, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 10, 10, 10, 10, 10, 10, 1, 1};
                for (int i : inp) {
                    inputs.add(i);
                }
            }

            newChromossome.setInputs(inputs);
            listChromos.add(newChromossome);
        }

        firstGen.setGeneration(listChromos);
        return firstGen;
    }

    /**
     * A private method that creates and dumps a Generation to a file with the <i>Generation</i> basic information
     * and complete info to the <i>logs</i> directory and another file to the <i>imports</i> directory also with basic
     * information and information of the <i>Generation</i> last iteration.
     *
     * @param x        the generation to be stored.
     * @param fileName the file name in which to store the generation.
     */
    private void dumpGenToFile(Generation x, String fileName) throws IOException {
        if (worldStageValidate()) {
            String userDirLogs = System.getProperty("user.dir") + "\\logs\\";
            String userDirImps = System.getProperty("user.dir") + "\\imports\\";

            File fileLog, fileImp, logDir, impDir;

            pI = new PropertiesInfo();
            int worldInt = pI.getWorld();
            int stageInt = pI.getStage();

            String ws = "\\World_" + worldInt + "_Stage_" + stageInt + "\\";
            logDir = new File(userDirLogs + ws);
            impDir = new File(userDirImps + ws);

            // verifies if the directory exists and if not, creates it
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            if (!impDir.exists()) {
                impDir.mkdirs();
            }

            // verifies the filename format to ensure that is created a .txt
            if (fileName.endsWith(".txt")) {
                fileLog = new File(logDir + "\\" + fileName);
                fileImp = new File(impDir + "\\" + fileName);
            } else {
                fileLog = new File(logDir + "\\" + fileName + ".txt");
                fileImp = new File(impDir + "\\" + fileName + ".txt");
            }

            // verifies if the files exist or if it wasn't able to create them
            if (!(fileLog.exists())) {
                if (!(fileLog.createNewFile())) {
                    throw new IOException("It wasn't possible create a Log file.");
                }
            }
            if (!(fileImp.exists())) {
                if (!(fileImp.createNewFile())) {
                    throw new IOException("It wasn't possible create an Import file.");
                }
            }

            FileWriter fw = new FileWriter(fileLog, true);
            BufferedWriter bw = new BufferedWriter(fw);

            FileWriter wI = new FileWriter(fileImp, true);
            BufferedWriter bI = new BufferedWriter(wI);

            // write the generation number and world (basic information)
            bw.write("Generation - " + this.numGen);
            bw.newLine();
            bw.write("World - " + this.level);
            bw.newLine();
            bw.newLine();

            bI.write("Generation - " + this.numGen);
            bI.newLine();
            bI.write("World - " + this.level);
            bI.newLine();
            bI.newLine();

            ArrayList<Chromosome> generation = x.getGeneration();

            int i = 1;
            for (Chromosome tmp : generation) {
                bw.write("CHROMOSOME: " + i);
                bw.newLine();
                bw.write("SOLUTION: " + tmp.getInputs().toString());
                bw.newLine();
                bw.write("COINS: " + tmp.getResults().getCoins());
                bw.newLine();
                bw.write("TIME_LEFT: " + tmp.getResults().getTime_left());
                bw.newLine();
                bw.write("X_POS: " + tmp.getResults().getX_pos());
                bw.newLine();
                bw.write("SPEED: " + ((double) (tmp.getResults().getX_pos())) / (((double) (levelTimeMapping[this.world - 1][this.lvl - 1])) -
                        ((double) (tmp.getResults().getTime_left()))));
                bw.newLine();
                bw.write("SCORE: " + tmp.getResults().getScore());
                bw.newLine();
                bw.write("FLAG: " + tmp.getResults().getFlag_get());
                bw.newLine();
                bw.write("REASON_FINISH: " + tmp.getResults().getReason_finish());
                bw.newLine();
                bw.write("STATUS: " + tmp.getResults().getStatus());
                bw.newLine();
                bw.write("COMMANDS_USED: " + tmp.getResults().getCommands_used());
                bw.newLine();
                bw.write("FITNESS VALUE: " + tmp.getFitnessValue());
                bw.newLine();
                bw.newLine();

                // Writes the import file in the import directory
                bI.write("CHROMOSOME: " + i);
                bI.newLine();
                bI.write("SOLUTION: " + tmp.getInputs().toString());
                bI.newLine();
                bI.write("FITNESS VALUE: " + tmp.getFitnessValue());
                bI.newLine();
                bI.newLine();

                i++;
            }

            // write the average time, coins, score and fitness score for the generation (basic information)
            bw.write("Average Generation Time: " + this.currGen.calculateGenAverageTime());
            bw.newLine();
            bw.write("Average Generation Speed: " + this.currGen.calculateGenAverageSpeed(this.world, this.lvl));
            bw.newLine();
            bw.write("Average Generation Pos: " + this.currGen.calculateGenAveragePos());
            bw.newLine();
            bw.write("Average Generation Coins: " + this.currGen.calculateGenAverageCoins());
            bw.newLine();
            bw.write("Average Generation Score: " + this.currGen.calculateGenAverageScore());
            bw.newLine();
            bw.write("Average Generation Fitness Score: " + this.currGen.calculateGenAverageFitnessScore());
            bw.newLine();

            bI.close();
            bw.close();
        } else {
            throw new IOException("Error! File may not exist or incorrect data may have been entered please check.");
        }
    }

    /**
     * Determines if the <i>world</i> and <i>stage</i> in the <i>properties</i> files are acceptable.
     *
     * @return true if is acceptable or false if not.
     */
    public boolean worldStageValidate() {
        boolean valid = false;

        pI = new PropertiesInfo();

        int worldInt = pI.getWorld();
        int stageInt = pI.getStage();

        if ((worldInt >= 1 && worldInt <= 8) && (stageInt >= 1 && stageInt <= 4)) {
            valid = true;
        }

        return valid;
    }

    /**
     * A getter method that returns the number of generations
     *
     * @return an integer representing the number of generations
     */
    public int getNumGen() {
        return this.numGen;
    }

    /**
     * A getter method that returns whether render is true ot false
     *
     * @return a boolean representing the render option
     */
    public boolean isRender() {
        return this.render;
    }

    /**
     * A getter method that returns the level
     *
     * @return a string representing the level
     */
    public String getLevel() {
        return this.level;
    }

    /**
     * A method which calculates a generation fitness value based on class variables
     */
    public void calculateFitness() {
        Generation generation = this.currGen;

        double coins = generation.calculateGenAverageCoins();
        double speedrun = generation.calculateGenAverageSpeed(this.world, this.lvl);
        double score = generation.calculateGenAverageScore();
        double x_pos = generation.calculateGenAveragePos();

        // 60% x_pos && 40% rest
        double posWeight = (this.coinWeight + this.speedrunWeight + this.scoreWeight) * 1.5;

        double total = (coins * this.coinWeight) + (speedrun * this.speedrunWeight) + (score * this.scoreWeight)
                + (x_pos * posWeight);

        coins = coins == 0.0 ? 0.001 : coins;
        speedrun = speedrun == 0.0 ? 0.001 : speedrun;
        score = score == 0.0 ? 0.001 : score;

        // calculate factors
        double coinFactor = total / coins;
        double speedrunFactor = total / speedrun;
        double scoreFactor = total / score;
        double x_posFactor = total / x_pos;

        ArrayList<Chromosome> chromosomes = generation.getGeneration();

        for (Chromosome tmp : chromosomes) {
            RunResult results = tmp.getResults();
            double chromoCoins = results.getCoins();
            double chromoSpeedrun = ((double) (results.getX_pos())) / (((double) (levelTimeMapping[this.world - 1][this.lvl - 1])) -
                    ((double) (results.getTime_left())));
            double chromoScore = results.getScore();
            double chromoPos = results.getX_pos();

            int gotFlag = Boolean.parseBoolean(results.getFlag_get()) ? 1 : 0;

            // getFlag 2x time multiplier
            double fitnessValue = ((chromoCoins * coinFactor * this.coinWeight) +
                    (chromoScore * scoreFactor * this.scoreWeight) +
                    (chromoSpeedrun * speedrunFactor * this.speedrunWeight) +
                    (chromoPos * x_posFactor * posWeight));

            fitnessValue += fitnessValue * (gotFlag * 2);

            if (results.getReason_finish().equals("win")){
               List<Integer> newList = tmp.getInputs().subList(0, results.getCommands_used());
               tmp.setInputs(new ArrayList<>(newList));
            }

            if (results.getReason_finish().equals("no_more_commands")){
                addMoreCommands(tmp);
            }

            if (results.getReason_finish().equals("death")) {
                List<Integer> newList = tmp.getInputs().subList(0, results.getCommands_used());
                tmp.setInputs(new ArrayList<>(newList));
            }

            if (results.getReason_finish().equals("win")) {
                ArrayList<Integer> newList = (ArrayList<Integer>) tmp.getInputs().subList(0, results.getCommands_used());
                tmp.setInputs(newList);
            }

            if (this.bestFit == null) {
                this.bestFit = tmp;
            } else {
                if (this.bestFit.getFitnessValue() < fitnessValue) {
                    this.bestFit = tmp;
                }
            }

            if (Double.isNaN(fitnessValue)) {
                tmp.setFitnessValue(0);
            } else {
                tmp.setFitnessValue(fitnessValue);
            }
        }
    }

    /**
     * A functions that breeds a new Generation
     */
    public void breedNewGen() {
        try {
            dumpGenToFile(this.currGen, this.fileName);
        } catch (IOException i) {
            System.err.println("Something went wrong storing the current generation to a file.");
        }

        Generation newGen = new Generation(this.populationSize);
        ArrayList<Chromosome> generation = new ArrayList<>(this.populationSize);

        if (this.fitnessProportionate) {
            if (this.elitism) {
                try {
                    @SuppressWarnings("unchecked")
                    ArrayList<Chromosome> gen = (ArrayList<Chromosome>) this.currGen.getGeneration().clone();

                    Collections.sort(gen);
                    Collections.reverse(gen);

                    for (int i = 0; i < this.top; i++) {
                        Chromosome newChromo = gen.get(i);
                        generation.add(newChromo);
                    }
                } catch (ClassCastException c) {
                    System.exit(0);
                    System.err.println("Something went wrong on the elitism method.");
                }
            }

            ArrayList<Pair<Double, Chromosome>> matingPool = fitnessProportionateSelection();

            int chromoNum = 0;
            for (int left = this.populationSize - generation.size(); left > 0; ) {
                int pos = new Random().nextInt(this.populationSize);
                Chromosome parent1 = this.currGen.getGeneration().get(pos);

                // choose second parent
                Chromosome parent2 = null;

                while (parent2 == null) {
                    double rand = Math.random();

                    for (Pair<Double, Chromosome> pair : matingPool) {
                        if (pair.getKey() >= rand) {
                            if (!pair.getValue().isClone(parent1)) {
                                parent2 = pair.getValue();
                            }
                            break;
                        }
                    }
                }

                if (left == 1) {
                    Chromosome[] children = crossover(parent1.clone(), parent2.clone());

                    if (children[0].isClone(parent1) && children[1].isClone(parent2)) {
                        continue;
                    }

                    Chromosome child1 = mutate(children[0]);

                    // add first child to new generation
                    generation.add(child1);
                } else {
                    Chromosome[] children = crossover(parent1.clone(), parent2.clone());

                    if (children[0].isClone(parent1) && children[1].isClone(parent2)) {
                        continue;
                    }

                    Chromosome child1 = mutate(children[0]);
                    Chromosome child2 = mutate(children[1]);

                    // add both children to new generation
                    generation.add(child1);
                    generation.add(child2);
                }
                left = this.populationSize - generation.size();
                chromoNum++;
            }

            // set the currGen
            newGen.setGeneration(generation);
            this.currGen = newGen;
            this.numGen++;

            return;
        }

        if (this.stochastic) {
            // TODO: This selection method.
        }

        if (this.tournament) {
            // TODO: This selection method.
        }

        if (this.truncation) {
            // TODO: This selection method.
        }
    }

    /**
     * This method imports the data from a file located in the <i>imports</i> directory to continue it's processing.
     *
     * @param fileName name of the file to be imported.
     * @throws IOException throws an exception if the file does not exist.
     */
    public void readGenFromFile(String fileName) throws IOException {
        try {
            if (worldStageValidate()) {
                Generation newGen = new Generation(this.populationSize);
                ArrayList<Chromosome> arrChromo = new ArrayList<>();

                pI = new PropertiesInfo();
                String importFolder = System.getProperty("user.dir") + "\\imports\\";

                File fileImp, file;

                int worldInt = pI.getWorld();
                int stageInt = pI.getStage();

                String ws = "\\World_" + worldInt + "_Stage_" + stageInt + "\\";

                fileImp = new File(importFolder + ws);

                if (fileName.endsWith(".txt")) {
                    file = new File(fileImp + "\\" + fileName);
                } else {
                    file = new File(fileImp + "\\" + fileName + ".txt");
                }

                if (!(file.exists())) {
                    throw new IOException("File does not exist.\nPlease check the spelling and confirm if the file is present in the (imports) folder.");
                }

                BufferedReader bufRead = Files.newBufferedReader(Paths.get(file.getPath()));

                String line;

                while ((line = bufRead.readLine()) != null) {
                    Chromosome newChro = new Chromosome();

                    String[] cms_sol = line.split(": ");

                    // Gets the last number of the chromosome from de file
                    if (cms_sol[0].equals("CHROMOSOME")) {
                        this.chromosome = Integer.parseInt(cms_sol[1]);
                    }

                    // Gets the last solution of the chromosome from de file
                    ArrayList<Integer> solut = new ArrayList<>();
                    if (cms_sol[0].equals("SOLUTION")) {

                        // first thing removes "[" and "]"
                        String values = cms_sol[1].replace("[", "");
                        values = values.replace("]", "");

                        // trimming and split
                        values = values.trim();

                        String[] arr = values.split(",");

                        for (String s : arr) {
                            solut.add(Integer.parseInt(s.trim()));
                        }

                        newChro.setInputs(solut);
                        arrChromo.add(newChro);
                    }
                }
                newGen.setGeneration(arrChromo);
                this.currGen = newGen;
                bufRead.close();
            }
        } catch (IOException ie){
            System.out.println("Error! File may not exist or incorrect data may have been entered please check.  DUMP");
        }
    }

    /**
     * A method which prints the selected weights
     *
     * @return a string with all the weights values
     */
    public String printWeights() {
        return "SCORE_WEIGHT: " + this.scoreWeight + "\nCOIN_WEIGHT: " + this.coinWeight
                + "\nSPEEDRUN_WEIGHT: " + this.speedrunWeight;
    }

    /**
     * A method to get the population size
     *
     * @return the population size
     */
    public int getPopulationSize() {
        return this.populationSize;
    }

    public Chromosome getBestFit() {
        return bestFit;
    }

    /**
     * This method does the fitness proportionate selection on the current selection
     *
     * @return the mating pool
     */
    private ArrayList<Pair<Double, Chromosome>> fitnessProportionateSelection() {
        // get current generation
        ArrayList<Chromosome> gen = this.currGen.getGeneration();

        // define the mating pool size and total fitness
        double totalFitness = 0.0;
        ArrayList<Pair<Double, Chromosome>> roulette = new ArrayList<>();

        // get the total fitness
        for (Chromosome tmp : gen) {
            double fitnessValue = tmp.getFitnessValue();
            totalFitness += fitnessValue;
        }

        // create roulette
        double currValue = 0.0;
        for (Chromosome chromo : gen) {
            double n = (chromo.getFitnessValue() / totalFitness);

            currValue += n;
            Pair<Double, Chromosome> tmp = new Pair<>(currValue, chromo.clone());
            roulette.add(tmp);
        }

        return roulette;
    }

    /**
     * This method performs the crossover function
     *
     * @param parent1 one of the parent chromosomes
     * @param parent2 the other parent chromosome
     * @return an array with two elements, the children of the parent chromosomes
     */
    private Chromosome[] crossover(Chromosome parent1, Chromosome parent2) {
        try {
            Chromosome[] children = new Chromosome[2];

            if (Math.random() < this.crossoverFrequency) {

                ArrayList<Integer> parent1Inputs = parent1.getInputs();
                ArrayList<Integer> parent2Inputs = parent2.getInputs();

                //separate array by blocks
                ArrayList<ArrayList<Integer>> blockedArray1 = separateArrayByBlocks(parent1Inputs);
                ArrayList<ArrayList<Integer>> blockedArray2 = separateArrayByBlocks(parent2Inputs);

                switch (this.crossoverType) {
                    case "ONE_POINT":
                        double onePointRate = this.crossoverOnePointRate;

                        // define crossover position
                        int pos1 = (int) Math.round(blockedArray1.size() * onePointRate);
                        int pos2 = (int) Math.round(blockedArray2.size() * onePointRate);

                        pos1 = pos1 == 0 ? 1 : pos1;
                        pos2 = pos2 == 0 ? 1 : pos2;

                        // separate lists
                        ArrayList<ArrayList<Integer>>[] separatedLists1 = separateLists(blockedArray1, pos1);
                        ArrayList<ArrayList<Integer>>[] separatedLists2 = separateLists(blockedArray2, pos2);

                        assert separatedLists1 != null;
                        assert separatedLists2 != null;
                        ArrayList<ArrayList<Integer>> list1 = separatedLists1[1];
                        ArrayList<ArrayList<Integer>> list2 = separatedLists2[1];

                        // remove values after crossover position
                        blockedArray1.subList(pos1 - 1, blockedArray1.size()).clear();
                        blockedArray2.subList(pos2 - 1, blockedArray2.size()).clear();

                        blockedArray1.addAll(list1);
                        blockedArray2.addAll(list2);

                        children[0] = new Chromosome();
                        children[1] = new Chromosome();

                        children[0].setInputs(blockedArrayToArrayList(blockedArray1));
                        children[1].setInputs(blockedArrayToArrayList(blockedArray2));

                        return children;
                    case "MULTI_POINT":
                        // TODO:
                        return null;
                    case "UNIFORM":
                        double uniformRate = this.crossoverUniformRate;

                        if (blockedArray1.size() >= blockedArray2.size()) {
                            for (int i = 0; i < blockedArray2.size(); i++) {
                                double rand = Math.random();

                                if (rand < uniformRate) {
                                    if (i >= blockedArray2.size()) {
                                        blockedArray2.add(blockedArray1.get(i));
                                    } else {
                                        // switch them
                                        ArrayList<Integer> tmp;
                                        tmp = blockedArray1.get(i);
                                        blockedArray1.set(i, blockedArray2.get(i));
                                        blockedArray2.set(i, tmp);
                                    }
                                }
                            }
                        } else {
                            for (int i = 0; i < blockedArray2.size(); i++) {
                                double rand = Math.random();

                                if (rand < uniformRate) {
                                    if (i >= blockedArray1.size()) {
                                        blockedArray1.add(blockedArray2.get(i));
                                    } else {
                                        // switch them
                                        ArrayList<Integer> tmp;
                                        tmp = blockedArray1.get(i);
                                        blockedArray1.set(i, blockedArray2.get(i));
                                        blockedArray2.set(i, tmp);
                                    }
                                }
                            }
                        }

                        children[0] = new Chromosome();
                        children[1] = new Chromosome();

                        children[0].setInputs(blockedArrayToArrayList(blockedArray1));
                        children[1].setInputs(blockedArrayToArrayList(blockedArray2));

                        return children;
                    default:
                        throw new Exception();
                }
            } else {
                children[0] = parent1;
                children[1] = parent2;
                return children;
            }
        } catch (Exception E) {
            System.out.println(E);
            System.err.println("Something went wrong in the mutate function, turning off JVM");
            System.exit(0);
            return null;
        }
    }

    /**
     * This method performs the mutation function
     *
     * @param chromo the chromosome to mutate
     * @return the mutated chromosome
     */
    private Chromosome mutate(Chromosome chromo) {
        double rand = Math.random();

        if (rand < this.mutationFrequency) {
            ArrayList<ArrayList<Integer>> blocks = separateArrayByBlocks(chromo.getInputs());

            int blockPos = new Random().nextInt(blocks.size());
            ArrayList<Integer> newInput = new ArrayList<>();

            // create an array representing button frequencies
            int[] buttons = new int[100];

            int pos = 0;
            for (int it = 0; it < this.buttonFrequencies.length; it++) {
                for (int freq = 0; freq < this.buttonFrequencies[it]; freq++) {
                    buttons[pos] = it;
                    pos++;
                }
            }

            // randomize inputs
            int button = this.space[buttons[new Random().nextInt(buttons.length)]];

            // making for button pressure
            for (int k = 0; k < (new Random().nextInt(5)) + 6; k++) {
                newInput.add(button);
            }

            blocks.set(blockPos, newInput);

            Chromosome mutatedChromo = new Chromosome();
            mutatedChromo.setInputs(blockedArrayToArrayList(blocks));

            return mutatedChromo;
        } else {
            return chromo;
        }
    }

    /**
     * This method transforms an array of integer into array of integer blocks
     *
     * @param arr the Integer array
     * @return the array of integer blocks
     */
    public static ArrayList<ArrayList<Integer>> separateArrayByBlocks(ArrayList<Integer> arr) {
        ArrayList<ArrayList<Integer>> tmpArr = new ArrayList<>();
        int value = arr.get(0);

        for (int i = 0; i < arr.size(); ) {
            ArrayList<Integer> newBlock = new ArrayList<>();

            while (i < arr.size() && value == arr.get(i)) {
                newBlock.add(value);
                i++;
            }

            if (newBlock.size() > 10) {
                ArrayList<Integer> partialBlock1 = new ArrayList<>(
                        newBlock.subList(0, (int) Math.floor((double) (newBlock.size()) / 2)));

                ArrayList<Integer> partialBlock2 = new ArrayList<>(
                        newBlock.subList((int) Math.floor((double) (newBlock.size()) / 2), newBlock.size()));

                tmpArr.add(partialBlock1);
                tmpArr.add(partialBlock2);
            } else {
                tmpArr.add(newBlock);
            }

            // change new value
            if (i < arr.size()) {
                value = arr.get(i);
            }
        }
        return tmpArr;
    }


    /**
     * This method converts an array of arrays into an arraylist
     *
     * @param blockedArray the array of arrays
     * @return the arraylist
     */
    private ArrayList<Integer> blockedArrayToArrayList(ArrayList<ArrayList<Integer>> blockedArray) {
        ArrayList<Integer> returnArr = new ArrayList<>();

        for (ArrayList<Integer> tmp : blockedArray) {
            returnArr.addAll(tmp);
        }
        return returnArr;
    }

    /**
     * Adds more commands to the chromosome if the commands have all been executed but Mario still hasn't reached
     * the end and there is still time remaining,
     * @param tmp chromosome to add more commands.
     * @return the chromosome with the added commands.
     */
    private Chromosome addMoreCommands(Chromosome tmp){
        ArrayList<Integer> commands = tmp.getInputs();

        // create an array representing button frequencies
        int[] buttons = new int[100];

        int pos = 0;
        for (int it = 0; it < buttonFrequencies.length; it++) {
            for (int freq = 0; freq < buttonFrequencies[it]; freq++) {
                buttons[pos] = it;
                pos++;
            }
        }

        // randomize 100-200 inputs
        for (int j = 0; j < (new Random().nextInt(100)) + 101; j++) {

            // randomize inputs
            int button = this.space[buttons[new Random().nextInt(buttons.length)]];

            // making for button pressure
            for (int k = 0; k < (new Random().nextInt(5)) + 6; k++) {
                commands.add(button);
            }
        }

        tmp.setInputs(commands);
        return tmp;
    }

    /**
     * Separates a list in two in the position pretended.
     * @param list list that is to be separated.
     * @param sPos position of the list to be separated.
     * @return an arrayList of arrayList.
     */
    private ArrayList<ArrayList<Integer>>[] separateLists(ArrayList<ArrayList<Integer>> list, int sPos) {

        if (sPos < list.size()) {
            ArrayList<ArrayList<Integer>>[] returnArr = new ArrayList[2];

            ArrayList<ArrayList<Integer>> firstList = new ArrayList<>();
            ArrayList<ArrayList<Integer>> secondList = new ArrayList<>();
            int it = 0;

            while (it < list.size()) {
                if (it < sPos) {
                    firstList.add(list.get(it));
                } else {
                    secondList.add(list.get(it));
                }
                it++;
            }
            returnArr[0] = firstList;
            returnArr[1] = secondList;

            return returnArr;
        } else {
            return null;
        }
    }
}

enum CROSSOVER_TYPES {
    ONE_POINT("ONE_POINT"),
    MULTI_POINT("MULTI_POINT"),
    UNIFORM("UNIFORM");

    private String type;

    CROSSOVER_TYPES(String type) {
        this.setType(type);
    }

    /**
     * This method returns the <i>Crossover</i> enum type.
     *
     * @return the type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * This method sets the <i>Crossover</i> enum type.
     *
     * @param type, the new Crossover type enum.
     */
    public void setType(String type) {
        this.type = type;
    }
}