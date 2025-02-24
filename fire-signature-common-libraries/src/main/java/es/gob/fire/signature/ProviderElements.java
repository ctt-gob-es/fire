package es.gob.fire.signature;

import java.util.ArrayList;
import java.util.List;

public class ProviderElements {

	private static final String VALUES_SEPARATOR = ","; //$NON-NLS-1$

	public static ProviderElement[] parse(final String providers) {
		if (providers == null) {
			return new ProviderElement[0];
		}

		final List<ProviderElement> providersList = new ArrayList<>();
		final String[] providersTempList = providers.split(VALUES_SEPARATOR);
		for (final String provider : providersTempList) {
			if (provider != null && !provider.trim().isEmpty()) {
				final ProviderElement prov = new ProviderElement(provider);
				if (!providersList.contains(prov)) {
					providersList.add(prov);
				}
			}
		}
		return providersList.toArray(new ProviderElement[providersList.size()]);
	}
}
