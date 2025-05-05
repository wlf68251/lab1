import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyGraphApplication {
    public static MyGraph graph = new MyGraph("./1.txt");

    // 显示图的功能选择
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 创建图

        while (true) {
            System.out.println("\n请选择一个操作:");
            System.out.println("1. 展示有向图");
            System.out.println("2. 查询桥接词");
            System.out.println("3. 根据桥接词生成新文本");
            System.out.println("4. 计算最短路径");
            System.out.println("5. 计算PageRank值");
            System.out.println("6. 随机游走");
            System.out.println("7. 计算单个单词到其他单词的最短路径");
            System.out.println("0. 退出");
            int choice = scanner.nextInt();
            scanner.nextLine(); // 清空缓冲区

            switch (choice) {
                case 1:
                    showDirectedMyGraph(graph);
                    break;
                case 2:
                    System.out.println("请输入两个单词:");
                    String word1 = scanner.nextLine();
                    String word2 = scanner.nextLine();
                    System.out.println(graph.queryBridgeWords(word1, word2));
                    break;
                case 3:
                    System.out.println("请输入文本:");
                    String txt = scanner.nextLine();
                    System.out.println("新文本: " + generateNewText(txt));
                    break;
                case 4:
                    System.out.println("请输入两个单词:");
                    String wordStart = scanner.nextLine();
                    String wordEnd = scanner.nextLine();
                    System.out.println("最短路径: " + calcShortestPath(graph.adjList, wordStart, wordEnd));
                    break;
                case 5:
                    System.out.println("请输入需要计算pagerank的单词:");
                    String word = scanner.nextLine();
                    System.out.println(graph.calculatePageRank(word));
                    break;
                case 6:
                    System.out.println(graph.randomWalk());
                    break;
                case 7:
                    System.out.println("请输入一个单词:");
                    String wordInput = scanner.nextLine();
                    // 计算该单词到其他所有单词的最短路径
                    for (String node : graph.adjList.keySet()) {
                        if (!node.equals(wordInput)) {
                            System.out.println("从 " + wordInput + " 到 " + node + " 的最短路径: "
                                    + calcShortestPath(graph.adjList, wordInput, node));
                        }
                    }
                    break;
                case 0:
                    System.out.println("退出程序.");
                    return;
                default:
                    System.out.println("无效选择，请重新输入。");
            }
        }
    }

    public static void exportGraphDataToCSV(MyGraph graph, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            // 写入 CSV 的标题
            writer.append("Source,Target,Weight\n");

            // 遍历图的边，写入节点对及权重
            Map<String, Map<String, Integer>> adjList = graph.adjList;
            for (String node : adjList.keySet()) {
                Map<String, Integer> neighbors = adjList.get(node);
                for (Map.Entry<String, Integer> neighborEntry : neighbors.entrySet()) {
                    String neighbor = neighborEntry.getKey();
                    Integer weight = neighborEntry.getValue();
                    writer.append(node).append(",").append(neighbor).append(",").append(weight.toString()).append("\n");
                }
            }

            System.out.println("Graph data exported to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 展示有向图
    public static void showDirectedMyGraph(MyGraph G) {
        exportGraphDataToCSV(graph, "./graph_data.csv");
        G.displayMyGraph();
    }

    // 产生新文本
    public static String generateNewText(String inputText) {
        String[] words = inputText.split(" ");
        StringBuilder result = new StringBuilder(words[0]);

        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];

            if (graph.adjList.containsKey(word1) && graph.adjList.containsKey(word2)) {
                // 遍历word1的邻接单词
                Map<String, Integer> neighbors1 = graph.adjList.get(word1);
                for (String word3 : neighbors1.keySet()) {
                    // 检查word3是否有指向word2的邻接单词
                    Map<String, Integer> neighbors3 = graph.adjList.get(word3);
                    if (neighbors3 != null && neighbors3.containsKey(word2)) {
                        result.append(" ").append(word3); // 如果word3是桥接词，加入到结果中
                    }
                }
            }
            // 添加第二个单词
            result.append(" ").append(word2);
        }

        return result.toString();
    }

    // 计算最短路径，考虑权重
    public static String calcShortestPath(Map<String, Map<String, Integer>> adjList, String word1, String word2) {
        if (!adjList.containsKey(word1) || !adjList.containsKey(word2)) {
            return "No such words in the graph!";
        }

        Map<String, String> parentMap = new HashMap<>();
        Map<String, Integer> distance = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.offer(word1);
        visited.add(word1);
        distance.put(word1, 0);
        parentMap.put(word1, null);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(word2)) {
                break;
            }

            Map<String, Integer> neighbors = adjList.get(current);
            if (neighbors == null) {
                continue; // 如果邻接列表为 null，则跳过
            }

            for (Map.Entry<String, Integer> neighborEntry : neighbors.entrySet()) {
                String neighbor = neighborEntry.getKey();
                int edgeWeight = neighborEntry.getValue();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                    distance.put(neighbor, distance.get(current) + edgeWeight);
                    parentMap.put(neighbor, current);
                }
            }
        }

        if (!parentMap.containsKey(word2)) {
            return "No path from " + word1 + " to " + word2 + "!";
        }

        List<String> path = new ArrayList<>();
        String current = word2;
        while (current != null) {
            path.add(current);
            current = parentMap.get(current);
        }

        Collections.reverse(path);
        StringBuilder pathString = new StringBuilder();
        for (String word : path) {
            pathString.append(word).append(" ");
        }

        return pathString.toString().trim() + " --with total weight: " + distance.get(word2);
    }

}

