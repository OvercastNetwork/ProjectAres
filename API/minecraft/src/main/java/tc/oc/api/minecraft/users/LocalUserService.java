package tc.oc.api.minecraft.users;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Punishment;
import tc.oc.api.docs.Session;
import tc.oc.api.docs.User;
import tc.oc.api.docs.UserId;
import tc.oc.api.docs.Whisper;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.exceptions.NotFound;
import tc.oc.api.minecraft.sessions.LocalSessionFactory;
import tc.oc.api.model.NullModelService;
import tc.oc.api.users.*;
import tc.oc.commons.core.concurrent.FutureUtils;
import tc.oc.minecraft.api.user.UserFinder;

@Singleton
class LocalUserService extends NullModelService<User, UserDoc.Partial> implements UserService {

    @Inject private LocalSessionFactory sessionFactory;
    @Inject private UserFinder userFinder;

    @Override
    public ListenableFuture<User> find(UserId userId) {
        return FutureUtils.mapSync(
            userFinder.findUserAsync(UUID.fromString(userId.player_id())),
            user -> {
                if(user.hasValidId() && user.name().isPresent()) {
                    return new LocalUserDocument(user);
                }
                throw new NotFound("No user with UUID " + userId.player_id());
            }
        );
    }

    @Override
    public ListenableFuture<UserSearchResponse> search(UserSearchRequest request) {
        return FutureUtils.mapSync(
            userFinder.findUserAsync(request.username),
            user -> {
                if(user.hasValidId()) {
                    return new UserSearchResponse(new LocalUserDocument(user), user.isOnline(), false, null, null);
                }
                throw new NotFound("No user named '" + request.username + "'");
            }
        );
    }

    @Override
    public ListenableFuture<LoginResponse> login(LoginRequest request) {
        final User user = new LocalUserDocument(request.uuid, request.username, request.ip.getHostAddress());
        final Session session = request.start_session ? sessionFactory.newSession(user, request.ip)
                                                      : null;

        return Futures.immediateFuture(new LoginResponse() {
            @Override
            public @Nullable String kick() {
                return null;
            }

            @Override
            public @Nullable String message() {
                return null;
            }

            @Override
            public @Nullable String route_to_server() {
                return null;
            }

            @Override
            public User user() {
                return user;
            }

            @Override
            public @Nullable Session session() {
                return session;
            }

            @Override
            public @Nullable Punishment punishment() {
                return null;
            }

            @Override
            public List<Whisper> whispers() {
                return Collections.emptyList();
            }

            @Override
            public int unread_appeal_count() {
                return 0;
            }
        });
    }

    @Override
    public ListenableFuture<?> logout(LogoutRequest request) {
        return Futures.immediateFuture(null);
    }

    private ListenableFuture<UserUpdateResponse> update(UserId userId) {
        return FutureUtils.mapSync(find(userId), user -> new UserUpdateResponse() {
            @Override
            public boolean success() {
                return true;
            }

            @Override
            public User user() {
                return user;
            }
        });
    }

    @Override
    public ListenableFuture<UserUpdateResponse> creditTokens(UserId userId, CreditTokensRequest request) {
        return update(userId);
    }

    @Override
    public ListenableFuture<User> changeGroup(UserId userId, ChangeGroupRequest request) {
        return find(userId);
    }

    @Override
    public ListenableFuture<FriendJoinResponse> joinFriend(UserId userId, FriendJoinRequest request) {
        return Futures.immediateFuture(new FriendJoinResponse() {
            @Override
            public boolean authorized() {
                return false;
            }

            @Override
            public String message() {
                return null;
            }
        });
    }

    @Override
    public ListenableFuture<User> purchaseGizmo(UserId userId, PurchaseGizmoRequest request) {
        return find(userId);
    }

    @Override
    public <T extends UserDoc.Partial> ListenableFuture<User> update(UserId userId, T update) {
        return find(userId);
    }

    @Override
    public ListenableFuture<User> changeSetting(UserId userId, ChangeSettingRequest request) {
        return find(userId);
    }

    @Override
    public ListenableFuture<User> changeClass(UserId userId, ChangeClassRequest request) {
        return find(userId);
    }
}
