package tc.oc.api.minecraft.model;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.gson.Gson;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import tc.oc.api.docs.virtual.Model;
import tc.oc.minecraft.scheduler.SyncExecutor;
import tc.oc.api.model.ModelMeta;
import tc.oc.api.model.ModelRegistry;
import tc.oc.api.serialization.Pretty;
import tc.oc.api.util.Permissions;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.NestedCommands;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.minecraft.api.command.CommandSender;

public class ModelCommands implements NestedCommands {

    public static class Parent implements Commands {
        @Command(
            aliases = "model",
            desc = "Commands related to API models",
            min = 1,
            max = -1
        )
        @NestedCommand(ModelCommands.class)
        @CommandPermissions(Permissions.DEVELOPER)
        public void model(CommandContext args, CommandSender sender) throws CommandException {}
    }

    private final ModelRegistry registry;
    private final Gson prettyGson;
    private final SyncExecutor syncExecutor;

    @Inject ModelCommands(ModelRegistry registry, @Pretty Gson prettyGson, SyncExecutor syncExecutor) {
        this.registry = registry;
        this.prettyGson = prettyGson;
        this.syncExecutor = syncExecutor;
    }

    private List<String> completeModel(String name) {
        return StringUtils.complete(name, registry.all()
                                                  .stream()
                                                  .filter(meta -> meta.store().isPresent())
                                                  .map(ModelMeta::name));
    }

    private ModelMeta<?, ?> parseModel(@Nullable String name) throws CommandException {
        if(name == null) return null;

        for(ModelMeta<?, ?> meta : registry.all()) {
            if(meta.store().isPresent() && name.equalsIgnoreCase(meta.name())) {
                return meta;
            }
        }
        throw new CommandException("Unknown model '" + name + "'");
    }

    @Command(
        aliases = "list",
        desc = "List all stored models",
        max = 0
    )
    public void list(CommandContext args, CommandSender sender) throws CommandException {
        for(ModelMeta<?, ?> meta : registry.all()) {
            if(meta.store().isPresent()) {
                sender.sendMessage(new Component(meta.name() + " (" + meta.store().get().count() + ")"));
            }
        }
    }

    @Command(
        aliases = "all",
        usage = "<model>",
        desc = "List all stored instances of a model",
        min = 1,
        max = 1
    )
    public List<String> all(CommandContext args, CommandSender sender) throws CommandException {
        final String modelName = args.getString(0, "");

        if(args.getSuggestionContext() != null) {
            return completeModel(modelName);
        }
        for(Model doc : parseModel(modelName).store().get().set()) {
            sender.sendMessage(new Component(doc._id() + " " + doc.toShortString()));
        }
        return null;
    }

    @Command(
        aliases = "show",
        usage = "<model> <id>",
        desc = "Show the contents of a single document",
        min = 2,
        max = 2
    )
    public List<String> show(CommandContext args, CommandSender sender) throws CommandException {
        final String modelName = args.getString(0, "");
        final String id = args.getString(1, "");

        if(args.getSuggestionContext() != null) {
            switch(args.getSuggestionContext().getIndex()) {
                case 0:
                    return completeModel(modelName);

                case 1:
                    final ModelMeta<?, ?> meta = parseModel(modelName);
                    return StringUtils.complete(id, meta.store().get().set().stream().map(Model::_id));

                default:
                    return null;
            }
        }

        final ModelMeta<?, ?> meta = parseModel(modelName);
        final Optional<?> doc = meta.store().get().tryId(id);
        if(!doc.isPresent()) {
            throw new CommandException("No " + meta.name() + " with _id " + id);
        }
        sender.sendMessage(new Component(prettyGson.toJson(doc.get())));
        return null;
    }

    @Command(
        aliases = "refresh",
        usage = "<model>",
        desc = "Refresh a model store from the API",
        min = 1,
        max = 1
    )
    public List<String> refresh(CommandContext args, CommandSender sender) throws CommandException {
        final String modelName = args.getString(0, "");

        if(args.getSuggestionContext() != null) {
            return completeModel(modelName);
        }

        final ModelMeta<?, ?> model = parseModel(modelName);
        sender.sendMessage("Refreshing model " + model.name() + "...");
        syncExecutor.callback(
            model.store().get().refreshAll(),
            response -> sender.sendMessage("Refreshed " + response.documents().size() + " " + model.name() + " document(s)")
        );
        return null;
    }
}
