package pacman.controllers.examples;

import java.awt.Color;
import java.util.*;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Constants.DM;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

public class Cluster2 extends Controller<MOVE> {

    private PillClusterManager clusterManager;
    private MOVE[] lastTwoBestClusters = new MOVE[2];
    private int loopCounter = 0;
    private int stuckAt = Integer.MIN_VALUE;
    private int currentLevel = 0;
    private HashMap<NodeDistance,Double> nodesScores;

    public MOVE getMove(Game game, long timeDue) {
        int MIN_DISTANCE;
        nodesScores = new HashMap<>();

        //if on last life be more careful
        if(game.getPacmanNumberOfLivesRemaining() <= 1){
           MIN_DISTANCE = 10;
        } else {
            MIN_DISTANCE = 8;
        }

        double[] moveScores = new double[5];

        int current = game.getPacmanCurrentNodeIndex();

        //Strategy 1: if any non-edible ghost is too close (less than MIN_DISTANCE), run away
        for(Constants.GHOST ghost : Constants.GHOST.values()){
            if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0){
                if(game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost)) < MIN_DISTANCE){
                   // System.out.println("Ghost too close, ah!");
                    MOVE nextMove =  game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost), DM.EUCLID);
                    loopCounter++;
                    if(game.getPillIndex(current) != -1 || game.getPowerPillIndex(current) != -1) {
                        // System.out.println("taking pill out of cluster");
                        clusterManager.removeElement(game, current);
                    }
                    return nextMove;
                }
            }
        }

        //Strategy 2: find the nearest edible ghost and go after them
        int minDistance = Integer.MAX_VALUE;
        Constants.GHOST minGhost = null;

        for(Constants.GHOST ghost : Constants.GHOST.values()){
            if(game.getGhostEdibleTime(ghost) > 0) {
                int distance = game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
                if(distance < minDistance) {
                    minDistance=distance;
                    minGhost=ghost;
                }
            }
        }


        if(minGhost != null) {   //we found an edible ghost
            // dont chase far away ghosts
            if(minDistance < 120){
                MOVE nextMove = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(minGhost), DM.EUCLID);
                loopCounter++;
                if(game.getPillIndex(current) != -1 || game.getPowerPillIndex(current) != -1) {
                    clusterManager.removeElement(game, current);
                }
                return nextMove;
            }
        }

        //Strategy 3:  chase clustered pills
        if (clusterManager == null || game.getCurrentLevel() != currentLevel) {
            if(clusterManager != null){
                currentLevel++;
            }
           // System.out.println("No cluster already exists");
            clusterManager = new PillClusterManager(game);
        } else if(game.getPillIndex(current) != -1 || game.getPowerPillIndex(current) != -1) {
            //System.out.println("taking pill out of cluster");
            clusterManager.removeElement(game, current);
        }

        List<NodeDistance> closestClusters = clusterManager.findClosestNodeIndexPerCluster(game, current);
        System.out.println("The nearest cluster is  " + closestClusters.toString() + " it contains " + closestClusters.size() + " nodes");

        System.out.println("looping thru nodes");
        for (NodeDistance node : closestClusters) {
            double score = node.size / (15 * (node.distance + 1)); // how heavily to penalise far away clusters
            //MOVE move = game.getNextMoveTowardsTarget(current, node.index, DM.EUCLID);
            System.out.println("Node " + node.toString() + " scores " + score + " has size: " + node.size + " and is " + node.distance + " far away");
            nodesScores.put(node, score);
            //moveScores[move.ordinal()] += score;
        }

//        double max = 0;
//        MOVE biggestClusterNode = null;
//        int i = 0;
//        for (double s : moveScores) {
//            System.out.println("Scores stored in moveScores: " + s);
//            if (max < s) {
//                max = s;
//                biggestClusterNode = MOVE.values()[i];
//            }
//            i++;
//        }
//
         nodesScores.forEach((n,s) -> System.out.println("Node: " + n + " score: " + s) );
