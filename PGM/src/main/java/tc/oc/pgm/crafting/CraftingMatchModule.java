package tc.oc.pgm.crafting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.inventory.Recipe;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.utils.MaterialPattern;
import javax.inject.Inject;

public class CraftingMatchModule extends MatchModule {

    @Inject private Server server;

    private final Set<Recipe> customRecipes;
    private final Set<MaterialPattern> disabledRecipes;

    public CraftingMatchModule(Match match, Set<Recipe> customRecipes, Set<MaterialPattern> disabledRecipes) {
        super(match);
        this.customRecipes = customRecipes;
        this.disabledRecipes = disabledRecipes;
    }

    @Override
    public void enable() {
        super.enable();

        List<Recipe> recipes = new ArrayList<>();
        for(Iterator<Recipe> iter = server.recipeIterator(); iter.hasNext();) {
            Recipe recipe = iter.next();
            boolean add = true;
            for(MaterialPattern result : disabledRecipes) {
                if(result.matches(recipe.getResult())) {
                    add = false;
                    break;
                }
            }
            if (add) {
                recipes.add(recipe);
            }
        }
        for (Recipe recipe: customRecipes) {
            recipes.add(recipe);
        }
        server.clearRecipes();

        for(Recipe recipe : recipes) {
            server.addRecipe(recipe);
        }
    }

    @Override
    public void disable() {
        // Recipe changes affect all worlds on the server, so we make changes at match start/end
        // to avoid interfering with adjacent matches. If we wait until unload() to reset them,
        // the next match would already be loaded.
        server.resetRecipes();
        super.disable();
    }
}
