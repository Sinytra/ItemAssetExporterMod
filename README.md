# Wiki Data Exporter

A minimal mod to render and export item assets from mods.

## Usage

Specify the namespaces for which you want to render items using the `wiki_exporter.render.namespaces` system
property, separated by commas (`,`). When the game finishes loading with the property present, the mod will run its
exports and then automatically shut it down once rendering has completed.
