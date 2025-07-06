@file:Suppress("PropertyName")

plugins {
    id("multiloader-loader")
    id("fabric-loom")
}

val mod_id: String by rootProject
val minecraft_version: String by rootProject
val fabric_loader_version: String by rootProject
val fabric_version: String by rootProject
val parchment_minecraft: String by rootProject
val parchment_version: String by rootProject

dependencies {
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${parchment_minecraft}:${parchment_version}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${fabric_loader_version}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_version}")
}

loom {
    accessWidenerPath = file("src/main/resources/${mod_id}.accesswidener")
    mixin {
        defaultRefmapName.set("${mod_id}.refmap.json")
    }
    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("runs/client")
        }
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir("runs/server")
        }

        create("exportClient") {
            client()
            runDir("runs/client")
            property("wiki_exporter.enabled", "true")
            property("wiki_exporter.config.path", file("runs/wiki_exporter/config.json").absolutePath)
        }
        create("exportServer") {
            server()
            runDir("runs/server")
            programArg("nogui")
            property("wiki_exporter.enabled", "true")
            property("wiki_exporter.config.path", file("runs/wiki_exporter/config.json").absolutePath)
        }
    }
}
