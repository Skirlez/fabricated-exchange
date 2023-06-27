# fabricated-exchange

A Fabric remake of the Minecraft mod Project E/Equivalent Exchange 2.
The mod attaches an "EMC" number value to every item. Some are worth more, some less.
Transmute items and blocks into EMC, and EMC to different items you've obtained before.

The mod is targetting 1.19.4. Once the mod is in a stable, feature complete state, I will port it to 1.19.2, 1.18.2, 
and then 1.16.5. Afterwards it will be upgraded to whatever versions after 1.19.4 that have the most mods.

All of the assets were taken from https://github.com/sinkillerj/ProjectE.
If the author(s) of the assets do not want this project using them, I will take the project 
down until I can replace the assets myself. 

According to the Project E description, the following people deserve credit for the assets:

Magic Banana: https://twitter.com/Magic_Banana

x3n0ph0b3: (No existing social media page I could find)

MidnightLightning: https://github.com/MidnightLightning

# Something isn't working

You're actually wrong. Everything is working. You must be hallucinating.
In any case, open an issue on this repository.

# Contributing

This is my very first Minecraft mod, and so I am unfamiliar with good Fabric mod practices.
If you spot anything that is done incorrectly, unsafely, or that could be done more 
efficiently, please open an issue/pull request a change.

Additionally, as of now, the mod is still under development. So you could also suggest or implement
new features, or old features from previous equivalence mods.

# Project Goals
- Finish the project
- Oh and also do not use any external libraries other than the Fabric API because I, personally, greatly dislike having to download additional jar files

# Implemented Features
- Philosopher's Stone
- Transmutation Table
- Energy Collectors Mk1-Mk3
- EMC Mapper

# NEW Features
- As much EMC as your RAM can store
- Infinitely precise EMC fractions (No more slabs without EMC values!)
- Distinction between "seed" and "custom" EMC values:
  * Seed: The EMC values the EMC mapper uses to infer the EMC values of other items
  * Custom: EMC values that get applied at the end of the mapping process and override any value the mapper might have assigned.
 