//
//        if(lastTwoBestClusters[0] == null){
//            lastTwoBestClusters[0] = biggestClusterNode;
//        } else if(lastTwoBestClusters[0] != null && lastTwoBestClusters == null){
//            lastTwoBestClusters[1] = biggestClusterNode;
//        } else {
//            lastTwoBestClusters[0] = lastTwoBestClusters[1];
//            lastTwoBestClusters[1] = biggestClusterNode;
//        }

//        if(loopCounter > 1) {
//            MOVE choosen = lastTwoBestClusters[0];
//            System.out.println("checkpoint");
//            //System.out.println(lastTwoBestClusters);
//            if(lastTwoBestClusters[0].ordinal() == biggestClusterNode.ordinal() && lastTwoBestClusters[1].ordinal() != biggestClusterNode.ordinal()){
//                System.out.println("in FLUX");
//                biggestClusterNode = lastTwoBestClusters[0];
//                choosen = biggestClusterNode;
//                lastTwoBestClusters[1] = choosen;
//                stuckAt = loopCounter;
//            }
//
//            if(loopCounter - stuckAt < 3){
//                System.out.println("overiding choices cause we were recently stuck");
//                loopCounter++;
//                lastTwoBestClusters[0] = lastTwoBestClusters[1];
//                lastTwoBestClusters[1] = lastTwoBestClusters[0];
//                System.out.println("STUCK: Biggest score is " + max + " so go " + choosen.toString());
//                System.out.println("STUCK: At loop " + loopCounter + " last biggest cluster is: " + lastTwoBestClusters[1] + " cluster before last is " + lastTwoBestClusters[0] + " and current cluster is " + biggestClusterNode);
//                return choosen;
//            }
//        }

        // DEBUG draw clusters
        clusterManager.drawClusters(game);

