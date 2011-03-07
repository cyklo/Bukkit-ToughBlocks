ToughBlocks
===========

ToughBlocks is a plugin for the Minecraft Bukkit API that makes mining tougher.
When you "destroy" a block with a tool, instead of disappearing instantly you
remove some of its "strength". You then have to mine the block multiple times
over to finally remove it.

The related discussion thread for this plugin is located at
<http://forums.bukkit.org/threads/6546/>

Downloading
-----------

Please note that OtherBlocks contains submodules, so to checkout:

    git clone git://github.com/cyklo/Bukkit-ToughBlocks.git
    cd Bukkit-ToughBlocks
    git submodule update --init

Building
--------

An Ant makefile is included. Building this project requires a copy of
`bukkit.jar` in the top level directory.

    cd Bukkit-ToughBlocks
    wget -O bukkit.jar http://ci.bukkit.org/job/dev-Bukkit/lastSuccessfulBuild/artifact/target/bukkit-0.0.1-SNAPSHOT.jar
    ant
    ant jar