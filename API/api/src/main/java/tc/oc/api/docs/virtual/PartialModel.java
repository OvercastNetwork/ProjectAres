package tc.oc.api.docs.virtual;

/**
 * A subset of the fields in some {@link Model} serving some particular use case.
 *
 * ### How the model hiearchy works ###
 *
 *      PartialModel ---------> Thing.Partial
 *           |                       |
 *           |                +------+------+
 *           |                |             |
 *           |                v             v
 *           |          Thing.FooInfo   Thing.BarInfo
 *           |                |             |
 *           |                +------+------+
 *           |                       |
 *           |                       v
 *         Model ------------> Thing.Complete
 *
 * The hiearchy for a model called Thing would start with an empty interface
 * called Thing.Partial that extends {@link PartialModel}. For every sub-group
 * of Thing fields that are sent/received together, there would be an interface
 * extending Thing.Partial that declares getter methods for those fields only.
 *
 * At the bottom of the hiearchy would be an interface called Thing.Complete
 * that extends {@link Model} and ALL of the Thing parts. Any fields that are not
 * declared in any of the parts should be declared in the complete model.
 *
 * Fields can be shared among different parts of the same model, as long as they
 * have identical signatures in all of the parts where they appear.
 *
 * This system ties together all the parts of a model in a type-safe way, ensuring
 * that the fields stay in sync. Model classes should be created for specific cases,
 * and only implement the interfaces containing the fields that they use, avoiding
 * empty fields and nulls.
 */
public interface PartialModel extends Document {}
