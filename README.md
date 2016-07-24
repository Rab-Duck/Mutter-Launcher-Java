# Mutter-Launcher-Java

Yet, yet another application lancher for Windows by Java.

## これは何？

自分で Java8/JavaFX 学習用に  
Mutter Launcher <http://hp.vector.co.jp/authors/VA022068/soft/bin/mlnch/>  
を Java に移植しようとしているものです。  
一人プロジェクト＆学習を兼ねているので説明などは不十分ですが、
もし興味あれば覗いてみて下さい。

## とりあえずアプリを動かしてみるには？

build/dist 配下にビルドしたものが置いてあるので、 MutterLauncher.jar で実行できます。
JIntellitype.dll はデフォルトでは 64bit のものなので、  
32bit 環境では JIntellitype32.dll の方を JIntellitype.dll にリネームして下さい。

## プロジェクトを動かすには？

Eclipse + e(fx)clipse<http://www.eclipse.org/efxclipse/index.html> で作っています。  
ソースの文字コードは UTF-8 に設定して下さい。  
Main.java から実行して下さい。

NetBeans の方、ごめんなさい...
ただ、JavaFX 用のプロジェクト設定をして、JIntellitype.jar/dll にパスを通せば行けるのではないかと思われます。

## ブランチについて
develop ブランチで開発をして、適宜 master 側にマージをしています。  
とりあえず動かしてみるには master 側をお勧めします。
