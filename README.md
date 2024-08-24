# Item Asset Exporter

A minimal mod to render and export item assets from mods.

Uses code from **glisco**'s [Isometric Renders](https://modrinth.com/mod/isometric-renders) mod, licensed under the
[MIT License](https://github.com/gliscowo/isometric-renders/blob/bb34dff4849d4b72289f54715b8f1b45c1882e7a/LICENSE).

## Usage

Specify the namespaces for which you want to render items using the `item_asset_export.render.namespaces` system
property, separated by commas (`,`). When the game finishes loading with the property present, the mod will run its
exports and then automatically shut it down once rendering has completed.

## Credits

- **glisco** for creating the amazing [Isometric Renders](https://modrinth.com/mod/isometric-renders) mod
