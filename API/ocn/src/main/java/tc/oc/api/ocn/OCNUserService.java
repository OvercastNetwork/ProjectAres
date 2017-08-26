package tc.oc.api.ocn;

import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.User;
import tc.oc.api.docs.UserId;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.http.HttpOption;
import tc.oc.api.message.types.PlayerTeleportRequest;
import tc.oc.api.minecraft.users.UserStore;
import tc.oc.api.model.HttpModelService;
import tc.oc.api.queue.Exchange;
import tc.oc.api.users.*;
import tc.oc.commons.core.concurrent.FutureUtils;
import tc.oc.minecraft.api.entity.Player;

@Singleton
class OCNUserService extends HttpModelService<User, UserDoc.Partial> implements UserService {

    @Inject private UserStore<Player> userStore;
    @Inject private Exchange.Topic topic;

    protected String memberUri(UserId userId) {
        return memberUri(userId.player_id());
    }

    protected String memberUri(UserId userId, String action) {
        return memberUri(userId.player_id(), action);
    }

    @Override
    protected void handleUpdate(User doc) {
        userStore.handleUpdate(doc);
    }

    protected ListenableFuture<UserUpdateResponse> handleUserUpdate(ListenableFuture<UserUpdateResponse> future) {
        return FutureUtils.peek(future, result -> {
            if(result.success() && result.user() != null) {
                handleUpdate(result.user());
            }
        });
    }

    @Override
    public ListenableFuture<User> find(UserId userId) {
        final User user = userStore.tryUser(userId);
        if(user != null) {
            return Futures.immediateFuture(user);
        } else {
            return find(userId.player_id());
        }
    }

    @Override
    public ListenableFuture<UserSearchResponse> search(UserSearchRequest request) {
        return client().post(collectionUri("search"), request, UserSearchResponse.class, HttpOption.INFINITE_RETRY);
    }

    @Override
    public ListenableFuture<LoginResponse> login(LoginRequest request) {
        return client().post(collectionUri("login"), request, LoginResponse.class, HttpOption.INFINITE_RETRY);
    }

    @Override
    public ListenableFuture<?> logout(LogoutRequest request) {
        return client().post(memberUri(request.player_id, "logout"), request, HttpOption.INFINITE_RETRY);
    }

    @Override
    public ListenableFuture<User> purchaseGizmo(UserId userId, PurchaseGizmoRequest request) {
        return handleUpdate(client().post(memberUri(userId, "purchase_gizmo"), request, User.class, HttpOption.INFINITE_RETRY));
    }

    @Override
    public ListenableFuture<UserUpdateResponse> creditTokens(UserId userId, CreditTokensRequest request) {
        return handleUserUpdate(client().post(memberUri(userId, "credit_tokens"), request, UserUpdateResponse.class, HttpOption.INFINITE_RETRY));
    }

    @Override
    public ListenableFuture<User> changeGroup(UserId userId, ChangeGroupRequest request) {
        return handleUpdate(client().post(memberUri(userId, "change_group"), request, User.class, HttpOption.INFINITE_RETRY));
    }

    @Override
    public ListenableFuture<FriendJoinResponse> joinFriend(UserId userId, FriendJoinRequest request) {
        return client().post(memberUri(userId, "join_friend"), request, FriendJoinResponse.class, HttpOption.INFINITE_RETRY);
    }

    @Override
    public <T extends UserDoc.Partial> ListenableFuture<User> update(UserId userId, T update) {
        return update(userId.player_id(), update);
    }

    @Override
    public ListenableFuture<User> changeSetting(UserId userId, ChangeSettingRequest request) {
        return handleUpdate(client().post(memberUri(userId, "change_setting"), request, User.class, HttpOption.INFINITE_RETRY));
    }

    @Override
    public ListenableFuture<User> changeClass(UserId userId, ChangeClassRequest request) {
        return handleUpdate(client().post(memberUri(userId, "change_class"), request, User.class, HttpOption.INFINITE_RETRY));
    }

    @Override
    public void requestTeleport(UUID travelerId, ServerDoc.Identity targetServer, UUID targetId) {
        topic.publishAsync(new PlayerTeleportRequest(travelerId, targetServer, targetId));
    }
}
