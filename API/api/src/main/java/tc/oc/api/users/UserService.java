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

    ListenableFuture<UserUpdateResponse> creditTokens(UserId userId, CreditTokensRequest request);

    ListenableFuture<User> changeGroup(UserId userId, ChangeGroupRequest request);

    ListenableFuture<FriendJoinResponse> joinFriend(UserId userId, FriendJoinRequest request);

    ListenableFuture<User> purchaseGizmo(UserId userId, PurchaseGizmoRequest request);

    <T extends UserDoc.Partial> ListenableFuture<User> update(UserId userId, T update);

    ListenableFuture<User> changeSetting(UserId userId, ChangeSettingRequest request);

    ListenableFuture<User> changeClass(UserId userId, ChangeClassRequest request);

    default void requestTeleport(UUID travelerId, ServerDoc.Identity targetServer, UUID targetId) {}
}
