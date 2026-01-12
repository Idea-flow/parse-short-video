前提:jenv local 25

1.
./mvnw spring-boot:build-image -Pnative


注意 artifactId 既镜像名称要全部小写
2.
$ docker run --rm -p 7861:7860 springboot4-graalvm:0.0.1-SNAPSHOT
$ docker run --rm -d -p 7861:7860 springboot4-graalvm:0.0.1-SNAPSHOT




docker run --rm  -p 40203:40203 parse-short-video-graalvm:0.0.1-SNAPSHOT

docker run --rm -d -p 40203:40203 parse-short-video-graalvm:0.0.1-SNAPSHOT

docker.io/library/parse-short-video-graalvm:0.0.1-SNAPSHOT



# 本地可执行文件


mvn -Pnative native:compile

Recommendations:
PGO:  Use Profile-Guided Optimizations ('--pgo') for improved throughput.
FUTR: Use '--future-defaults=all' to prepare for future releases.
AWT:  Use the tracing agent to collect metadata for AWT.
HEAP: Set max heap for improved and more predictable memory usage.
CPU:  Enable more CPU features with '-march=native' for improved performance.
------------------------------------------------------------------------------------------------------------------------
                       8.8s (9.7% of total time) in 1159 GCs | Peak RSS: 4.56GB | CPU load: 7.05
------------------------------------------------------------------------------------------------------------------------
Build artifacts:
/Users/wangpenglong/projects/java/parse-short-video/target/parse-short-video-graalvm (executable)





# 推送到dockerhub

wangpenglong@wangpenglongdeMacBook-Pro ~ % docker images parse-short-video-graalvm
REPOSITORY                  TAG              IMAGE ID       CREATED        SIZE
parse-short-video-graalvm   0.0.1-SNAPSHOT   15abd8cd28f4   46 years ago   143MB


为镜像打标签（重命名）
docker tag parse-short-video-graalvm:0.0.1-SNAPSHOT biliw/parse-short-video-graalvm:0.0.1-SNAPSHOT

推送镜像到 Docker Hub

docker push biliw/parse-short-video-graalvm:0.0.1-SNAPSHOT


docker pull biliw/parse-short-video-graalvm:0.0.1-SNAPSHOT