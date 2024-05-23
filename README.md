# PhotoManager  
一个图片管理程序，使用 Java Swing 实现，支持 Windows 和 Linux (支持GUI的Linux发行版)

# 使用方式
1. 确保系统已经安装了JDK，并将相关路径添加到系统环境变量中  
   JDK安装方法：[Windows](https://www.runoob.com/java/java-environment-setup.html#win-install)、[Linux](https://www.runoob.com/java/java-environment-setup.html#linux-install)
2. 打开终端并前往源代码所在的路径（使用`cd`等命令）
3. 运行以下命令进行构建
   - Windows
     ```shell
     build.bat
     ```
   - Linux
     ```shell
     bash build.sh
     ```
4. 运行以下命令启动程序
   ```shell
   java -jar PhotoManager.jar
   ```

## 已实现功能
1. 目录树以及其他文件夹切换方式
2. 图片格式支持：`jpg`, `jpeg`, `png`, `bmp`, `gif`
3. 图片预览页面及逻辑（选中文件、右键菜单等）
4. 文件复制、粘贴、删除、单个文件重命名
5. 文件操作冲突提示
6. 图片查看、幻灯片放映

## 问题
1. 幻灯片放映
   - 图片过大时加载缓慢，但计时不变，导致看起来很快就切换下一张了
2. Linux 系统下的问题（非重点）  
   - 幻灯片放映选项窗口默认大小不合适，要手动调整
   - 双击图片进入图片查看器不会加载图片，需要手动调整窗口后才会加载
   - 图片查看器窗口默认加载的图片会偏大（与窗口边框的粗细有关）

## 最近修改
1. 提高文件加载速度
2. 加载更多文件时更快，尽可能减少滚动卡顿（12000个文件大概10秒左右完成加载，并可操作）
3. 信息栏新增显示文件大小信息
4. 优化部分窗口的展开位置
5. 优化图片查看器的自适应效果，现在会根据图片大小和窗口大小来判断调整
6. 修改了全局字体
