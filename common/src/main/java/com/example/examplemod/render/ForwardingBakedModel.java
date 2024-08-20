/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.examplemod.render;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Base class for specialized model implementations that need to wrap other baked models.
 * Avoids boilerplate code for pass-through methods.
 */
public abstract class ForwardingBakedModel implements BakedModel {
	/** implementations must set this somehow. */
	protected BakedModel wrapped;

	@Override
	public List<BakedQuad> getQuads(BlockState blockState, Direction face, RandomSource rand) {
		return wrapped.getQuads(blockState, face, rand);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return wrapped.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return wrapped.isGui3d();
	}

	@Override
	public boolean isCustomRenderer() {
		return wrapped.isCustomRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return wrapped.getParticleIcon();
	}

	@Override
	public boolean usesBlockLight() {
		return wrapped.usesBlockLight();
	}

	@Override
	public ItemTransforms getTransforms() {
		return wrapped.getTransforms();
	}

	@Override
	public ItemOverrides getOverrides() {
		return wrapped.getOverrides();
	}
}
