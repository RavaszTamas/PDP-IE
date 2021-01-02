import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Main {

    private static int NUMBER_OF_GRAPHS = 10;
    private static AtomicBoolean found = new AtomicBoolean(false);
    private static List<Integer> resultPath;

    public static void main(String[] args) {
        List<DirectedGraph> graphList = new ArrayList<>();
            IntStream.range(1, NUMBER_OF_GRAPHS + 1).forEach(i -> {
            graphList.add(generateRandomGraphWithPossibleSolution(i*10));

        });

        solveGraphsSequential(graphList);
        solveGraphsParallel(graphList);

    }

    private static void solveGraphsSequential(List<DirectedGraph> graphList) {

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("testResults.txt", true)))) {
            graphList.forEach(graph -> {
                long startingTime = System.currentTimeMillis();
                solveSequential(graph);
                long endingTime = System.currentTimeMillis();
                writer.println("sequential test: size of graph: " +graph.numberOfNodes()+"; time: " + (endingTime-startingTime) + "; ms");
            });
        } catch (IOException ignored) {

        }

    }

    /**
     *
     * Seqential solution uses a backtracking method to find the hamiltonian  cycle
     * @param graph
     */
    private static void solveSequential(DirectedGraph graph) {

        List<Integer> path = new ArrayList<>();
        int currentVertex = 0;

        if (hamiltonianCycleUtil(graph, path, currentVertex, currentVertex)) {
            System.out.println("The solution: " + path);
        } else {
            System.out.println("No possible solution");
        }

    }

    /**
     * At each step we add a node then call recursively the function for a neighbour until we have a valid cycle, then return true
     * if we hane no cycle we return false,
     * @param graph
     * @param path
     * @param currentVertex
     * @param starting
     * @return
     */
    private static boolean hamiltonianCycleUtil(DirectedGraph graph, List<Integer> path, int currentVertex, int starting) {
        if(found.get())
            return false;
        path.add(currentVertex);
        if (path.size() == graph.numberOfNodes()) {
            if(graph.getNeighboursOfNode(currentVertex).contains(starting))
            return graph.getNeighboursOfNode(currentVertex).contains(starting);
        }

        for (Integer neighbour : graph.getNeighboursOfNode(currentVertex)) {
            if (!path.contains(neighbour)) {
                return hamiltonianCycleUtil(graph, path, neighbour, starting);
            }
        }
        path.remove(path.size() - 1);

        return false;
    }

    /**
     * Parallel solution uses a similar method but for each recursive call we create a new thread until we use up the total amount of threads
     * then we will call the sequential solution to solve it for that sub part
     * @param graphList
     */
    private static void solveGraphsParallel(List<DirectedGraph> graphList) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("testResults.txt", true)))) {
            int poolSize = Runtime.getRuntime().availableProcessors();
            graphList.forEach(graph -> {
                long startingTime = System.currentTimeMillis();
                solveParallel(graph,Runtime.getRuntime().availableProcessors());
                long endingTime = System.currentTimeMillis();
                writer.println("parallel test: number of threads: " + poolSize + "; size of graph: " +graph.numberOfNodes()+"; time: " + (endingTime-startingTime) + " ms");
            });

        } catch (IOException ignored) {

        }

    }

    /**
     * Here the search is started with some initalization and the displaying o the results
     * @param graph the graph
     * @param threadCount number of available threads
     */
    private static void solveParallel(DirectedGraph graph,int threadCount) {
        List<Integer> path = new ArrayList<>();
        int currentVertex = 0;
        found.set(false);
//        System.out.println(graph);
        resultPath = new ArrayList<>(graph.numberOfNodes());
        searchForSolutionParallel(graph, path, currentVertex, currentVertex, new AtomicInteger(threadCount));


        if (found.get()) {
            System.out.println("Solution found: " + resultPath);
        } else {
            System.out.println("No solution!");
        }

    }

    /**
     * This is similar to the sequential util function but in this case several threads are created to find a solution
     * until the "pool" is filled then a for each thread the standard util function is called
     * @param graph
     * @param path
     * @param currentVertex
     * @param startingNode
     * @param nrOfThreads
     */
    private static void searchForSolutionParallel(DirectedGraph graph, List<Integer> path, int currentVertex, int startingNode, AtomicInteger nrOfThreads) {
        if (found.get()) return;

        path.add(currentVertex);
        if (path.size() == graph.numberOfNodes()) {

            if (graph.getNeighboursOfNode(currentVertex).contains(startingNode)) {
                if (found.compareAndSet(false, true)) {
                    resultPath.addAll(path);
                }
                return;
            }
        }

        List<Thread> childThreads = new ArrayList<>();

        for (Integer neighbour : graph.getNeighboursOfNode(currentVertex)) {
            if (!path.contains(neighbour)) {
                int left = nrOfThreads.decrementAndGet();
                List<Integer> copyOfPath = new ArrayList<>();
                for(Integer integer:path)
                {
                    copyOfPath.add(integer.intValue());
                }

                if(left >= 1) {
                    Thread childThread = new Thread(() -> {
                            searchForSolutionParallel(graph, copyOfPath, neighbour, startingNode,nrOfThreads);
                        });

                    childThread.start();
                    childThreads.add(childThread);

                }
                else{
                    if(hamiltonianCycleUtil(graph,copyOfPath,neighbour,startingNode))
                    {
                        if (found.compareAndSet(false, true)) {
                            resultPath.addAll(copyOfPath);
                        }
                    }
                }
            }
        }

        for(Thread thread:childThreads) {
            try {
                thread.join();
                nrOfThreads.incrementAndGet();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Generate graph with random size
     * @param sizeOfGraph
     * @return
     */
    static DirectedGraph generateRandomGraphWithPossibleSolution(int sizeOfGraph) {
        DirectedGraph newGraph = new DirectedGraph(sizeOfGraph);
        Random random = new Random();

        var nodesCopy = new ArrayList<>(newGraph.getNodes());

        Collections.shuffle(nodesCopy);

        for (int i = 1; i < nodesCopy.size(); i++) {
            newGraph.addEdge(nodesCopy.get(i - 1), nodesCopy.get(i));
        }

        newGraph.addEdge(nodesCopy.get(nodesCopy.size() - 1), nodesCopy.get(0));


        for (int i = 0; i < sizeOfGraph / 2; i++) {
            int source = random.nextInt(sizeOfGraph - 1);
            int destination = random.nextInt(sizeOfGraph - 1);
            newGraph.addEdge(source, destination);
        }
        return newGraph;
    }
}
