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

            property("wiki_exporter.render.namespaces", "minecraft")
            property("wiki_exporter.render.outputs.gif", "true")
            property("wiki_exporter.render.outputs.png", "true")
            property("wiki_exporter.render.output", file("runs/client/.assets/item").absolutePath)
        }
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir("runs/server")
        }
    }
}
