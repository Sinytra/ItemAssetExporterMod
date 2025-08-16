@file:Suppress("PropertyName")

plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev")
}

val mod_id: String by rootProject
val neoforge_version: String by rootProject
val parchment_minecraft: String by rootProject
val parchment_version: String by rootProject

neoForge {
    version = neoforge_version
    // Automatically enable neoforge AccessTransformers if the file exists
    val at = project(":common").file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
    parchment {
        minecraftVersion = parchment_minecraft
        mappingsVersion = parchment_version
    }
    runs {
        configureEach {
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
            // Unify the run config names with fabric
            ideName = "NeoForge ${name.replaceFirstChar { it.uppercase() } + name.substring(1)} (${path})"
        }
        create("client") {
            client()
        }
        create("data") {
            clientData()
        }
        create("server") {
            server()

            programArgument("nogui")
        }

        create("exportClient") {
            client()
            systemProperty("wiki_exporter.enabled", "true")
        }
        create("exportServer") {
            server()
            programArgument("nogui")
            systemProperty("wiki_exporter.enabled", "true")
        }
    }
    mods {
        create(mod_id) {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets.main {
    resources.srcDir("src/generated/resources") 
}
