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

package com.cloudcontactai.smsreceiver.model

import org.springframework.data.annotation.CreatedDate
import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "response_keywords")
class ResponseKeyword() {
    constructor(keyword: String) : this() {
        this.keyword = keyword
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "response_keywords_sequence")
    @SequenceGenerator(name = "response_keywords_sequence", sequenceName = "response_keywords_sequence", allocationSize = 1)
    var id: Long = 0

    @ManyToOne
    @JoinColumn(name = "phone_response_id")
    lateinit var phoneResponse: PhoneResponse

    @Column(name = "keyword", length = 500, nullable = false)
    var keyword: String? = null

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: Instant = Instant.now()
}