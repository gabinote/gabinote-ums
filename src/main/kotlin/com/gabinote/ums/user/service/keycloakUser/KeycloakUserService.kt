package com.gabinote.ums.user.service.keycloakUser

import com.gabinote.ums.common.util.exception.service.ServerError
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


    fun updateUserRole(userId: String, roleName: String) {

        try {
            val userResource = keycloak.realm(realm).users().get(userId)
            val currentRoles = userResource.roles().realmLevel().listAll()
            userResource.roles().realmLevel().remove(currentRoles)
            val roleToAdd = keycloak.realm(realm).roles().get(roleName).toRepresentation()
            userResource.roles().realmLevel().add(listOf(roleToAdd))

        } catch (e: NotFoundException) {
            throw ServerError(
                "keycloak user or role not found. userId: $userId, roleName: $roleName"
            )
        } catch (e: Exception) {
            throw e
        }
    }

    fun getUserEmail(userId: String): String {
        try {
            val userResource = keycloak.realm(realm).users().get(userId)
            val userRepresentation = userResource.toRepresentation()
            return userRepresentation.email ?: throw ServerError(
                "keycloak user email not found. userId: $userId"
            )
        } catch (e: NotFoundException) {
            throw ServerError(
                "keycloak user not found. userId: $userId"
            )
        } catch (e: Exception) {
            throw e
        }
    }

    fun deleteUser(userId: String) {
        try {
            val response = keycloak.realm(realm).users().delete(userId)
            assert(response.status == 204) {
                "keycloak user not found. userId: $userId"
            }
        } catch (e: AssertionError) {
            throw ServerError(
                e.message ?: "An error occurred while deleting user",
            )
        } catch (e: Exception) {
            throw e
        }
    }

    fun disableUser(userId: String) {
        try {
            val userResource = keycloak.realm(realm).users().get(userId)
            val userRepresentation = userResource.toRepresentation()
            userRepresentation.isEnabled = false
            userResource.update(userRepresentation)
        } catch (e: NotFoundException) {
            throw ServerError(
                "keycloak user not found. userId: $userId"
            )
        } catch (e: Exception) {
            throw e
        }
    }
}