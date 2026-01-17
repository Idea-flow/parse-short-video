

## 项目概述
这是一个基于 springboot3.5.9版本 的视频解析服务，可以从多个中国社交媒体平台提取视频信息。它能够去除水印并提供来自 2个平台的直接视频 URL，包括抖音、小红书。


## 使用技术规范
 http客户端:org.springframework.web.client.RestClient
 lombok

## 完成功能
### 参考项目-本地项目地址
  这是一个python项目,需要转为springboot项目,项目需要结构清晰,易于维护.
  主文件:/Users/wangpenglong/projects/java/parse-short-video/parse-video-py-main-my/main.py

  解析器模块文件夹:/Users/wangpenglong/projects/java/parse-short-video/parse-video-py-main-my/parser
  只需要完成抖音,小红书的解析即可

## 其他额外要求
 我是一个初学者,请在代码中添加详细注释,方便我学习.





# github镜像打包

参考python的/Users/wangpenglong/projects/java/parse-short-video/parse-video-py-main-my/.github/workflows/docker-my.yml
1. 创建.github/workflows/docker-my.yml文件 完成springboot 这个项目的docker镜像打包和推送到dockerhub
2. 镜像需要支持linux平台 mac系统
3. env:
   image_name_build: biliw
   IMAGE_NAME: parse-short-video:latest





# 创建一个代理接口
   /proxy?url=xxx

创建一个代理接口 /proxy?url=xxx
这个url可能是图片 或者 视频,  是/share/url/parse 接口解析的各大媒体的图片,视频地址,写一个代理接口 来下载这些内容

