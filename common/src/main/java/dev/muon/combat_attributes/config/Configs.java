package dev.muon.combat_attributes.config;

import me.fzzyhmstrs.fzzy_config.api.ConfigApi;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;

import java.util.function.Supplier;

/**
 * Central access point and registration hook for the mod's FzzyConfig instances.
 *
 * <p>Three configs are registered on mod init, split by how each is loaded and synced:
 * <ul>
 *   <li>{@link #CLIENT} — {@link RegisterType#CLIENT}: local-only preferences</li>
 *   <li>{@link #SERVER} — {@link RegisterType#SERVER}: server-only, never synced</li>
 *   <li>{@link #SYNC}   — {@link RegisterType#BOTH}: server-authoritative, synced to clients</li>
 * </ul>
 *
 * <p>After {@link #register()} runs, read values anywhere via e.g.
 * {@code Configs.SYNC.featureEnabled.get()}.
 */
public final class Configs {

    public static ConfigClient CLIENT;
    public static ConfigServer SERVER;
    public static ConfigSync SYNC;
    public static ConfigAttributes ATTRIBUTES;

    private Configs() {}

    /**
     * Registers and loads all configs. Safe to call from common init on both loaders;
     * FzzyConfig handles dist-appropriate gating via {@link RegisterType}.
     *
     * <p>Idempotent: a second call is a no-op. This matters on Fabric, where attribute
     * registration may be triggered eagerly from a mixin (to beat {@code DefaultAttributes}
     * static-init), and the trampoline class re-enters this method before the regular
     * {@code onInitialize} path does.
     */
    public static synchronized void register() {
        if (ATTRIBUTES != null) return;
        // Supplier casts disambiguate from the Kotlin Function0 overload.
        CLIENT = ConfigApi.registerAndLoadConfig((Supplier<ConfigClient>) ConfigClient::new, RegisterType.CLIENT);
        SERVER = ConfigApi.registerAndLoadConfig((Supplier<ConfigServer>) ConfigServer::new, RegisterType.SERVER);
        SYNC = ConfigApi.registerAndLoadConfig((Supplier<ConfigSync>) ConfigSync::new, RegisterType.BOTH);
        ATTRIBUTES = ConfigApi.registerAndLoadConfig((Supplier<ConfigAttributes>) ConfigAttributes::new, RegisterType.BOTH);
    }
}
