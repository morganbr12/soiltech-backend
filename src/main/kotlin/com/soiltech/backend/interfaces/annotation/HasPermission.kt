package com.soiltech.backend.interfaces.annotation

import org.springframework.security.access.prepost.PreAuthorize

/**
 * Enforces that the authenticated admin user holds the specified permission.
 * Usage: @HasPermission("users:view")
 *
 * Permissions follow the resource:action convention defined in AdminRbacSeeder.
 * Resolves via Spring Security hasAuthority() — admin permissions are loaded
 * as GrantedAuthority entries in UserDetailsServiceImpl.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAuthority('{value}')")
annotation class HasPermission(val value: String)
