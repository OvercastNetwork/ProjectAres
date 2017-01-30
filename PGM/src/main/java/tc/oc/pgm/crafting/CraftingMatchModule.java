package tc.oc.pgm.crafting;

import java.util.Iterator;
import java.util.Set;

import org.bukkit.inventory.Recipe;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.utils.MaterialPattern;

public class CraftingMatchModule extends MatchModule {

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

        for(Iterator<Recipe> iter = getMatch().getServer().recipeIterator(); iter.hasNext();) {
            Recipe recipe = iter.next();
            for(MaterialPattern result : disabledRecipes) {
                if(result.matches(recipe.getResult())) {
                    iter.remove();
                    break;
                }
            }
        }

        for(Recipe recipe : customRecipes) {
            getMatch().getServer().addRecipe(recipe);
        }
    }

    @Override
    public void disable() {
        // Recipe changes affect all worlds on the server, so we make changes at match start/end
        // to avoid interfering with adjacent matches. If we wait until unload() to reset them,
        // the next match would already be loaded.
        getMatch().getServer().resetRecipes();
        super.disable();
    }
}
