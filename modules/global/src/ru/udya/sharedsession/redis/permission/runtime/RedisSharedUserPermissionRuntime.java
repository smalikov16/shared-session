package ru.udya.sharedsession.redis.permission.runtime;

import com.haulmont.cuba.core.entity.contracts.Id;
import com.haulmont.cuba.security.entity.User;
import org.springframework.stereotype.Component;
import ru.udya.sharedsession.permission.domain.SharedUserEntityAttributePermission;
import ru.udya.sharedsession.permission.domain.SharedUserPermission;
import ru.udya.sharedsession.permission.domain.SharedUserScreenElementPermission;
import ru.udya.sharedsession.permission.helper.SharedUserPermissionBuildHelper;
import ru.udya.sharedsession.permission.helper.SharedUserPermissionParentHelper;
import ru.udya.sharedsession.permission.helper.SharedUserPermissionWildcardHelper;
import ru.udya.sharedsession.permission.repository.SharedUserPermissionRepository;
import ru.udya.sharedsession.permission.runtime.SharedUserPermissionRuntime;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Component(SharedUserPermissionRuntime.NAME)
public class RedisSharedUserPermissionRuntime
        implements SharedUserPermissionRuntime {

    protected SharedUserPermissionBuildHelper permissionHelper;
    protected SharedUserPermissionWildcardHelper permissionWildcardHelper;
    protected SharedUserPermissionParentHelper permissionParentHelper;

    protected SharedUserPermissionRepository sharedUserPermissionRepository;

    @Override
    public boolean isPermissionGrantedToUser(Id<User, UUID> userId, SharedUserPermission permission) {

        // Redis implementation doesn't support so deep permissions
        if (permission instanceof SharedUserEntityAttributePermission
            || permission instanceof SharedUserScreenElementPermission) {

            return true;
        }

        List<SharedUserPermission> permissions =
                permissionParentHelper.calculateParentPermissions(permission);

        permissions = Stream.concat(Stream.of(permission),
                                    permissions.stream())
                            .distinct().collect(toList());

        for (SharedUserPermission perm : permissions) {
            var isGranted = sharedUserPermissionRepository
                    .isUserHasPermission(userId, perm);

            if (isGranted) {
                return true;
            }
        }

        return false;
    }
}