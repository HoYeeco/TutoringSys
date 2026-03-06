#!/bin/bash
set -e

echo "拉取最新代码..."
git pull origin main

echo "停止旧容器..."
docker-compose down

echo "构建并启动新容器..."
docker-compose up -d --build

echo "检查容器状态..."
sleep 5
if ! docker-compose ps | grep -q "Up"; then
    echo "容器启动失败，回滚到上一版本..."
    docker-compose down
    docker-compose up -d   # 使用之前的镜像（如果有）
    exit 1
fi

echo "部署完成！"
