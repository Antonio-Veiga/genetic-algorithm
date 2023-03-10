package classes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesInfo {

    private int genValue;
    private String ip;
    private String impFile;
    private int populationSize;
    private int world;
    private int stage;

    public PropertiesInfo() {
        String propsPath = "/GeneticAlgorithm.properties";

        try (InputStream input = getClass().getResourceAsStream(propsPath)) {

            Properties props = new Properties();
            props.load(input);

            this.world = Integer.parseInt(props.getProperty("WORLD"));
            this.stage = Integer.parseInt(props.getProperty("LEVEL"));
            this.impFile = props.getProperty("IMPORT_FILE");
            this.ip = props.getProperty("IP");
            this.genValue = Integer.parseInt(props.getProperty("GENERATIONS"));
            this.populationSize = Integer.parseInt(props.getProperty("POPULATION_SIZE"));

        } catch (IOException ie) {
            System.out.println("Error! File may not exist or incorrect data may have been entered please check.");
        }
    }

    public int getGenValue() {
        return genValue;
    }

    public String getIp() {
        return ip;
    }

    public String getImpFile() {
        return impFile;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public int getWorld() {
        return world;
    }

    public int getStage() {
        return stage;
    }
}
