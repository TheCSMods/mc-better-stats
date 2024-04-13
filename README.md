## Better Statistics Screen

This Minecraft mod replaces the Vanilla statistics screen with an new and improved statistics screen that is easier to use and that has more helpful features. The new statistics screen features an easier to navigate and read UI layout, as well as a few filters and a search field that will help you find the statictics you need a lot easier, especially when there are dozens, if not hundreds of entries to keep track of.

### Dependencies (v3.9.4+)
Note that "client" refers to installing mods to the game launched through a Minecraft launcher, whereas "server" refers to installing mods to a server you join through the "multiplayer" menu.
- **Installing this mod (BSS)**
    - **Client**: Optional
    - **Server**: Optional
- [TCDCommons API](https://modrinth.com/mod/Eldc1g37)
    - **Client**: Optional
    - **Server**: Optional
- [Roughly Enough Items (REI)](https://modrinth.com/mod/nfn13YXA)
    - **Client**: Optional
    - **Server**: Not needed
- [Mod menu](https://modrinth.com/mod/mOgUt4GM)
    - **Client**: Optional
    - **Server**: Not needed

## Features
This mod aims to improve on the existing features, as well as add additional features that may futher be helpful in tracking and managing one's statistics.  
  
Note that any optional features that this mod adds to the game, such as the "/statistics" command and the "Stat announcement system", may be disabled in the config or though the in-game mod settings menu.

### The `/statistics` command
**Alt command:** `/stats`  
This is an optional feature that adds a command that allows people (with sufficient permission levels, such as OP) to manage and view player statistics through commands. This feature may also be useful for writing datapacks that may wanna read as well as manage player statistics.  

*Note: Can be disabled in the config.*

**The command syntax is the following:**
- `/statistics clear <targets>`
- `/statistics edit <targets> <stat_type> <stat> (set|increase) <value>`
- [ex. general stat]: `/statistics edit @s minecraft:custom minecraft:jump set 123`
- [ex. item stat]: `/statistics edit @s minecraft:used minecraft:wooden_shovel set 123`
- [ex. mob stat]: `/statistics edit @s minecraft:killed minecraft:zombie set 123`

### The "Stat announcement system"
This is an optional server-sided feature that broadcasts special messages when players do certain things or achieve certain stats or "milestones". As of `v3.9.7`, the SAS announces when players achieve certain stats for the first time, such as for example "Mining their first diamond", or "Crafting their first item of a specific type", and so on..  
  
*Note: Can be disabled in the config.*  
  
![The SAS in action](https://github.com/TheCSMods/mc-better-stats/assets/66475965/3467ecae-5393-4fa9-9bf3-5146cc07f7b7)

### The "General" statistics tab
This tab does not add nor show anything special regarding general statistics other than the redesigned UI layout. The filters menu features a way for you to hide all general statistics that are currently set to '0', as well as a search bar that will help you look for a specific statistic.  
  
![General statistics tab](https://cdn.modrinth.com/data/n6PXGAoM/images/f612dcb73d9e4ffc4ca0494559a31221396c2435.png)

### The "Items" statistics tab
The items tab displays items in a visual grid of items, similar to how an inventory screen does it. The items are categorized in item groups, just like they would be in the creative inventory menu. All items you interacted with are shown in this tab. This will hopefully help you find the items you are looking for easier. The search field in the filters menu can be used to look for specific items. To see a statistic for an item, place your cursor over the said item, or use "Tab" to navigate to it using your keyboard, and a tooltip text will display the statistics. The "Show item names" checkbox defines whether or not the tooltip will also show the item name.  
  
![Item statistics tab](https://cdn.modrinth.com/data/n6PXGAoM/images/f612dcb73d9e4ffc4ca0494559a31221396c2435.png)

### The "Mobs" statistics tab
Similar to the "Items" tab, the "Mobs" tab will also show mobs in a grid. The mobs are visually rendered on the GUI screen, so it is easier for you to find the mob you are looking for, and because it looks nicer. Just like with the items tab, to see the statistics for a given mob, place your cursor over the said mob, or use "Tab" to navigate to it using your keyboard. A tooltip text will show you the statistics.  
  
*Note that due to technical limitations in my programming, some mobs may fail to render properly, and some mobs may even appear as "plain text" instead of their 3D model due to errors in my rendering method.*  
  
![Mob statistics tab](https://cdn.modrinth.com/data/n6PXGAoM/images/f612dcb73d9e4ffc4ca0494559a31221396c2435.png)

### The "Food & drinks" statistics tab (formerly "A balanced diet")
This tab aims to help you achieve the "A balanced died" advancement. It does that by displaying all food items in grouped grids, similarly to how the "Items" tab does it. This tab will show you all food items, even the ones you haven't interacted with, and will also highlight the foods you already ate before, helping you identify which foods you need to eat next to earn the advancement.  
  
*Note that this tab may not precisely tell you exactly which foods you do and do not need, as that kind of criteria is controlled "server-side", and may be modified by other mods and data-packs. As such, all foods are shown instead.*  

![Food & drinks statistics tab](https://cdn.modrinth.com/data/n6PXGAoM/images/9e3478d0573276349e9842337de3cf598dcf75db.png)

### The "Hostile creatures" statistics tab (formerly "Monsters Hunted")
This tab aims to help you achieve the "Monsters hunted" advancement. It shows you a grid of mobs similarly to the "Mobs" tab, except the mobs shown on this tab are hostile creatures you have either killed or are yet to kill. Similarly to the "Food & drinks" tab, any mobs you have killed will be highlighted in this tab, helping you easily see which mobs you need to kill next in order to earn the advancement.  
  
*Note that just like with "Food & drinks", the criteria for getting the advancement is controlled server-side, and may be modified by other mods and data-packs. As such, all hostile creatures are always shown.*  

![Hostile creatures statistics tab](https://cdn.modrinth.com/data/n6PXGAoM/images/fe98cfce86319282774228f4f4c81748eaa00139.png)

### In-game config menu
A menu that lets you configure the mod in-game. Nothing more or less.  

![In-game config menu](https://cdn.modrinth.com/data/n6PXGAoM/images/7413856bcf4e799b7e265bcf732f17cbb1e2e7c2.png)

### MCBS files
This is an additional feature that aims to let people save a "snapshot" of their current statistics, into a file, which can then be opened later or shared with other players.  
  
*Note that opening an MCBS file does not "override" you current stats, as it only shows you what your stats looked like at the time you saved the MCBS file.*

![Saving a file](https://cdn.modrinth.com/data/n6PXGAoM/images/315ca6da7dcb943b3435c6a11917393786ae4367.png)
![Opening a file](https://cdn.modrinth.com/data/n6PXGAoM/images/5f1855efceb8870339456bb1d3e8235d795f3cfe.png)

### The statistics hud
This is another additional feature that allows you to "pin" any stat to the "in-game-hud", where you can then look at any statistic of your choosing as you play the game. To pin a stat, simply right-click it with your mouse (hold down shift as well if you have REI installed).  

*Note that due to technical limitations, pinned statistics update occasionally by default, and will not update "live" unless this mod is installed on the server-side as well and the "live stat updates" is enabled on the client-side.*  

![Pinning stats](https://cdn.modrinth.com/data/n6PXGAoM/images/3c7e15962f4c8a2268bc5da9a2e7d97ae7850fb6.png)
![Configuring pinned stats](https://cdn.modrinth.com/data/n6PXGAoM/images/681f25b72304d0fdde71ab3104d51fab506c3c34.png)
![What pinned stats look like during gameplay](https://cdn.modrinth.com/data/n6PXGAoM/images/fd7f29e99ed6b73981b2e2cbcdb4eddb93a2bbcb.png)

## Some QnA
- What about Minecraft Forge?
    - I have no plans on making future ports to Forge or any other mod loaders. See [GitHub Issue #100](https://github.com/TheCSMods/mc-better-stats/issues/100) for more info. However, not all hope is lost! [Sinytra Connector](https://modrinth.com/mod/u58R1TMW) is a mod that lets people run Fabric mods on Forge! Please check it out. Also keep in mind the mod is in beta (as of me writing this), so please don't be harsh towards the mod's developers while expecting the mod to work 100% of the time.
- Port to older Minecraft versions?
    - Nope. Same reason as the 'Minecraft Forge' one.
- I can't pin stats to the hud. I right-click them, but REI shows up instead. How do I pin stats?
    - Hold down the left shift button while right clicking a stat. That is, if you have REI installed.

## Credits
Thank you to everyone who contributed to the project and helped translate the mod to other languages! And a special thank **you**, the person reading this, for choosing to use this mod! (Well, thank you even if you're just checking out the mod and not actually using it. That also means a lot!)

### The showcase YouTube video I recorded
I decided to test the mod as well as take screenshots and video footage of the mod in my survival world from the [Better Minecraft](https://www.curseforge.com/minecraft/modpacks/better-mc-fabric) mod pack, so as to show how this mod works and that it works with modded features as well. The screenshots and the video footage also feature the [Complementary Shaders](https://www.curseforge.com/minecraft/customization/complementary-shaders) being used alongside [Iris](https://www.curseforge.com/minecraft/mc-mods/irisshaders), as well as the [BYG](https://www.curseforge.com/minecraft/mc-mods/oh-the-biomes-youll-go) mod.

### The screenshots I took
Resource packs and shader packs used at the time of screenshotting.
- [Complementary shaders](https://www.complementary.dev/shaders/)
- [3D Default](https://modrinth.com/resourcepack/5aPp18Lx)
- [3D crops Revamped](https://modrinth.com/resourcepack/PgpTtNoI)
- [Default Dark Mode](https://modrinth.com/resourcepack/6SLU7tS5)

Sorry if I missed any. I had too many of them applied at once.
