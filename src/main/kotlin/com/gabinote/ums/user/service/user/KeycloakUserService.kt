package com.gabinote.ums.user.service.user

import com.gabinote.ums.common.util.exception.service.ServerError
import com.gabinote.ums.user.domain.user.UserRepository
import jakarta.ws.rs.NotFoundException
import org.keycloak.admin.client.Keycloak
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class KeycloakUserService(
    private val keycloak: Keycloak,

    @Value("\${keycloak.admin-client.realm}")
    private val realm: String
) {

    fun updateUserGroup(
        userId: String,
        groupId: String
    ) {
        try {
            val beforeGroup = keycloak.realm(realm)
                .users()
                .get(userId)
                .groups()
            for (group in beforeGroup) {
                keycloak.realm(realm)
                    .users()
                    .get(userId)
                    .leaveGroup(group.id)
            }

            keycloak.realm(realm)
                .users()
                .get(userId)
                .joinGroup(groupId)
        } catch (e: NotFoundException) {
            throw ServerError(
                "keycloak user or group not found. userId: $userId, groupId: $groupId"
            )
        } catch (e: Exception) {
            throw e
        }
    }

}