// 假设图类
class MyGraph {
    public static Map<String, Map<String, Integer>> adjList; // 每条边都带有权重
    public static List<String> words;

    public MyGraph(String filepath) {
        words = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath));
            String line;
            while ((line = reader.readLine()) != null) {
                // 使用正则表达式提取英文单词
                Matcher matcher = Pattern.compile("[a-zA-Z]+").matcher(line);
                while (matcher.find()) {
                    words.add(matcher.group().toLowerCase());
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("文件读取失败: " + e.getMessage());
        }

        adjList = new HashMap<>();

        // 遍历words，构建邻接表并添加权重
        for (int i = 0; i < words.size() - 1; i++) {
            String word1 = words.get(i);
            String word2 = words.get(i + 1);

            // 确保word1在adjList中有一个空的邻接列表，即使它没有出边
            adjList.putIfAbsent(word1, new HashMap<>());
            adjList.putIfAbsent(word2, new HashMap<>()); // 确保word2也被加入

            // 添加word2到word1的邻接链表，权重为1（你可以根据实际情况修改权重）
            adjList.get(word1).put(word2, adjList.get(word1).getOrDefault(word2, 0) + 1);
        }

        System.out.println("Adjacency List: " + adjList);

    }

    // 展示图
    public void displayMyGraph() {
        System.out.println("有向图的邻接表（带权重）:");
        Set<String> printedNodes = new HashSet<>(); // 用于记录已打印的节点

        for (Map.Entry<String, Map<String, Integer>> entry : adjList.entrySet()) {
            String node = entry.getKey();
            // 如果节点已经打印过，跳过
            if (printedNodes.contains(node)) {
                continue;
            }

            // 打印节点和它的邻接表（包括权重）
            Map<String, Integer> neighbors = entry.getValue();
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> neighborEntry : neighbors.entrySet()) {
                sb.append(neighborEntry.getKey()).append("(").append(neighborEntry.getValue()).append(") ");
            }
            System.out.println(node + " -> " + sb.toString().trim());

            // 将该节点标记为已打印
            printedNodes.add(node);
        }
    }

    // 获取邻接的节点，确保返回值是List类型而不是Map
    public List<String> getNeighbors(String node) {
        Map<String, Integer> neighbors = adjList.get(node);
        if (neighbors != null) {
            return new ArrayList<>(neighbors.keySet());
        }
        return new ArrayList<>();
    }

    public Double calculatePageRank(String wordseek) {
        Map<String, Double> pageRank = new HashMap<>();
        final double DAMPING_FACTOR = 0.85;
        final int MAX_ITERATIONS = 100;
        final double THRESHOLD = 1e-6;

        // 初始化PageRank值
        int numNodes = adjList.size();
        for (String word : adjList.keySet()) {
            pageRank.put(word, 1.0 / numNodes); // 初始值均为 1 / N
        }

        // 迭代计算PageRank
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            Map<String, Double> newPageRank = new HashMap<>();
            double maxDiff = 0.0;

            // 遍历每个节点，计算新的PageRank值
            for (String node : adjList.keySet()) {
                double newRank = (1 - DAMPING_FACTOR) / numNodes; // 基础PR值

                // 获取所有指向当前节点的入度邻居，使用 Set 去重
                Set<String> inNeighbors = new HashSet<>(getInNeighbors(node));

                // 累加每个入度邻居的PageRank贡献
                for (String neighbor : inNeighbors) {
                    newRank += DAMPING_FACTOR * pageRank.get(neighbor) / adjList.get(neighbor).size();
                }

                // 更新新的PageRank值
                newPageRank.put(node, newRank);

                // 计算最大差异，用于判断是否收敛
                maxDiff = Math.max(maxDiff, Math.abs(newRank - pageRank.get(node)));
            }

            // 更新PageRank值
            pageRank = newPageRank;

            // 如果变化非常小，认为计算已收敛，停止迭代
            if (maxDiff < THRESHOLD) {
                break;
            }
        }

        // 返回目标节点的PageRank值
        return pageRank.getOrDefault(wordseek, 0.0); // 如果节点不存在，返回0.0
    }

    // 获取节点的入度邻居（指向该节点的边），返回权重
    private List<String> getInNeighbors(String node) {
        List<String> inNeighbors = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : adjList.entrySet()) {
            if (entry.getValue().containsKey(node)) {
                inNeighbors.add(entry.getKey());
            }
        }
        return inNeighbors;
    }

    public static String randomWalk() {
        Random rand = new Random();

        // 从图中随机选择一个起点
        List<String> nodes = new ArrayList<>(adjList.keySet());
        String currentNode = nodes.get(rand.nextInt(nodes.size()));

        Set<String> visitedNodes = new HashSet<>();
        Set<String> visitedEdges = new HashSet<>();
        List<String> walkPath = new ArrayList<>();
        List<String> edges = new ArrayList<>();

        visitedNodes.add(currentNode);

        // 开始随机游走
        while (adjList.containsKey(currentNode) && !adjList.get(currentNode).isEmpty()) {
            Map<String, Integer> neighbors = adjList.get(currentNode);
            List<String> nextNodes = new ArrayList<>(neighbors.keySet());
            List<Integer> weights = new ArrayList<>(neighbors.values());

            // 计算总权重
            int totalWeight = weights.stream().mapToInt(Integer::intValue).sum();

            // 按照权重选择下一个节点
            int randomWeight = rand.nextInt(totalWeight);
            int cumulativeWeight = 0;
            String nextNode = null;

            for (int i = 0; i < nextNodes.size(); i++) {
                cumulativeWeight += weights.get(i);
                if (randomWeight < cumulativeWeight) {
                    nextNode = nextNodes.get(i);
                    break;
                }
            }

            String edge = currentNode + "->" + nextNode;

            // 如果已经访问过该边，则停止游走
            if (visitedEdges.contains(edge)) {
                break;
            }

            visitedEdges.add(edge);
            walkPath.add(nextNode);
            edges.add(edge);
            visitedNodes.add(nextNode);

            // 移动到下一个节点
            currentNode = nextNode;
        }

        // 输出随机游走的节点路径
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("random_walk_output.txt"))) {
            writer.write("随机游走路径: " + String.join(" -> ", walkPath));
            writer.newLine();
            writer.write("游走的边: " + String.join(", ", edges));
        } catch (IOException e) {
            System.out.println("文件写入失败: " + e.getMessage());
        }

        return ("随机游走路径: " + String.join(" -> ", walkPath));
    }

    public static String queryBridgeWords(String word1, String word2) {
        // 检查两个单词是否存在
        if (!words.contains(word1) || !words.contains(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }

        List<String> bridgeWords = new ArrayList<>();
        Map<String, Integer> word1Neighbors = adjList.get(word1);

        // 遍历word1的邻接单词
        for (Map.Entry<String, Integer> entry : word1Neighbors.entrySet()) {
            String word3 = entry.getKey();
            // 检查word3是否有指向word2的邻接单词
            if (adjList.containsKey(word3) && adjList.get(word3).containsKey(word2)) {
                bridgeWords.add(word3); // 如果word3是桥接词，加入到结果中
            }
        }

        // 根据结果输出不同的提示
        if (bridgeWords.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        } else {
            return "The bridge words from " + word1 + " to " + word2 + " are: " + String.join(", ", bridgeWords) + ".";
        }
    }

}