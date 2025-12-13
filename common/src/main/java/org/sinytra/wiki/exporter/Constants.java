package org.sinytra.wiki.exporter;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {
	public static final String MOD_ID = "wiki_exporter";
	public static final String MOD_NAME = "Wiki Data Exporter";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static Identifier location(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}