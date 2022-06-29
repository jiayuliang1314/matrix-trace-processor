package cn.itcast.job.task.leakcanary.matrix;

import cn.itcast.job.task.leakcanary.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import utils.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MethodMapping {
    @Value("${file.of.methodmapping}")
    private String methodMapping;
    //存放mapping文件
    private static ConcurrentHashMap<Integer, String> methodMap = new ConcurrentHashMap<>();

    public void prepareMethodMapping(){
        methodMap.clear();
        readMappingFile(methodMap);
    }

    /**
     * 从methodMapping读取，将方法ID，函数特征值存放到methodMap里
     * @param methodMap
     */
    public void readMappingFile(ConcurrentHashMap<Integer, String> methodMap) {
        Log.i("MethodMapping","readMappingFile");
        BufferedReader reader = null;
        String tempString = null;
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(methodMapping), StandardCharsets.UTF_8);
            reader = new BufferedReader(isr);
            while ((tempString = reader.readLine()) != null) {
                String[] contents = tempString.split(",");
                methodMap.put(Integer.parseInt(contents[0]), contents[2].replace('\n', ' '));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 解析stackKey
     * @param key
     * @return
     */
    public String getFormatKey(String key) {
        if (key == null || key.isEmpty()) {
            return "";
        }
        String stackKey = key;//entry.getKey();
        String afterFormatStackKey = key;//entry.getKey();
        int methodid = 0;
        try {
            methodid = Integer.parseInt(stackKey);
        } catch (NumberFormatException e) {
        }
        if (methodid != 0 && methodMap.containsKey(methodid)) {
            afterFormatStackKey = methodMap.get(methodid);
        }
        return afterFormatStackKey;
    }

    /**
     * 解析stack
     * @param stack
     * @return
     */
    public String getFormatStack(String stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(" ");
        String[] lines = stack.split("\n");
        for (String line : lines) {
            if (line == null || line.isEmpty()) {
                continue;
            }
            String[] args = line.split(",");
            int method = 0;
            try {
                method = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                stringBuilder.append(args[0]);
                stringBuilder.append(",");
                if (args.length == 5) {
                    stringBuilder.append(args[4]);
                    stringBuilder.append(",");
                } else {
                    stringBuilder.append(args[1]);
                    stringBuilder.append(",");
                }
                stringBuilder.append(args[2]);
                stringBuilder.append(",");
                stringBuilder.append(args[3] + "\n");
                continue;
            }
            boolean isContainKey = methodMap.containsKey(method);
            if (!isContainKey) {
                stringBuilder.append(args[0]);
                stringBuilder.append(",");
                if (args.length == 5) {
                    stringBuilder.append(args[4]);
                    stringBuilder.append(",");
                } else {
                    stringBuilder.append(args[1]);
                    stringBuilder.append(",");
                }
                stringBuilder.append(args[2]);
                stringBuilder.append(",");
                stringBuilder.append(args[3] + "\n");
                continue;
            }

            args[1] = methodMap.get(method);
            stringBuilder.append(args[0]);
            stringBuilder.append(",");
            stringBuilder.append(args[1]);
            stringBuilder.append(",");
            stringBuilder.append(args[2]);
            stringBuilder.append(",");
            stringBuilder.append(args[3] + "\n");
        }
        return stringBuilder.toString();
    }
}
