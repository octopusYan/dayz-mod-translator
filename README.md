<div style="text-align:center;">

# DayZ Mod Translator

![JDK](https://img.shields.io/badge/JDK-21-%2300599C)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.4-%2300599C)](https://openjfx.io/)
![platform](https://img.shields.io/badge/platform-Windows-blueviolet)
<br>
[![license](https://img.shields.io/github/license/octopusYan/dayz-mod-translator)](https://github.com/octopusYan/dayz-mod-translator)
![commit](https://img.shields.io/github/commit-activity/m/octopusYan/dayz-mod-translator?color=%23ff69b4)
<br>
![stars](https://img.shields.io/github/stars/octopusYan/dayz-mod-translator?style=social)
![GitHub all releases](https://img.shields.io/github/downloads/octopusYan/dayz-mod-translator/total?style=social)

<br>

使用JavaFx编写的DayZ/ArmA游戏模组汉化工具

</div>

### 使用

- 设置 -> 翻译，选择翻译接口，填写配置信息，点击确定
- 点击打开文件按钮选择需要翻译的模组pbo文件
- 等待待可翻译文本获取完成，点击右侧翻译按钮
- 翻译完成后，点击打包按钮，选择保存位置

<details open>
<summary>截图</summary>

![start](doc/img/screenshot01.png 'start')
![open file](doc/img/screenshot02.png 'open file')
![edit](doc/img/screenshot03.png 'edit')

</details>

### 本地构建

#### 环境说明

| 名称   | 描述                                                      |
|------|---------------------------------------------------------|
| 系统环境 | windows 10/11                                           |
| JDK  | 21                                                      |
| 构建工具 | Maven                                                   |
| 打包工具 | [JavaPackager](https://github.com/fvarrui/JavaPackager) |

#### 本地运行

1. 克隆代码
   ```bash
   git clone https://github.com/octopusYan/dayz-mod-translator
   ```
2. 运行
   ```bash
   mvn clean javafx:run -Pdev
   ```

#### 打包

1. 克隆代码
   ```bash
   git clone https://github.com/octopusYan/dayz-mod-translator
   ```
2. 运行
   ```bash
   mvn clean package -Pbuild
   ```

### 可能会用到

吾爱论坛 / 谷歌翻译修复：[谷歌浏览器右键翻译失效了咋办，一键修复，才13KB](https://www.52pojie.cn/thread-1781877-1-1.html)

### 依赖/引用的项目

<figure>

|                                                                             |                          |
|-----------------------------------------------------------------------------|--------------------------|
| [PBO Manager](https://github.com/winseros/pboman3)                          | 打开、打包和解包 ArmA PBO 文件的工具。 |
| [JavaFX](https://openjfx.io/)                                               | Java 桌面开发                |
| [AtlantaFX](https://mkpaz.github.io/atlantafx/)                             | JavaFX CSS 主题集合          |
| [JavaPackager](https://github.com/fvarrui/JavaPackager)                     | 打包插件                     |
| [Apache Commons](https://commons.apache.org/proper/commons-exec/index.html) | 工具包                      |
| [SLF4J](https://slf4j.org/)                                                 | 日志工具                     |

</figure>