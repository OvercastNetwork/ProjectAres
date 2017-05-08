package tc.oc.api.users;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.User;
import tc.oc.api.docs.UserId;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.exceptions.NotFound;
import tc.oc.api.model.NullModelService;

public class NullUserService extends NullModelService<User, UserDoc.Partial> implements UserService {

    @Override
    public ListenableFuture<User> find(UserId userId) {
        return Futures.immediateFailedFuture(new NotFound());
    }

    @Override
    public ListenableFuture<UserSearchResponse> search(UserSearchRequest request) {
        return Futures.immediateFailedFuture(new NotFound());
    }

    @Override
    public ListenableFuture<LoginResponse> login(LoginRequest request) {
        return Futures.immediateFailedFuture(new NotFound());
    }

    @Override
    public ListenableFuture<?> logout(LogoutRequest request) {
        return Futures.immediateFuture(null);
    }

    @Override
    public ListenableFuture<UserUpdateResponse> creditRaindrops(UserId userId, CreditRaindropsRequest request) {
        return Futures.immediateFuture(UserUpdateResponse.FAILURE);
    }

    @Override
    public ListenableFuture<User> purchaseGizmo(UserId userId, PurchaseGizmoRequest request) {
        return Futures.immediateFailedFuture(new NotFound());
    }

    @Override
    public ListenableFuture<UserUpdateResponse> creditMaptokens(UserId userId, CreditMaptokensRequest request) {
        return Futures.immediateFuture(UserUpdateResponse.FAILURE);
    }

    @Override
    public ListenableFuture<UserUpdateResponse> creditMutationtokens(UserId userId, CreditMutationtokensRequest request) {
        return Futures.immediateFuture(UserUpdateResponse.FAILURE);
    }

    @Override
    public <T extends UserDoc.Partial> ListenableFuture<User> update(UserId userId, T update) {
        return Futures.immediateFailedFuture(new NotFound());
    }

    @Override
    public ListenableFuture<User> changeSetting(UserId userId, ChangeSettingRequest request) {
        return Futures.immediateFailedFuture(new NotFound());
    }

    @Override
    public ListenableFuture<User> changeClass(UserId userId, ChangeClassRequest request) {
        return Futures.immediateFailedFuture(new NotFound());
    }
}
