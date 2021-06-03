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

package com.cloudcontactai.smsreceiver.repository

import com.cloudcontactai.smsreceiver.model.PhoneResponse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PhoneResponseRepository : JpaRepository<PhoneResponse, Long> {
    @Query("SELECT r FROM PhoneResponse r WHERE r.clientId = ?1 AND r.deletedAt IS NULL")
    fun findAllByClientId(clientId: Long): List<PhoneResponse>
    @Query("SELECT r FROM PhoneResponse r WHERE r.clientId = ?1 AND r.phoneNumber = ?2 AND r.deletedAt IS NULL")
    fun findAllByClientIdAndPhoneNumber(clientId: Long, phone: String): List<PhoneResponse>

    fun findAllByClientIdIn(clients: List<Long>): List<PhoneResponse>

    fun findAllByPhoneNumberAndKeywordsKeywordContainsIgnoreCase(phone: String, keyword: String): List<PhoneResponse>
}