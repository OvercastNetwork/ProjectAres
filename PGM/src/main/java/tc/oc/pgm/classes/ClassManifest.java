package tc.oc.pgm.classes;

import tc.oc.commons.core.commands.CommandBinder;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.map.StaticMethodMapModuleFactory;

public class ClassManifest extends HybridManifest {
    @Override
    protected void configure() {
        install(new StaticMethodMapModuleFactory<ClassModule>(){});
        new CommandBinder(binder()).register(ClassCommands.class);
    }
}
