# simple-react-native-hot-code-push
## 摘要
实现了React Native 的 Android版本客户端热更新机制。
区别于**Microsoft**提供的CodePush和**React Native中文网**提供的代码热更新服务，本项目的更新流程都是由Native层代码进行控制。

## 背景
目前，大家考虑使用React Native 技术的关键点主要有个三个:
1. iOS和Android程序可以使用统一的语言，并且部分代码可以实现共用；
2. 热更新能力，无需发布版本即可实现升级、bug修复等内容
3. 体验接近原生App

针对于第2点，**MicroSoft**和**React Native**中文网都提供了相似的解决方案，然而他们的方案就将jsbundle的管理放置在了自己发服务器上，并且由于js代码本身就是源码的形式提供出去的，放到别人的服务器上**安全性**难以保障；同时两者将热更新服务写在了js代码中，这意味着必须先加载了jsbundle，更新服务才能进行使用，那么为了安全，必须保障加载前的jsbundle是合法的、安全的。基于以上两点的考虑，本项目抛弃了将更新服务器罗写在js代码中的实现方案，而将其在native层实现。

## 功能特点
1. 适合于将React Native用于某个子业务的场景，即由Native的页面事件调起整个React Native应用；
2. 使用简单，在Native页面的适合时机调用`MMCodePush.checkForUpdate(...)`即可完成更新检查和下载任务，启动React Native时会自动检查是否下载完成等内容，Native只需要将业务id通过Intent传给MMBaseActivity即可；
```java
Intent intent = new Intent(mContext, DemoRNActivity.class);
intent.putExtra(MMCodePushConstants.KEY_BUSINESS_ID, "AAF047B7-E816-2AE0-949A-D5FB4CE40245");
mContext.startActivity(intent);
```
3. 使用laoding页面解决React Native加载前的白屏问题，等待加载完成再消除loading界面；
4. 使用了common包 + n个业务小包的机制，减少了下载量，同时使用了bsdiff合成完整包时，完整包用完即删除，落脚地时间极短（可在一定程度上保护jsbundle文件，结合文件加密等措施可以更安全地保护文件）

## 使用方法
1. 先启动服务器，地址https://github.com/MarcusMa/simple-react-native-hot-code-push-server
2. 使用 android studio 导入项目中的android工程，编译运行；
> 注意修改MMCodePush的访问地址,如下：
```java
String mServerUrl = "http://172.20.143.41:8888";
codePush = new MMCodePush(this, mServerUrl, true);
```
3. 若要测试新包的发布，请参考服务器的发布说明

## 后续更新
1. 可添加对jsbundle包的加密措施；
2. 可添加预加载功能，提高加载时间；