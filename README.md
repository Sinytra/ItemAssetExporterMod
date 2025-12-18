# Wiki Data Exporter

A minimal mod to render and export item assets and other useful information from mods.

Made for [Modded Minecraft Wiki](https://moddedmc.wiki) authors.

## Installation

```groovy
repositories {
    maven {
        url "https://maven.sinytra.org"
    }
}

dependencies {
    // ModDevGradle
    // Version list: https://maven.sinytra.org/org/sinytra/wiki-exporter-neoforge
    runtimeOnly "org.sinytra:wiki-exporter-neoforge:<version>"

    // Fabric Loom
    // Version list: https://maven.sinytra.org/org/sinytra/wiki-exporter-fabric
    modRuntimeOnly "org.sinytra:wiki-exporter-fabric:<version>"
}
```

## Usage

### Configure the exporter

The exporter is configured using a JSON file following the format described in `config.example.jsonc`.
When copying this file, please remove the comments and use the `.json` extension instead.

By default, the exporter will look for the configuration file at `run/wiki_exporter/config.json`, but you can define
your own file path using the `wiki_exporter.config.path` system property. See below for details.

Example configuration:

```json5
{
  // (optional) Output base path, relative to the "run" directory
  "output_path": "wiki_exporter",
  // Enabled exporter modules
  "enabled": [
    "metadata",
    "render"
  ],
  // Per-module config
  "modules": {
    "render": {
      // In-game ID namespaces to export from
      "namespaces": [
        "minecraft"
      ],
      // Export file resolution in px
      "resolution": 128,
      // Enable static texture exports
      "png": true
    }
  }
}
```

### Enable the exporter

The exporter will only run when the `wiki_exporter.enabled` system property is set to `true`.
You should configure it individually per each run configuration as desired.
We recommend creating a separate run configuration for the exporter.

The exporter will run automatically when the game launches, and will close it once all exports are complete.

Certain modules, such as `render`, will only run on client instances.

Example configuration using NeoForge ModDevGradle:

```groovy
neoForge {
    runs {
        exportClient {
            client()
            systemProperty 'wiki_exporter.enabled', 'true'
        }
        exportServer {
            server()
            programArgument 'nogui'
            systemProperty 'wiki_exporter.enabled', 'true'
        }
    }
}
```

## Reference

### Available modules

| ID         | Description                                                                           | Side   |
|------------|---------------------------------------------------------------------------------------|--------|
| `render`   | Exports GUI renders of specified items/blocks                                         | CLIENT |
| `metadata` | Exports item and block properties such as stack size, hardness, blast resistance etc. | SERVER |

### Available system properties

| Name                                      | Description                             | Default value                     |
|-------------------------------------------|-----------------------------------------|-----------------------------------|
| `wiki_exporter.enabled`                   | Enable exporter                         | `false`                           |
| `wiki_exporter.config.path`               | Path to exporter configuration file.    | `<run>/wiki_exporter/config.json` |
| `wiki_exporter.output.path`               | Path to exporter base output directory. | `<run>/wiki_exporter/output`      |
| `wiki_exporter.module.<name>.output.path` | Per-module output directory override.   | -                                 |
