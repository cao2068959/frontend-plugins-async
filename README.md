# frontend-plugins

前端异步打包maven插件

> 本项目是改造frontend-plugins 插件，用于可以异步化前端打包流程，插件原地址 https://github.com/eirslett/frontend-maven-plugin

## 基本流程
本插件可以把整个插件的执行流程给异步化，脱离maven生命周期的同步化。

但是这个插件里所有的任务，都是同步的。

列如：
> A模块加载了 frontend-plugins 插件,在A模块 构建周期 clean 中 同时设置了 4个异步任务。
>
> B模块 同样加载了 frontend-plugins 插件 然后在compile构建周期 中设置了任务 waitAsync
>
>那么运行结果为，会在A模块 执行clean时，开启一个线程来顺序执行 设置的4个异步任务，同时maven正常的构建流程会同时执行。
>
>正常的maven构建流程走到B模块执行compile 周期的时候，会去调用waitAsync 任务，这个任务会阻塞maven的正常构建流程，直到异步任务完全执行结束。


## 配置
配置和之前一样，在pom文件中，指定插件的nodejs配置以及要执行的命令即可。

 ### 增加2个参数
     
 ### async
 >用于全局的设置插件是否开启异步任务
```$xslt 
<plugin>
    <groupId>插件坐标</groupId>
    <artifactId>插件坐标</artifactId>
    <configuration>
        <!-- 开启异步任务 -->    
        <async>true</async>
     </configuration>
    <execution>
        要执行的任务
    </execution>
</plugin>
```
 
  ### lastTask
  >用于设置对应的插件任务是最后一个任务，当这个任务执行结束后，会关闭对应的线程
 ```$xslt 
      <execution>
         <id>任务Id</id>
         <goals>
            <goal>要执行的任务</goal>
         </goals>
         <configuration>
            <arguments>这个任务的某一个参数</arguments>
            <!-- 指定任务是最后一个 -->
            <lastTask>true</lastTask>
          </configuration>
       </execution>
 ```
 
 
 ### 增加了一个任务 
 
 ### waitAsync
 > 这个任务可以设置在一个合适的位置来阻塞等待异步任务的执行结束。这个任务是在maven的构建周期之中的。所以他阻塞后整个
>maven的构建流程会暂停，直到等待异步任务执行结束。

- 注意
    - 虽然这个任务也是在frontend-plugins 之中，但是不会受到 async关键字的影响
    - 必须要设置关键字 lastTask 来显示的指定最后一个任务是谁，不然将 永远阻塞，直到世界末日
    - 任务waitAsync 可以不和异步任务的任务设置在一起。
   
```
    <executions>
       <execution>
          <id>waitAsync</id>
           <!-- 这个任务要执行在哪一个Maven生命周期之中 -->
          <phase>generate-resources</phase>
           <goals>
              <goal>waitAsync</goal>
           </goals>
       /execution>
    </executions>    

```
