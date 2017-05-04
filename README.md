# simple-react-native-hot-code-push
## 摘要
实现了React Native 的 Android版本客户端热更新机制。
区别于**Microsoft**提供的CodePush和**React Native中文网**提供的代码热更新服务，本项目的更新流程都是由Native层代码进行控制。

## 背景
目前，大家考虑使用React Native 技术的关键点主要有个三个:
1. iOS和Android程序可以使用统一的语言，并且部分代码可以实现共用；
2. 热更新能力，无需发布版本即可实现升级、bug修复等内容
3. 体验接近原生App

正对于第2点，**MicroSoft**和**React Native**中文网都提供了相似的解决方案，然而他们的方案就将jsbundle的管理放置在了自己发服务器上，并且由于js代码本身就是
