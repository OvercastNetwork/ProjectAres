package tc.oc.commons.core.commands;

import tc.oc.commons.core.inject.HybridManifest;

public class CommandsManifest extends HybridManifest {

    @Override
    protected void configure() {
        bind(CommandExceptionHandler.Factory.class);
        bind(GuiceInjectorAdapter.class);

        new CommandBinder(binder());

        requestStaticInjection(CommandFutureCallback.class);
    }
}
