package cn.itcast.job.task.leakcanary.matrix;

import cn.itcast.job.utils.FileUtil;
import utils.Log;

import java.io.Serializable;
import java.util.*;

public class EvilMethodStackFlameGraphUtils {
//    public static String stack = " 0,android.os.Handler dispatchMessage (Landroid.os.Message;)V,1,709\n" +
//            "1,androidx.localbroadcastmanager.content.LocalBroadcastManager$1 handleMessage (Landroid.os.Message;)V,1,705\n" +
//            "2,androidx.localbroadcastmanager.content.LocalBroadcastManager executePendingBroadcasts ()V,1,705\n";

    private static void backtrack(Node node, List<Stack<Node>> allPathsFromRootToLeaf, Node root, Stack<Node> stack) {
        if (node == root) {
            stack = new Stack<>();
        }
        stack.push(node);
        if (node.thisNodeTime(true) > 0) {
            allPathsFromRootToLeaf.add((Stack<Node>) stack.clone());
        }
//        Log.i("push " + node);
        // 组合：遍历的起始点是 index
        if (node.children != null && node.children.size() > 0) {
            long timeAllChildren = 0;
            for (int i = 0; i < node.children.size(); i++) {
//                Log.i("forward child " + node.children.get(i));
                timeAllChildren += node.children.get(i).time;
                backtrack(node.children.get(i), allPathsFromRootToLeaf, root, stack);
//                Log.i("pop child " + node.children.get(i));
                stack.pop();
            }
        } else {
//            Log.i("leaf " + stack.size());
            int i = 0;
            for (Node node1 : stack) {
//                Log.i("stack level " + i + " " + node1);
                i++;
            }
            allPathsFromRootToLeaf.add( (Stack<Node>) stack.clone());
        }
    }

    public static String convertToFoldFile(String stackOfEvil, String pathOfFoldFile) {
//        try {
//        Log.i("stackOfEvil");
        String[] lines = stackOfEvil.split("\n");
        Node root = null;
        Map<Integer, Node> everyLevelLastNode = new HashMap<>();//存放每一层出现的最后一个
        Node beforeNode = null;//上一个node
        for (String line : lines) {
            line = line.trim();
            if (root == null) {
                root = new Node(line);
                everyLevelLastNode.put(root.level, root);
                beforeNode = root;
            } else {
                Node node = new Node(line);
                everyLevelLastNode.put(node.level, node);
                if (node.level - beforeNode.level == 1) {
                    beforeNode.addChild(node);
                } else {
                    Node parent = everyLevelLastNode.get(node.level - 1);
                    int beforeLevel = node.level - 1;
                    while (parent == null && beforeLevel > 0) {
                        beforeLevel--;
                        parent = everyLevelLastNode.get(beforeLevel);
                    }
                    parent.addChild(node);
                }
                beforeNode = node;
            }
        }

        List<Stack<Node>> allPathsFromRootToLeaf = new ArrayList<>();
        backtrack(root, allPathsFromRootToLeaf, root, null);
        Log.i("Result");
        StringBuilder stringBuilder = new StringBuilder();
        for (Stack<Node> stack : allPathsFromRootToLeaf) {
            for (int i = 0; i <= stack.size() - 1; i++) {
                stringBuilder.append(stack.get(i));
                if (i != stack.size() - 1) {
                    stringBuilder.append(";");
                }
            }
            stringBuilder.append(" " + stack.peek().thisNodeTimeResult());
            stringBuilder.append("\n");
        }
        if (pathOfFoldFile != null) {
//            FileUtil.writeFile(pathOfFoldFile, stringBuilder.toString(), false);
        }
        Log.i(stringBuilder.toString());
        return stringBuilder.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "";
//        }
    }

    private static class Node implements Serializable {
        String infos;   //"7,me.leolin.shortcutbadger.impl.HuaweiHomeBadger executeBadge (Landroid.content.Context;Landroid.content.ComponentName;I)V,1,24\n"
        int level;      //层级
        String fun;     //函数
        int frequent;   //次数
        long time;      //执行时间 ms
        boolean haveErrorWhenInit = false;

        List<Node> children = new ArrayList<>();

        public Node(String v) {
            infos = v;
            try {
                String[] infosArray = infos.split(",");
                level = Integer.parseInt(infosArray[0]);
                fun = infosArray[1];
                frequent = Integer.parseInt(infosArray[2]);
                time = Long.parseLong(infosArray[3]);
            } catch (Exception e) {
                e.printStackTrace();
                haveErrorWhenInit = true;
            }
        }

        /**
         * 除了当前节点花的时间
         *
         * @param needCheckChild 需要检查是否有子节点，如果没有子节点则返回0
         * @return
         */
        public long thisNodeTime(boolean needCheckChild) {
            if (needCheckChild) {
                if (children == null || children.size() == 0) {
                    return 0;
                }
            }
            long timeOfChild = 0;
            for (Node node : children) {
                timeOfChild += node.time;
            }
            return time - timeOfChild;
        }

        public long thisNodeTimeResult() {
            long timeOfChild = 0;
            for (Node node : children) {
                timeOfChild += node.time;
            }
            return ((time - timeOfChild) == 0) ? 1 : (time - timeOfChild);
        }

        @Override
        public String toString() {
            return fun.replaceAll(";", ",") + ",调用了" + frequent + "次," + time + "ms";
        }

        public void addChild(String child) {
            if (child == null || child.isEmpty()) {
                return;
            }
            children.add(new Node(child));
        }

        public Node addChild(Node child) {
            children.add(child);
            return this;
        }
    }
}
