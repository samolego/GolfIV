# Hey there!
This is super non-production ready!
It contains [STDOUT]s in code that may eat your log.
Just kidding, but really make sure you don't use it in production.
False positives are still a thing, your players might get falsely kicked.

# Ok, I get it, but how do I get the mod?

## Downloading it from GH actions
1. Go to [GH actions](https://github.com/samolego/GolfIV/actions?query=workflow%3Abuild) and click on the latest commit.
2. On the bottom there's a link to the `Artifacts`. Click it to download.
3. Unzip the file and put it in `mods/` folder.

## I'm brave enough to build it myself!

1. Clone the repository.
```
git clone https://github.com/samolego/GolfIV.git
```

2. Go in the GolfIV folder.
3. Run the build script (you might want to omit `./`).
```
./gradlew build
```

**Please** test before using!

# GolfIV
[![Discord](https://img.shields.io/discord/797713290545332235?logo=discord)](https://discord.gg/9PAesuHFnp)

An anticheat attempt for Fabric servers. For more info, check [wiki](https://github.com/samolego/GolfIV/wiki).

Thanks to [Johnan's tutorials](https://www.youtube.com/user/jonhanpvp) for ideas.