//        System.out.println("Biggest score is " + max + " so go " + biggestClusterNode.toString());
//        System.out.println("At loop " + loopCounter + " last biggest cluster is: " + lastTwoBestClusters[1] + " cluster before last is " + lastTwoBestClusters[0] + " and current cluster is " + biggestClusterNode);

        loopCounter++;

        NodeDistance best = nodesScores.entrySet().stream().max((n, s) -> n.getValue() > s.getValue() ? 1 : -1).get().getKey();
        return game.getNextMoveTowardsTarget(current, best.index, DM.EUCLID);
    }

    private class NodeDistance {

        private int index;
        private double distance;
        private int size;

        public NodeDistance(int index, double distance, int size) {
            this.index = index;
            this.distance = distance;
            this.size = size;
        }
    }

    private class ConnectedComponents {

        private final SortedSet<Integer> clusterElements;
        private int clusterSize;

        public ConnectedComponents(Collection<Integer> pillsInCluster) {
            clusterElements = new TreeSet<>(pillsInCluster);
            clusterSize = clusterElements.size();
        }


        public List<ConnectedComponents> removeElement(Game game, int elementIndex) {
            boolean removed = clusterElements.remove(elementIndex);
            if (!removed) {
                return null;
            } else {
                List<ConnectedComponents> newClusters = new ArrayList<>();
                int[] neighbours = getNeighbouringPills(game, elementIndex);
                for (int neighbour : neighbours) {
                    SortedSet<Integer> newCluster = new TreeSet<>();
                    newCluster = createCluster(newCluster, game, neighbour);
                    if (!newCluster.isEmpty()) {
                        newClusters.add(new ConnectedComponents(newCluster));
                    }
                }
                return newClusters;
            }
        }

        private SortedSet<Integer> createCluster(SortedSet<Integer> newCluster, Game game, int elementIndex) {
            if (clusterElements.remove(elementIndex)) {
                newCluster.add(elementIndex);
                int[] neighbours = getNeighbouringPills(game, elementIndex);
                for (int neighbour : neighbours) {
                    createCluster(newCluster, game, neighbour);
                }
            }
            return newCluster;
        }

        private int[] getNeighbouringPills(Game game, int nodeIndex) {
            int[] neighbours = game.getNeighbouringNodes(nodeIndex);

            int i = 0;
            int nbPillNeighbours = 0;
            for (int neighbourIndex : neighbours) {
                MOVE direction = game.getMoveToMakeToReachDirectNeighbour(nodeIndex, neighbourIndex);
                int count = 0;
                int MAX_PILL_DISTANCE = 5;

                while (count < MAX_PILL_DISTANCE && neighbourIndex != -1 && game.getPillIndex(neighbourIndex) == -1 && game.getPowerPillIndex(neighbourIndex) == -1) {
                    count++;
                    neighbourIndex = game.getNeighbour(neighbourIndex, direction);
                }

                if (count == MAX_PILL_DISTANCE || neighbourIndex == -1) {
                    neighbours[i++] = -1;
                } else {
                    nbPillNeighbours++;
                    neighbours[i++] = neighbourIndex;
                }
            }

            i = 0;
            int[] pillNeighbours = new int[nbPillNeighbours];
            for (int neighbour : neighbours) {
                if (neighbour != -1) {
                    pillNeighbours[i++] = neighbour;
                }
            }

            return pillNeighbours;
        }

        public NodeDistance findClosestNodeIndex(Game game, int indexNodeOrigin) {
            double minDistance = Double.POSITIVE_INFINITY;
            int minNodeIndex = -1;
            for (Integer element : clusterElements) {
                double distance = game.getDistance(indexNodeOrigin, element, DM.EUCLID);
                if (distance < minDistance) {
                    minDistance = distance;
                    minNodeIndex = element;
                }
            }
            if (minNodeIndex != -1) {
                return new NodeDistance(minNodeIndex, minDistance, clusterElements.size());
            } else {
                System.out.println("cluster is empty");
                throw new RuntimeException("no elements in cluster");
            }
        }

        public void draw(Game game, Color color) {
            int[] ints = new int[clusterElements.size()];
            int i = 0;
            for (int value : clusterElements) {
                ints[i++] = value;
            }
            GameView.addPoints(game, color, ints);
        }

        public int size() {
            return clusterSize;
        }
    }

    private class PillClusterManager {

        private final List<ConnectedComponents> clusters;

        public PillClusterManager(Game game) {
            List<Integer> allActivePills = getAllActivePills(game);
            clusters = new LinkedList<>();
            clusters.add(new ConnectedComponents(allActivePills));
        }

        public void drawClusters(Game game) {
            int colorId = 0;
            for (ConnectedComponents cluster : clusters) {
                cluster.draw(game, Color.getHSBColor(
                        (float) colorId / clusters.size(), 1, 1));
                colorId++;
            }
        }

        private List<Integer> getAllActivePills(Game game) {

            int[] activePills = game.getActivePillsIndices();
            int[] activePowerPills = game.getActivePowerPillsIndices();

            List<Integer> targets = new ArrayList<>();

            for (int pill: activePills)
                targets.add(pill);

            for (int pill : activePowerPills)
                targets.add(pill);

            return targets;
        }

        public void removeElement(Game game, int elementIndex) {
            List<ConnectedComponents> clustersToAdd = new LinkedList<>();
            Iterator<ConnectedComponents> it = clusters.iterator();
            while (it.hasNext()) {
                ConnectedComponents cluster = it.next();
                List<ConnectedComponents> newClusters = cluster.removeElement(game, elementIndex);
                if (newClusters != null) {
                    it.remove();
                    clustersToAdd.addAll(newClusters);
                }
            }
            clusters.addAll(clustersToAdd);
        }

        public List<NodeDistance> findClosestNodeIndexPerCluster(Game game, int indexNodeOrigin) {
            List<NodeDistance> closestNodes = new ArrayList<>();
            for (ConnectedComponents cluster : clusters) {
                NodeDistance closestNode = cluster.findClosestNodeIndex(game, indexNodeOrigin);
                closestNodes.add(closestNode);
            }
            return closestNodes;
        }
    }
}