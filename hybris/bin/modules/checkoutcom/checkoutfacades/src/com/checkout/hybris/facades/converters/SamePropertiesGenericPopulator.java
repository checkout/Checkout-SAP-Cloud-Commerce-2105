package com.checkout.hybris.facades.converters;

import de.hybris.platform.converters.Populator;
import org.modelmapper.ModelMapper;


public class SamePropertiesGenericPopulator<S, T> implements Populator<S, T> {
	private final ModelMapper mapper = new ModelMapper();

	@Override
	public void populate(final S s, final T t) {
		mapper.map(s, t);
	}

}
