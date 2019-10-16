/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.automation.internal.provider.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.I18nUtil;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.Condition;
import org.openhab.core.automation.Module;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.util.ModuleBuilder;
import org.osgi.framework.Bundle;

/**
 * This class is used as utility for resolving the localized {@link Module}s. It automatically infers the key if the
 * default text is not a constant with the assistance of {@link TranslationProvider}.
 *
 * @author Ana Dimova - Initial contribution
 */
@NonNullByDefault
public class ModuleI18nUtil {

    public static <T extends Module> List<T> getLocalizedModules(TranslationProvider i18nProvider, List<T> modules,
            @Nullable Bundle bundle, String uid, String prefix, Locale locale) {
        List<T> lmodules = new ArrayList<>();
        for (T module : modules) {
            String label = getModuleLabel(i18nProvider, bundle, uid, module.getId(), module.getLabel(), prefix, locale);
            String description = getModuleDescription(i18nProvider, bundle, uid, prefix, module.getId(),
                    module.getDescription(), locale);
            @Nullable
            T lmodule = createLocalizedModule(module, label, description);
            if (lmodule != null) {
                lmodules.add(lmodule);
            }
        }
        return lmodules;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Module> @Nullable T createLocalizedModule(T module, @Nullable String label,
            @Nullable String description) {
        if (module instanceof Action) {
            return (T) createLocalizedAction((Action) module, label, description);
        }
        if (module instanceof Condition) {
            return (T) createLocalizedCondition((Condition) module, label, description);
        }
        if (module instanceof Trigger) {
            return (T) createLocalizedTrigger((Trigger) module, label, description);
        }
        return null;
    }

    private static Trigger createLocalizedTrigger(Trigger module, @Nullable String label,
            @Nullable String description) {
        return ModuleBuilder.createTrigger(module).withLabel(label).withDescription(description).build();
    }

    private static Condition createLocalizedCondition(Condition module, @Nullable String label,
            @Nullable String description) {
        return ModuleBuilder.createCondition(module).withLabel(label).withDescription(description).build();
    }

    private static Action createLocalizedAction(Action module, @Nullable String label, @Nullable String description) {
        return ModuleBuilder.createAction(module).withLabel(label).withDescription(description).build();
    }

    private static @Nullable String getModuleLabel(TranslationProvider i18nProvider, @Nullable Bundle bundle,
            String uid, String moduleName, @Nullable String defaultLabel, String prefix, @Nullable Locale locale) {
        String key = I18nUtil.stripConstantOr(defaultLabel, () -> inferModuleKey(prefix, uid, moduleName, "label"));
        return i18nProvider.getText(bundle, key, defaultLabel, locale);
    }

    private static @Nullable String getModuleDescription(TranslationProvider i18nProvider, @Nullable Bundle bundle,
            String uid, String prefix, String moduleName, @Nullable String defaultDescription,
            @Nullable Locale locale) {
        String key = I18nUtil.stripConstantOr(defaultDescription,
                () -> inferModuleKey(prefix, uid, moduleName, "description"));
        return i18nProvider.getText(bundle, key, defaultDescription, locale);
    }

    private static String inferModuleKey(String prefix, String uid, String moduleName, String lastSegment) {
        return prefix + uid + ".input." + moduleName + "." + lastSegment;
    }
}
