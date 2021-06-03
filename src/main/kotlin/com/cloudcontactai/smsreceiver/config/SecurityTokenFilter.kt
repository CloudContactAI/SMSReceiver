/* Copyright 2021 Cloud Contact AI, Inc. All Rights Reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.cloudcontactai.smsreceiver.config


import com.cloudcontactai.smsreceiver.util.UserAuthority
import io.jsonwebtoken.Jwts
import org.apache.tomcat.util.codec.binary.Base64
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.annotation.PostConstruct
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SecurityTokenFilter : OncePerRequestFilter() {
    private val ignoreUrlPatterns = listOf("/api/v1/sms/incoming")

    @Value("\${auth.token.publicKey}")
    lateinit var publicKeyString: String

    lateinit var publicKey:PublicKey

    @PostConstruct
    fun configure(){
        val keyBytes = Base64.decodeBase64(publicKeyString)
        this.publicKey= KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(keyBytes))
    }

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {

        /** Options method, used by CORS requires no auth. */
        if(HttpMethod.OPTIONS.matches(request.method)){
            filterChain.doFilter(request,response)
            return
        }

        val authHeader = request.getHeader("Authorization");
        if(authHeader == null){
            response.status = HttpStatus.UNAUTHORIZED.value()
            return
        }
        try {
            val token = authHeader.removePrefix("Bearer ")
            val tokenBody = Jwts.parser().setSigningKey(this.publicKey).parseClaimsJws(token).body
            val email = tokenBody.subject
            val role = tokenBody.get("role", String::class.java);
            SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
                    UserAuthority(email),
                    token,
                    listOf(SimpleGrantedAuthority(role))
            )
        }
        catch (e: Exception){
            logger.debug(e)
            response.status = HttpStatus.UNAUTHORIZED.value()
            return
        }
        filterChain.doFilter(request,response)
    }

    @Throws(ServletException::class)
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val pathMatcher = AntPathMatcher()
        return ignoreUrlPatterns.stream()
                .anyMatch { p -> pathMatcher.match(p, request.servletPath) }
    }
}
