package com.github.xpenatan.gdx.teavm.backends.minigame.config.plugins;

import org.teavm.jso.impl.JSOPlugin;
import org.teavm.vm.spi.Before;
import org.teavm.vm.spi.TeaVMHost;
import org.teavm.vm.spi.TeaVMPlugin;

/**
 * TeaVM plugin for the minigame backend.
 * Installs class transformer for @Emulate annotations and JavaObjectExporter.
 */
@Before(JSOPlugin.class)
public class MiniGamePlugin implements TeaVMPlugin {

    @Override
    public void install(TeaVMHost host) {
        host.add(new MiniGameClassTransformer());
        host.add(new JavaObjectExporterDependency());
    }
}
