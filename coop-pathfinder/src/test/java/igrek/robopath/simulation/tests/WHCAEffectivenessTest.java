package igrek.robopath.simulation.tests;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import ch.qos.logback.classic.Level;
import igrek.robopath.mazegenerator.MazeGenerator;
import igrek.robopath.mazegenerator.RandomFactory;
import igrek.robopath.simulation.whca.Controller;
import igrek.robopath.simulation.whca.MobileRobot;
import igrek.robopath.simulation.whca.SimulationParams;


public class WHCAEffectivenessTest {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static Random random;
	
	@BeforeClass
	public static void beforeAll() {
		RandomFactory randomFactory = new RandomFactory();
		randomFactory.randomSeed = "";
		random = randomFactory.provideRandom();
		// please, shut up
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Controller.class)).setLevel(Level.INFO);
	}
	
	@Test
	public void testEffectiveness() {
		int SIMS_COUNT = 20;
		int mapW = 15, mapH = 15;
		int robotsCount = 5;
		int stepsMax = mapW * mapH;
		
		logger.info("Simulation params: map " + mapW + "x" + mapH + ", " + robotsCount + " robots, maxSteps=" + stepsMax);
		
		int successful = 0;
		for (int s = 0; s < SIMS_COUNT; s++) {
			if (runSimulation(mapW, mapH, robotsCount, stepsMax))
				successful++;
		}
		logger.info("successfull: " + successful + " / " + SIMS_COUNT);
		
	}
	
	private boolean runSimulation(int mapW, int mapH, int robotsCount, int stepsMax) {
		Controller controller = createRandomSimulation(mapW, mapH, robotsCount);
		int steps = simulate(controller, stepsMax);
		if (steps <= 0) {
			logger.info("failed to reach all targets");
			return false;
		} else {
			logger.info("all targets reached in " + steps + " steps");
			return true;
		}
	}
	
	
	private Controller createRandomSimulation(int mapW, int mapH, int robotsCount) {
		SimulationParams params = new SimulationParams();
		params.mapSizeW = mapW;
		params.mapSizeH = mapH;
		params.robotsCount = robotsCount;
		Controller controller = new Controller(null, params);
		controller.setRandom(random);
		controller.setMazegen(new MazeGenerator(random));
		
		controller.generateMaze();
		controller.placeRobots();
		params.timeDimension = controller.getRobots().size() + 1;
		controller.randomTargetPressed();
		return controller;
	}
	
	private int simulate(Controller controller, int stepsMax) {
		for (int step = 0; step < stepsMax; step++) {
			//			logger.debug("simulation step " + step);
			controller.stepSimulation();
			boolean allReached = true;
			for (MobileRobot robot : controller.getRobots()) {
				//				logger.debug("robot " + robot.toString() + ": " + robot.getPosition() + " -> " + robot
				//						.getTarget());
				if (!robot.hasReachedTarget())
					allReached = false;
			}
			if (allReached)
				return step + 1;
		}
		return -1;
	}
	
}