package pacman.controllers.examples;

import java.util.*;

import pacman.controllers.Controller;
import pacman.game.Game;

import static pacman.game.Constants.*;

/**
 * Pac-Man controller as part of the starter package - simply upload this file as a zip called
 * MyPacMan.zip and you will be entered into the rankings - as simple as that! Feel free to modify 
 * it or to start from scratch, using the classes supplied with the original software. Best of luck!
 * 
 * This controller utilises 3 tactics, in order of importance:
 * 1. Get away from any non-edible ghost that is in close proximity
 * 2. Go after the nearest edible ghost
 * 3. Go to the nearest pill/power pill
 */
public class MyPacMan extends Controller<MOVE>
{	
	private static int MIN_DISTANCE;	//if a ghost is this close, run away
    private static int MAX_PILL_DISTANCE;
    private PillCluster cluster;
    double[] moveScores;
	
	public MOVE getMove(Game game,long timeDue)
	{
	    //Strategy 1: if any non-edible ghost is too close (less than MIN_DISTANCE), run away
        moveScores = new double[5];
        MIN_DISTANCE = 5;
        MAX_PILL_DISTANCE = 15;
        int current=game.getPacmanCurrentNodeIndex();

		for(GHOST ghost : GHOST.values())
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
				if(game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<MIN_DISTANCE)
					return game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost),DM.MANHATTAN);

		//Strategy 2: find the nearest edible ghost and go after them
		int minDistance = Integer.MAX_VALUE;
		GHOST minGhost=null;		
		
		for(GHOST ghost : GHOST.values())
			if(game.getGhostEdibleTime(ghost) > 0)
			{
				int distance=game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
				
				if(distance<minDistance)
				{
					minDistance=distance;
					minGhost=ghost;
				}
			}
		
		if(minGhost!=null)	//we found an edible ghost
			return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(minGhost),DM.MANHATTAN);

		// strategy 3:  chase clustered pill
        if(cluster == null){
            cluster = new PillCluster(game);
        } else if(game.getPillIndex(current) != -1 || game.getPowerPillIndex(current) != -1){
            cluster.remove(game, current);
        }

        List<NodeDistance> closestClusters = cluster.findClosestNodeInCluster(game, current);

        for (NodeDistance node : closestClusters) {
            double score = node.size / node.distance;
            MOVE move = game.getNextMoveTowardsTarget(current, node.index, DM.MANHATTAN);
            moveScores[move.ordinal()] += score;
        }

        double max = Double.NEGATIVE_INFINITY;
        MOVE biggestCluster = null;

        int i = 0;
        for (double score : moveScores) {
            if (max < score) {
                max = score;
                biggestCluster = MOVE.values()[i];
            }
            i++;
        }

        return biggestCluster;
	}

	private static class Connected{
        private SortedSet<Integer> clusterIndices;

        public Connected(Collection<Integer> cluster){
            clusterIndices = new TreeSet<>((Comparator<? super Integer>) cluster);
        }

        public List<Connected> remove(Game game, int index) {
            if (!clusterIndices.remove(index)) {
                return null;
            } else {
                List<Connected> newClusters = new ArrayList<>();
                int[] neighbours = getNeighbouringPills(game, index);
                for (int neighbour : neighbours) {
                    SortedSet<Integer> newCluster = new TreeSet<>();
                    newCluster = createCluster(newCluster, game, neighbour);
                    if (!newCluster.isEmpty()) {
                        newClusters.add(new Connected(newCluster));
                    }
                }
                return newClusters;
            }
        }

        private SortedSet<Integer> createCluster(SortedSet<Integer> newCluster, Game game, int next) {
            if (clusterIndices.remove(next)) {
                newCluster.add(next);
                int[] neighbours = getNeighbouringPills(game, next);
                for (int neighbour : neighbours) {
                    createCluster(newCluster, game, neighbour);
                }
            }
            return newCluster;
        }

        private int[] getNeighbouringPills(Game game, int index) {
            int[] neighbours = game.getNeighbouringNodes(index);
            int n = 0;
            int pillNeighboursCount = 0;

            for(int neighbour: neighbours){
                MOVE move = game.getMoveToMakeToReachDirectNeighbour(index, neighbour);
                int i;
                for(i = 0; i < MAX_PILL_DISTANCE && neighbour != -1 && game.getPillIndex(neighbour) != -1 && game.getPowerPillIndex(neighbour) != -1; i++){
                    neighbour = game.getNeighbour(neighbour, move);
                }
                if (i == MAX_PILL_DISTANCE || neighbour == -1) {
                    neighbours[n++] = -1;
                } else {
                    pillNeighboursCount++;
                    neighbours[n++] = neighbour;
                }
            }

            n = 0;
            int[] pillNeighbours = new int[pillNeighboursCount];
            for (int neighbourIndex : neighbours) {
                if (neighbourIndex != -1) {
                    pillNeighbours[n++] = neighbourIndex;
                }
            }

            return pillNeighbours;
        }

        public NodeDistance findClosest(Game g, int index) {
            double min = Double.POSITIVE_INFINITY;
            int minIndex = -1;
            for(int i: clusterIndices){
                double distance = g.getDistance(index, i, DM.MANHATTAN);
                if(distance < min){
                    min = distance;
                    minIndex = i;
                }
            }
            if(minIndex != -1){
                return new NodeDistance(minIndex, min, clusterIndices.size());
            } else {
                System.out.println("cluster is empty");
            }
            return null;
        }
    }

    private static class PillCluster{
	    List<Connected> cluster;

	    public PillCluster(Game g){
	        List<Integer> active = getAllActivePills(g);
	        cluster = new LinkedList<>();
	        cluster.add(new Connected(active));

        }

        private List<Integer> getAllActivePills(Game g) {
	        int[] activePills = g.getActivePillsIndices();
	        int[] activePowerPills = g.getActivePowerPillsIndices();
	        List<Integer> targets = new ArrayList<>();

	        for (int a : activePills){
	            targets.add(a);
            }

            for (int ap : activePowerPills){
	            targets.add(ap);
            }

            return targets;
        }

        public List<Connected> getCluster() {
            return cluster;
        }

        public List<NodeDistance> findClosestNodeInCluster(Game g, int index){
	        List<NodeDistance> closestNodes = new ArrayList<>();
	        for(Connected c: cluster){
	            NodeDistance closest = c.findClosest(g, index);
	            closestNodes.add(closest);
            }
            return closestNodes;
        }

        public void remove(Game game, int current) {
            List<Connected> add = new LinkedList<Connected>();
            for(Connected a: add) {
                List<Connected> newClusters = a.remove(game,current);
                if (newClusters != null) {
                     add.addAll(newClusters);
                }
            }
            cluster.addAll(add);
        }
    }

    private static class NodeDistance{
        int index;
        double distance;
        int size;

        public NodeDistance(int i, double d, int s) {
            index = i;
            distance = d;
            size = s;
        }
    }
}

/*
    Init
    Cs = List clusters, initialize with a single
    cluster containing all pills
    Each time a pill P is eaten do:
    C = findClusterContainingP( Cs, P )
    Remove C from Cs
    For every possible neighbour Ni of P do:
    Create a new cluster Ci,
    Split C at Ni for Ci
    Add Ci to Cs
    End
    End
 */























