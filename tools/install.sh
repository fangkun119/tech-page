!/bin/bash

# 前置条件
# 1. 安装homebrew
# 
# 2. 安装ruby
# 
# 3. bundler 2.6.8并配国内源
# gem sources --remove https://rubygems.org/
# gem sources --add https://mirrors.tuna.tsinghua.edu.cn/rubygems/
# gem install bundler:2.6.8
# bundle config mirror.https://rubygems.org https://mirrors.tuna.tsinghua.edu.cn/rubygems
#
# 4. 安装rbenv
# brew install rbenv ruby-build
# 
# 5. 创建3.3.0本地环境
# rbenv init
# rbenv install 3.3.0

rbenv local 3.3.0 && bundle install