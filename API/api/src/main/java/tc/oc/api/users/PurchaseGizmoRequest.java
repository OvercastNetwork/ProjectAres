package tc.oc.api.users;

public class PurchaseGizmoRequest {
    public final String gizmo_name;
    public final int price;

    public PurchaseGizmoRequest(String gizmo_name, int price) {
        this.gizmo_name = gizmo_name;
        this.price = price;
    }
}
