1: 注解model - Java Lib
Annotation 为 butter knife , 提供代码生成器所需要的注解

2: 代码生成器 Compiler - Java Lib
从注解model获取信息，通过annotationProcessor或者apt生成代码

3: 核心model - Android Lib
提供一些核心的功能模块，包括配置数据的管理, 网络请求, 路由的架构, 照相与二维码/图片的处理, 通用的工具（网络状态判断等）, WebView的处理， 支付的处理, 文件持久化等

4: 业务model - Android Lib
它依赖于核心模块，实现具体业务中要求的模型。根据业务的不同，比如电商、或者是新闻等需求，独立开发
包括一类的UI,逻辑

5: 具体项目的module模块 - Android Application
只有这个项目使用的第三方库; 特有的项目功能
