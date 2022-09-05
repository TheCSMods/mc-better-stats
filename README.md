# Better Statistics Screen
This Minecraft mod replaces the Vanilla statistics screen with an new and improved statistics screen that is easier to use and that has more helpful features. The new statistics screen features an easier to navigate and read UI layout, as well as a few filters and a search field that will help you find the statictics you need a lot easier, especially when there are dozens, if not hundreds of entries to keep track of.

The new UI also features two extra statistic tabs, alongside Vanilla's three tabs, those being:
- General - This tab contains all of your general statistics
- Items - This tab contains statistics of all times you interacted with
- Mobs - This tab contains statistics of all mobs you interacted with
- A healthy diet - This tab aims to help you obtain the "A healthy diet" advancement
- Monsters hunted - This tab aims to help you obtain the "Monsters hunted" advancement

Below you can read some extra info on those tabs and see screenshots of what they look like.

### Credits (regarding screenshots and the video)
I decided to test the mod as well as take screenshots of the mod in my survival world from the [Better Minecraft](https://www.curseforge.com/minecraft/modpacks/better-mc-fabric) mod pack, so as to show that the mod works with modded features as well. The screenshots also feature the [Complementary Shaders](https://www.curseforge.com/minecraft/customization/complementary-shaders) being used alongside [Iris](https://modrinth.com/mod/iris), as well as the [BYG](https://modrinth.com/mod/biomesyougo) mod.

### A short video
Here is a short video showcasing the mod and what it looks like (click the image)</br>
</br>
[![Better Statistics Screen showcase](https://img.youtube.com/vi/AaC8J0G238c/0.jpg)](https://www.youtube.com/watch?v=AaC8J0G238c)

### General
This tab does not add nor show anything special regarding general statistics other than the redesigned UI layout. The filters menu features a way for you to hide all general statistics that are currently set to '0', as well as a search bar that will help you look for a specific statistic.

![Screenshot_1](https://user-images.githubusercontent.com/66475965/188211298-0e1f3a45-7745-49ba-b9ef-738d2f8e9356.png)

### Items
The items tab displays items in a visual grid of items, similar to how an inventory screen does it. The items are categorized in item groups, just like they would be in the creative inventory menu. All items you interacted with are shown in this tab. This will hopefully help you find the items you are looking for easier. The search field in the filters menu can be used to look for specific items. To see a statistic for an item, place your cursor over the said item, or use "Tab" to navigate to it using your keyboard, and a tooltip text will display the statistics. The "Show item names" checkbox defines whether or not the tooltip will also show the item name.

![Screenshot_2](https://user-images.githubusercontent.com/66475965/188212375-248c643d-a86f-4426-bd18-97fd0c99e942.png)

### Mobs
Similar to the "Items" tab, the "Mobs" tab will also show mobs in a grid. The mobs are visually rendered on the GUI screen, so it is easier for you to find the mob you are looking for, and because it looks nicer. Just like with the items tab, to see the statistics for a given mob, place your cursor over the said mob, or use "Tab" to navigate to it using your keyboard. A tooltip text will show you the statistics.

![Screenshot_3](https://user-images.githubusercontent.com/66475965/188287966-69108493-a072-40df-adce-753302abce3d.png)

### A balanced diet
This tab aims to help you achieve the "A balanced died" advancement. It does that by displaying all food items in grouped grids, similarly to how the "Items" tab does it. This tab will show you all food items, even the ones you haven't interacted with, and will also highlight the foods you already ate before, helping you identify which foods you need to eat next to earn the advancement. The filters can be used to hide foods you haven't interacted with, as well as look for specific foods with the search field.

Note: I do not know whether or not modded food items count or not, but I have decided to include them on this tab as well. It may also depend on whether or not a given mod will require you to eat all of it's foods for the advancement, or an advancement of it's own.

![Screenshot_4](https://user-images.githubusercontent.com/66475965/188213469-f843e6de-7807-4d01-ba2c-7ee45a76f2c5.png)

### Monsters hunted
This tab aims to help you achieve the "Monsters hunted" advancement. It shows you a grid of mobs similarly to the "Mobs" tab, except the mobs shown on this tab are hostile creatures you have either killed or are yet to kill. Similarly to the "A balanced diet" tab, any mobs you have killed will be highlighted in this tab, helping you easily see which mobs you need to kill next in order to earn the advancement.

Note: Just like in the "A balanced diet" tab, I do not know whether or not modded hostile mobs count or not. It is up to the mods and data packs to dictate that behavior. As such, all hostile mobs will be shown here, including modded ones.

![Screenshot_5](https://user-images.githubusercontent.com/66475965/188287976-d61922e3-e48a-431a-9a3a-3835aaf4b393.png)

### Debug Mode (as of v1.1)
This is a special tab that you can only access by holding down the CTRL key while cycling tabs. I added this tab so I can more easily debug the mod while working on it. You can use any debug features in there yourself as well. As for the "Show everything" option, please keep in mind that it may cause lag spikes when used with large mods and mod-packs because of the amount of items/entities that may end up being shown on the screen. Those lag spikes are the main reason I chose NOT to show all item/mob stats even when the "Hide empty stats" checkbox is unchecked.

# Mod config
To configure the mod, go to the `config` directory, and create the `betterstats.properties` file. In that file, you may define the desired config properties. Below is a list of properties you can use, their default values, and information on what they do.

Notice: Regarding colors, the color properties take integers representing RGB color values that can be obtained using `java.awt.Color.getRGB()`. More info on how to use color properties will be below.

```properties
# This property keeps track of whether or not the user has seen the better statistic screen
# before. It is used for drawing a red dot in the top right corner of the "Statistics" button.
# In other words, if the user never saw the screen before, a small red dot will let them know
# to click on the "Statistics" button.
SEEN_BSS=false

# This one is an extra unused feature I decided to include. At first I was gonna use it, but
# I then changed my mind, as I couldn't come up with a cool texture for the button.
# When turned on, the `betterstats:textures/gui/stats_btn_bg.png` image will be drawn below
# the "Statistics" button's text. You can use this to customize the button if you wish to.
# Don't forget to keep the texture's edges transparent, so as to let the game's button outline
# work properly. You can find the placeholder image you could use in the mod's `assets` folder.
BSS_BTN_IMG=false

# Used to remember whether or not the user hid empty stats using the filter checkbox.
FILTER_HIDE_EMPTY_STATS=false

# Used to remember whether or not the user chose to show item names using the filter checkbox.
FILTER_SHOW_ITEM_NAMES=true

# The text color used in the general statistics tab (used in some other places as well such
# as rendering mob names as replacement for mob models in mob tabs, not used in tooltips)
COLOR_STAT_GENERAL_TEXT=-1

# The background color of a statistic entry. Any entry, from any tab.
COLOR_STAT_BG=599045300

# The outline color used for when you hover over a given statistic entry
COLOR_STAT_OUTLINE=-4144960

# The background color of the filters tab and the stats tab
COLOR_CONTENTPANE_BG=2013265920

# The color used for category names when statistic entries are categorized
COLOR_CATEGORY_NAME_NORMAL=-922747136

# Same as COLOR_CATEGORY_NAME_NORMAL except this is when the names are "glowing"/"brighter"
COLOR_CATEGORY_NAME_HIGHLIGHTED=-256
```

As for obtaining integer numbers for given RGBA values, I have created a Java code snippet on the online compiler site that you can use right now to obtain the integer for any color you wish to use. You can see it by [clicking here](http://tpcg.io/_YRQJVE).

## betterstats_mobStatRenderer.json (as of v1.1+)
(Available as of v1.1)<br/>
When rendering mob statistics, the mod tries it's best to position and scale the mob models correctly so as to make them fit the GUI box properly. However, not every mob model behaves the same, and as such, some modded mobs may appear positioned inappropriately, despite the mod's efforts to make it positioned and scaled appropriately.

If you are someone who uses this mod, or a mod-pack creator/developer, you can use this config file to tell the mod how it should render certain entities. I have already tweaked some Vanilla entities to make them positioned appropriately. You can always override those tweaks using the config file.

To get started, create the `betterstats_mobStatRenderer.json` file in the `config` directory, make sure it's contents are at least `{}` to avoid errors, and make sure you use the appropriate JSON syntax. Below is an example configuration:
```json
{
    "minecraft:axolotl": { "MobStatGuiPosOffset": [10,-10] },
    "minecraft:ghast": { "MobStatGuiPosOffset": [0,-25] },
    "minecraft:spider": {  "MobStatGuiSize": 95 },
    "minecraft:cave_spider": {  "MobStatGuiSize": 80 },
    "minecraft:squid": {  "MobStatGuiPosOffset": [0,-25] },
    "minecraft:glow_squid": { "MobStatGuiPosOffset": [0,-25] },

    "minecraft:phantom": { "MobStatGuiSize": 60, "MobStatGuiPosOffset": [0,-20] },
    "minecraft:turtle": { "MobStatGuiSize": 70, "MobStatGuiPosOffset": [0,0] },

    "minecraft:ender_dragon": { "MobStatGuiSize": 300, "MobStatGuiPosOffset": [0,-20] },

    "minecraft:slime": { "MobStatGuiSize": 400 },
    "minecraft:magma_cube": { "MobStatGuiSize": 400 },
    "minecraft:pufferfish": { "MobStatGuiSize": 150 }
}
```
Each entry key is an `Identifier` for a given `LivingEntity`, for example `"minecraft:pig": { ... }`. Each entry can have certain properties defining how the entity in question will be rendered on a GUI mob statistic. Below is the list of those properties:

### MobStatGuiSize (Integer)
Before rendering mobs, the mod attempts to calculate which size it should use when rendering a given mob. This integer is a percentage value that multiplies the mod's chosen size number by `MobStatGuiSize / 100`. So for example, if the mod chose to render a mob using the size of `30`, and the value of this property was let's say `200`, then the final rendering size would be `30 * (200 / 100) = 60`, aka `60`.<br/>
Example: `"MobStatGuiSize": 100`

### MobStatGuiPosOffset (Integer array with the length of 2)
This property offsets a given entity's GUI position relative to the mod's chosen position at which to render a mob at. The mod attempts to render a mob near the center of it's GUI statistic box. The X and Y values are also percentages, and those percentages are relative to the size of the GUI box. In v1.1, the size of the box is `50`, so a value of let's say `X = 100` would offset the entity by `50` in the X axis, because `50 * (100 / 100) = 50`, and so the final value would be `50 (chosen by the mod) + 50 (custom offset) = 100`. It is not recommended to use values `> 100 or < -100`.<br/>
Example: `"MobStatGuiPosOffset": [0,-20]`

##
<p align=center>
  <a href="https://www.curseforge.com/minecraft/mc-mods/better-stats"><img alt="CurseForge" src="https://cf.way2muchnoise.eu/667464.svg"/></a>
  <a href="https://modrinth.com/mod/better-stats"><img alt="Modrinth" src="https://img.shields.io/modrinth/dt/n6PXGAoM?label=Modrinth"></a>
</p>
