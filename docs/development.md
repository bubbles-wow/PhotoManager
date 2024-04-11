# PhotoManager
## 介绍
简单的图片查看/管理程序

## 程序要求
1. 创建GUI界面，Java Swing或者Java FX等实现
2. 必须支持的文件类型：JPG、 JPEG、 GIF、 PNG、和 BMP。可以加点其他的图片格式。
3. 大文件支持，按比例显示等。
4. 主要功能是文件夹内的图片预览和图片幻灯片展示。

## 架构设计
初步分为涉及文件的读写操作的后端和程序图形界面的各种逻辑实现的前端。  
前端主要负责处理用户的交互，将用户交互转化为相应的请求并调用后端来处理，而后端主要负责处理前端的接口调用以及维持整个程序的正常运行。

   ### 后端主要功能设计
   1. 目录树。可以用类实现，比较方便。考虑是打开文件夹才读取存储（花销小点）或者是后台读取（方便搜索，如果做搜索功能的话）。注意文件夹都要显示，而文件只需要显示图片格式文件即可。
   2. 图片缓存/预加载，用于显示缩略图，需要尽可能少占内存。
   3. 文件复制粘贴重命名操作等，还有文件批量重命名。
   4. 文件搜索功能
   5. 打开特定文件（从内部或外部）

   ### 前端主要功能设计
   主要有4种窗口，主要的文件夹浏览窗口，图片查看窗口，文件批量重命名窗口，还有各种警告提示框等。

   - 主窗口（文件浏览器）
      1. 界面设计  
         参考系统文件资源管理器，顶部是地址栏，搜索框与功能栏，左侧为目录预览栏（目录树），底部为状态栏（文件信息，文件夹信息等），中间为文件夹下的图片预览。
      2. 功能设计  
         - 界面设计实现
         - 文件拖拽，主要表现为将选中的文件拖到文件夹上时，会复制/移动文件到相应的文件夹等。
         - 文件重命名，单选文件时直接在主窗口实现
         - 文件选择，实现能够选择文件，并显示出来
         - 文件打开，进入下一级目录，图片预览打开等
         - 地址栏相关功能：能够实现当前路径显示
         - 右键菜单，

   - 图片查看器  
      1. 界面设计  
         主要预览的图片显示在窗口正中间，工具栏和图片预览栏在窗口下部。要确保图片的比例显示正确。
      2. 功能设计  
         - 工具栏按钮的功能实现（上一张，下一张，播放，放大缩小，旋转等）
         - 幻灯片播放，实现定时显示下一张图片，并隐藏工具栏。
   
   - 文件批量重命名窗口  
      1. 界面设计  
         竖排的文本输入框来读取用户输入名称前缀、起始编号、编号位数等，并给出相应说明。
      2. 功能设计
         没什么特别的，主要收集用户输入的参数，调用后端相应方法实现。
   
   - 提示框  
      - 在用户进行敏感操作（对文件修改等）进行提示  

## 模块设计  
### 目录树实现  
   1. 模块概述  
      使用FileNode类实现，主要是用File类来保存当前文件的信息，并实现部分文件修改操作。
   2. 模块功能  
      - 文件查看：
      - 文件复制：
      - 文件移动：
      - 文件重命名：
   3. 模块主要方法（接口）  
      TODO
   4. 使用的数据结构  
      TODO
   5. 依赖关系  
      [java.io.File](https://docs.oracle.com/javase/8/docs/api/java/io/File.html)  
      TODO
### 
      



## ~~TODO~~ 
