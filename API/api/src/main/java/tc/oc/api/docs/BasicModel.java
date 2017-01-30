package tc.oc.api.docs;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Model;

/**
 * Implements {@link Model#_id()} as a public field.
 */
public class BasicModel extends AbstractModel {

    @Serialize public String _id;

    public BasicModel() {
    }

    public BasicModel(String _id) {
        this._id = _id;
    }

    @Override
    public String _id() {
        return _id;
    }

    public String getId() {
        return _id;
    }
}
