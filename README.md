# Matrix Trace Processor

处理  [matrix-trace-canary](https://github.com/Tencent/matrix/tree/master/matrix/matrix-android/matrix-trace-canary) 中的堆栈信息。

## Thanks

[profilo](https://github.com/facebookincubator/profilo)

## Demo

![demo](https://raw.githubusercontent.com/LinXiaoTao/matrix-trace-processor/master/demo/1581129760409.png)

## Java版本

1. MethodMapping.java负责解析卡顿堆栈

2. EvilMethodStackFlameGraphUtils.java负责将解析之后的堆栈转换为.folded文件

3. 使用 [FlameGraph](https://github.com/brendangregg/FlameGraph) 将上面输出的 1581129760409.folded 转到 svg 文件

   ``` shell
    flamegraph.pl demo/1581129760409.folded > demo/1581129760409.svg
   ```

## Python版本 Usage

1. 保存堆栈信息到文件中

2. 将 matrix 生成的 methodMapping.txt 保存下来

3. 执行 python 脚本

   ``` shell
   python3 main.py workflow_traces demo/1581129760409.log > demo/1581129760409.txt demo/methodMapping.txt
   python3 main.py workflow_traces /Users/admin/StudioProjects/demo/app/matrixTrace/1581129760409.log > /Users/admin/StudioProjects/demo/app/matrixTrace/1581129760409.txt /Users/admin/StudioProjects/demo/app/matrixTrace/methodMapping.txt
   ```

4. 使用 [FlameGraph](https://github.com/brendangregg/FlameGraph) 将上面输出的 1581129760409.txt 转到 svg 文件

   ``` shell
    stackcollapse.pl demo/1581129760409.txt > demo/1581129760409.folded
    flamegraph.pl demo/1581129760409.folded > demo/1581129760409.svg
   ```
   ``` shell
    stackcollapse.pl /Users/admin/StudioProjects/demo/app/matrixTrace/1581129760409.txt > /Users/admin/StudioProjects/demo/app/matrixTrace/1581129760409.folded
    /Users/admin/StudioProjects/FlameGraph/flamegraph.pl /Users/admin/StudioProjects/demo/app/matrixTrace/1581129760409.folded > /Users/admin/StudioProjects/demo/app/matrixTrace/1581129760409.svg
   ```
## Contributing

PRs accepted.

## License

MIT