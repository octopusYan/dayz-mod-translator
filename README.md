<div style="text-align:center;">

# DayZ Mod Translator

<br>
<div>
    <img alt="JDK" src="https://img.shields.io/badge/JDK-17-%2300599C">
    <img alt="platform" src="https://img.shields.io/badge/platform-Windows-blueviolet">
</div>

[//]: # (<div>)

[//]: # (    <img alt="license" src="https://img.shields.io/github/license/octopusYan/dayz-mod-translator">)

[//]: # (    <img alt="commit" src="https://img.shields.io/github/commit-activity/m/octopusYan/dayz-mod-translator?color=%23ff69b4">)

[//]: # (</div>)

[//]: # (<div>)

[//]: # (    <img alt="stars" src="https://img.shields.io/github/stars/octopusYan/dayz-mod-translator?style=social">)

[//]: # (    <img alt="GitHub all releases" src="https://img.shields.io/github/downloads/octopusYan/dayz-mod-translator/total?style=social">)

[//]: # (</div>)

<br>

使用 JavaFx 编写的 DayZ 游戏mod 汉化GUI工具

</div>

### 使用

- 设置 -> 翻译，选择翻译接口，填写配置信息，点击确定
- 点击左侧打开文件选择需要翻译的模组pbo文件
- 待翻译文本获取完成后，点击右侧翻译按钮
- 翻译完成后，点击打包按钮，选择保存位置

<details><summary>截图</summary>

![Main window start](doc/img/screenshot01.png 'Main application window start')

![Main window open file](doc/img/screenshot02.png 'Main window open file')

</details>

### 本地构建

#### 环境说明

| 名称    | 描述                                                      |
|-------|---------------------------------------------------------|
| 系统环境  | windows11/10                                            |
| JDK版本 | 17                                                      |
| 构建工具  | Mavne                                                   |
| 打包工具  | [JavaPackager](https://github.com/fvarrui/JavaPackager) |

#### 步骤

1. 环境配置
    - 打开 [JavaFx](https://gluonhq.com/products/javafx/) 官网环境下载页面
    - 在下方 Downloads 处
        - `JavaFX version` 选择 `17.0.12[LTS]`
        - `Operating System` 选择 `Windows`
        - `Type` 选择 `jmods`
    - 点击右侧绿色按钮下载解压
    - 将解压文件夹内所有 `.jmod` 后缀名的文件复制到 `jdk环境目录` 的`jmods`文件夹中
2. 下载源代码并使用 [IntelliJ IDEA](https://www.jetbrains.com/zh-cn/idea/download/?section=windows) 打开
   ```bash
   git clone https://github.com/octopusYan/dayz-mod-translator.git
   ```
3. 打包
    - 使用 `IntelliJ IDEA` Maven UI
        - 点击右侧工具栏的`Maven`
        - 展开 `DayzModTranslator\Lifecycle`
        - 点击 package
    - 使用 mvn 命令
        ```bash
        mvn package
        ```

### 可能会用到

吾爱论坛 / 谷歌翻译修复：[谷歌浏览器右键翻译失效了咋办，一键修复，才13KB](https://www.52pojie.cn/thread-1781877-1-1.html)

## 致谢

### 开源库

- [pboman3](https://github.com/winseros/pboman3)：打开、打包和解包 ArmA PBO 文件的工具。