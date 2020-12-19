## Brainsuck

Brainsuck is an optimizing compiler prototype for the Brainfuck programming language.  It's written in Scala using the same technologies used in Spark SQL's Catalyst optimizer.  It's minimum, and only consists of 292 lines of code (test code not included).  This project is designed for [my QCon Beijing 2015 talk][1].

感谢原作者，这里将Brainsuck改为了maven编译版本，方便导入使用  
vm options: -Xss8m  
Program arguments: ../scripts/hanoi.b -O 2  
运行Interpreter类即可

## Licence

MIT

[1]: https://www.slideshare.net/liancheng/dive-into-catalyst
[2]: http://calmerthanyouare.org/2015/01/07/optimizing-brainfuck.html
