package tc.oc.api.users;

import java.util.UUID;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.User;
import tc.oc.api.docs.UserId;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.model.ModelService;

public interface UserService extends ModelService<User, UserDoc.Partial> {

    ListenableFuture<User> find(UserId userId);

    ListenableFuture<UserSearchResponse> search(UserSearchRequest request);

    ListenableFuture<LoginResponse> login(LoginRequest request);

    ListenableFuture<?> logout(LogoutRequest request);

    default ListenableFuture<UserUpdateResponse> creditRaindrops(UserId userId, int raindrops) {
        return creditRaindrops(userId, () -> raindrops);
    }

    ListenableFuture<UserUpdateResponse> creditRaindrops(UserId userId, CreditRaindropsRequest request);

    ListenableFuture<User> purchaseGizmo(UserId userId, PurchaseGizmoRequest request);

    default ListenableFuture<UserUpdateResponse> creditMaptokens(UserId userId, int maptokens) {
        return creditMaptokens(userId, () -> maptokens);
    }

    ListenableFuture<UserUpdateResponse> creditMaptokens(UserId userId, CreditMaptokensRequest request);

    default ListenableFuture<UserUpdateResponse> creditMutationtokens(UserId userId, int mutationtokens) {
        return creditMutationtokens(userId, () -> mutationtokens);
    }

    ListenableFuture<UserUpdateResponse> creditMutationtokens(UserId userId, CreditMutationtokensRequest request);

    <T extends UserDoc.Partial> ListenableFuture<User> update(UserId userId, T update);

    ListenableFuture<User> changeSetting(UserId userId, ChangeSettingRequest request);

    ListenableFuture<User> changeClass(UserId userId, ChangeClassRequest request);

    default void requestTeleport(UUID travelerId, ServerDoc.Identity targetServer, UUID targetId) {}
}
