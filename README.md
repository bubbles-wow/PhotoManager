# PhotoManager  
一个图片管理程序

## 已实现功能
1. 目录树以及其他文件夹切换方式
2. 图片格式支持：`jpg`, `jpeg`, `png`, `bmp`, `gif`
3. 图片预览页面及逻辑（选中文件、右键菜单等）
4. 文件复制、粘贴、删除、单个文件重命名
5. 文件操作冲突提示

## 待实现功能
1. 图片查看器（幻灯片放映）
2. 批量重命名

## 问题
1. 目录树相关
    - ~~有时节点展开会被意外重置~~
    - ~~文件夹显示不全（目录在加载完成前就刷新了）~~
    - ~~点击的节点识别错误~~  
   全都修好了
2. 文件操作相关
    - ~~冲突提示会导致已选中的文件暂时在文件列表中消失~~（已修复）
    - 没了
3. 其他潜在问题
