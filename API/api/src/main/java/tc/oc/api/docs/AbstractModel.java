package tc.oc.api.docs;

import javax.inject.Inject;

import com.google.gson.Gson;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.serialization.Pretty;

/**
 * Implements some boilerplate stuff for {@link Model}
 */
public abstract class AbstractModel implements Model {

    protected @Inject @Pretty Gson prettyGson;

    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;
        if(!(o instanceof Model))
            return false;

        return _id().equals(((Model) o)._id());
    }

    @Override
    public int hashCode() {
        return _id().hashCode();
    }

    @Override
    public String toString() {
        if(prettyGson == null) return super.toString();

        try {
            return prettyGson.toJson(this);
        } catch(Exception e) {
            return super.toString() + " (exception trying to inspect fields: " + e + ")";
        }
    }
}
