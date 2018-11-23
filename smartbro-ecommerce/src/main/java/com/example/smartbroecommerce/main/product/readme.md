# 产品显示包概述

1: 产品列表页
ListDelegate + ProductListAdaptor 组成

2: 产品详情页
DetailDelegate 整个页面的布局及加载
ProductInfoDelegate 由 DetailDelegate 加载, 掺入Product对象用来专门显示产品的文字相关信息
CircleTextView  专门用来显示一个圆圈，表示选择了几个产品

3: 产品详情页中的Tab内容 主要包括比如产品规格、产品尺寸图、或者评论等...
这部分内容主要由TabPagerAdapter和ImageDelegate结合而生成, 因为假定所有的tab内容，其实就是一些图片