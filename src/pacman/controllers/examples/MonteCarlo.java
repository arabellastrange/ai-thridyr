package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.*;

public class MonteCarlo extends Controller<Constants.MOVE>{
    private Constants.MOVE[] allMoves = Constants.MOVE.values();
    private int maxIterations = 20;
    private double exploration = (1/Math.sqrt(2));
    private int i = 0;


    @Override
    public Constants.MOVE getMove(Game game, long timeDue) {
        Tree tree = new Tree(game);
        Node root = tree.getRoot();

        while (i < maxIterations){
            //select
            Node best = selectBest(root);
            //expand

            expand(best);

            //rollout

            //backprog
            i++;
        }

        return null;
    }

    public Node selectBest(Node root){
        int parentVisited = 0;
                //root.getState().get times visited
        int score = root.getState().getScore();
        return Collections.max(root.getChildren(), Comparator.comparing(c -> (score/ c.getVisited()) + exploration * Math.sqrt(Math.log(parentVisited) / c.getVisited())));
    }

    public void expand(Node node){
        Constants.MOVE[] allPossibleMoves = node.getState().getPossibleMoves(node.getState().getPacmanCurrentNodeIndex());
        for(Constants.MOVE m : allPossibleMoves){
            //
        }
    }
}

class Node {
    Node parent;
    HashSet<Node> children;
    Game st;
    int visited;

    public Node(Game state){
        st = state;
    }

    public Node(Game state, Node parent, HashSet<Node> children) {
        st = state;
        this.parent = parent;
        this.children = children;
        visited = 1;
    }

    public int getVisited(){
        return visited;
    }

    public void incVisited(){
        visited++;
    }

    public Game getState() {
        return st;
    }

    public void setState(Game state) {
        st = state;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public HashSet<Node> getChildren() {
        return children;
    }

    public void setChildArray(HashSet<Node> ch) {
        children = ch;
    }


    public Node getRandomChild() {
        int size = children.size();
        int item = new Random().nextInt(size);
        int i = 0;
        for(Node child : children)
        {
            if (i == item){
                return child;
            }
            i++;
        }

        return null;
    }

    public Node getChildWithMaxScore() {
        return Collections.max(children, Comparator.comparing(c -> {
            return c.getState().getScore();
        }));
    }
}

class Tree{

    Node root;

    public Tree(Game st) {
        root = new Node(st);
    }

    public Tree(Node root) {
        this.root = root;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public void addChild(Node parent, Node child) {
        parent.getChildren().add(child);
    }
}
